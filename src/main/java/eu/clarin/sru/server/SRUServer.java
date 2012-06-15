/**
 * This software is copyright (c) 2011 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z3950.zing.cql.CQLNode;

import eu.clarin.sru.server.SRUServerConfig.DatabaseInfo;
import eu.clarin.sru.server.SRUServerConfig.IndexInfo;
import eu.clarin.sru.server.SRUServerConfig.LocalizedString;
import eu.clarin.sru.server.SRUServerConfig.SchemaInfo;


/**
 * SRU/CQL protocol implementation for the server-side (SRU/S). This class
 * implements SRU/CQL version 1.1 and and 1.2.
 * <p>
 * An example servlet using this class:
 * </p>
 * <pre>
 * public class MySRUServlet extends HttpServlet {
 *     private transient SRUServer sruServer;
 *
 *
 *     public void init() throws ServletException {
 *         final ServletContext ctx = getServletContext();
 *         try {
 *             URL url = MySRUServlet.class.getClassLoader().getResource(
 *                     &quot;META-INF/endpoint-config.xml&quot;);
 *             if (url == null) {
 *                 throw new ServletException(&quot;not found, url == null&quot;);
 *             }
 *
 *             // get additional runtime configuration from Servlet context
 *             HashMap&lt;String, String&gt; params = new HashMap&lt;String, String&gt;();
 *             for (Enumeration&lt;?&gt; i = ctx.getInitParameterNames(); i
 *                     .hasMoreElements();) {
 *                 String key = (String) i.nextElement();
 *                 String value = ctx.getInitParameter(key);
 *                 if ((value != null) &amp;&amp; !value.isEmpty()) {
 *                     params.put(key, value);
 *                 }
 *             }
 *
 *             SRUServerConfig config = SRUServerConfig.parse(params,
 *                     url.openStream());
 *             SRUSearchEngine searchEngine = new MySRUSearchEngine(config, params);
 *             sruServer = new SRUServer(config, searchEngine);
 *         } catch (Exception e) {
 *             throw new ServletException(&quot;error initializing endpoint&quot;, e);
 *         }
 *     }
 *
 *
 *     protected void doGet(HttpServletRequest request,
 *             HttpServletResponse response) throws ServletException, IOException {
 *         sruServer.handleRequest(request, response);
 *     }
 *
 *
 *     protected void doPost(HttpServletRequest request,
 *             HttpServletResponse response) throws ServletException, IOException {
 *         sruServer.handleRequest(request, response);
 *     }
 * }
 * </pre>
 *
 * @see SRUServerConfig
 * @see SRUSearchEngine
 * @see <a href="http://www.loc.gov/standards/sru/">SRU/CQL protocol 1.2</a>
 */
public class SRUServer {
    private static final String SRU_NS =
            "http://www.loc.gov/zing/srw/";
    private static final String SRU_PREFIX = "sru";
    private static final String SRU_DIAGNOSIC_NS =
            "http://www.loc.gov/zing/srw/diagnostic/";
    private static final String SRU_DIAGNOSTIC_PREFIX = "diag";
    private static final String SRU_DIAGNOSTIC_RECORD_SCHEMA =
            "info:srw/schema/1/diagnostics-v1.1";
    private static final String SRU_EXPLAIN_NS =
            "http://explain.z3950.org/dtd/2.0/";
    private static final String SRU_EXPLAIN_PREFIX = "zr";
    private static final String SRU_XCQL_NS =
            "http://www.loc.gov/zing/cql/xcql/";
    static final String RESPONSE_ENCODING = "utf-8";
    private static final String RESPONSE_CONTENT_TYPE = "application/xml";
    private static final int RESPONSE_BUFFER_SIZE = 64 * 1024;
    private static final Logger logger =
            LoggerFactory.getLogger(SRUServer.class);
    private final SRUServerConfig config;
    private final SRUSearchEngine searchEngine;
    private final XMLOutputFactory writerFactory;


    /**
     * Constructor.
     *
     * @param config
     *            a SRUEndpointConfig object
     * @param searchEngine
     *            an object implementing the SRUSearchEngine interface
     * @throws NullPointerException
     *             if config or searchEngine is <code>null</code>
     * @throws SRUException
     *             if an error occurred
     */
    public SRUServer(SRUServerConfig config, SRUSearchEngine searchEngine)
            throws SRUException {
        if (config == null) {
            throw new NullPointerException("config == null");
        }
        this.config = config;
        if (searchEngine == null) {
            throw new NullPointerException("searchEngine == null");
        }
        this.searchEngine = searchEngine;
        this.writerFactory = XMLOutputFactory.newInstance();
    }


    /**
     * Handle a SRL/CQL request.
     *
     * @param request
     *            a HttpServletRequest request
     * @param response
     *            a HttpServletResponse request
     */
    public void handleRequest(HttpServletRequest request,
            HttpServletResponse response) {
        final SRURequestImpl req = new SRURequestImpl(config, request);
        try {
            // set response properties
            response.setContentType(RESPONSE_CONTENT_TYPE);
            response.setCharacterEncoding(RESPONSE_ENCODING);
            response.setStatus(HttpServletResponse.SC_OK);
            // make sure we can reset the stream later in case of error ...
            response.setBufferSize(RESPONSE_BUFFER_SIZE);

            try {
                if (req.checkParameters()) {
                    switch (req.getOperation()) {
                    case EXPLAIN:
                        explain(req, response);
                        break;
                    case SCAN:
                        scan(req, response);
                        break;
                    case SEARCH_RETRIEVE:
                        search(req, response);
                        break;
                    }
                } else {
                    // (some) parameters are malformed, send error
                    SRUXMLStreamWriter out =
                        createXMLStreamWriter(response.getOutputStream(),
                                SRURecordPacking.XML, req.getIndentResponse());
                    writeFatalError(out, req, req.getDiagnostics());
                }
            } catch (XMLStreamException e) {
                logger.error("An error occured while serializing reponse", e);
                throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                        "An error occured while serializing reponse", e);
            } catch (IOException e) {
                /*
                 * Well, can't really do anything useful here ...
                 */
                logger.error("An unexpected exception occured", e);
            }
        } catch (SRUException e) {
            if (!response.isCommitted()) {
                response.resetBuffer();
                try {
                    List<SRUDiagnostic> diagnostics = req.getDiagnostics();
                    if (diagnostics != null) {
                        diagnostics.add(e.getDiagnostic());
                    } else {
                        diagnostics = Arrays.asList(e.getDiagnostic());
                    }
                    SRUXMLStreamWriter out =
                            createXMLStreamWriter(response.getOutputStream(),
                                    SRURecordPacking.XML,
                                    req.getIndentResponse());
                    writeFatalError(out, req, diagnostics);
                } catch (Exception ex) {
                    logger.error("An exception occured while in error state",
                            ex);
                }
            } else {
                logger.error("A fatal error occured, but the response was "
                        + "already committed", e);
            }
        }
    }


    private void explain(SRURequestImpl request, HttpServletResponse response)
            throws IOException, XMLStreamException, SRUException {
        logger.info("explain");

        // commence explain ...
        SRUExplainResult result = searchEngine.explain(config,
                request, request);

        // send results
        SRUXMLStreamWriter out =
                createXMLStreamWriter(response.getOutputStream(),
                                      request.getRecordPacking(),
                                      request.getIndentResponse());

        beginResponse(out, request);

        // write the explain record
        writeExplainRecord(out, request);

        if (config.getEchoRequests()) {
            writeEchoedExplainRequest(out, request);
        }

        // diagnostics
        writeDiagnosticList(out, request.getDiagnostics());

        // extraResponseData
        if (result != null) {
            if (result.hasExtraResponseData()) {
                out.writeStartElement(SRU_NS, "extraResponseData");
                result.writeExtraResponseData(out);
                out.writeEndElement(); // "extraResponseData" element
            }
        }

        endResponse(out);
    }


    private void scan(SRURequestImpl request, HttpServletResponse response)
            throws IOException, XMLStreamException, SRUException {
        logger.info("scan: scanClause = \"{}\"",
                new Object[] { request.getRawScanClause() });

        // commence scan
        final SRUScanResultSet result = searchEngine.scan(config,
                request, request);
        if (result == null) {
            throw new SRUException(SRUConstants.SRU_UNSUPPORTED_OPERATION,
                    "The 'scan' operation is not supported by this endpoint.");
        }

        // send results
        SRUXMLStreamWriter out =
                createXMLStreamWriter(response.getOutputStream(),
                                      request.getRecordPacking(),
                                      request.getIndentResponse());

        beginResponse(out, request);

        try {
            out.writeStartElement(SRU_NS, "terms");
            while (result.hasMoreTerms()) {
                out.writeStartElement(SRU_NS, "term");

                out.writeStartElement(SRU_NS, "value");
                out.writeCharacters(result.getValue());
                out.writeEndElement(); // "value" element

                if (result.getNumberOfRecords() > -1) {
                    out.writeStartElement(SRU_NS, "numberOfRecords");
                    out.writeCharacters(
                            Integer.toString(result.getNumberOfRecords()));
                    out.writeEndElement(); // "numberOfRecords" element
                }

                if (result.getDisplayTerm() != null) {
                    out.writeStartElement(SRU_NS, "displayTerm");
                    out.writeCharacters(result.getDisplayTerm());
                    out.writeEndElement(); // "displayTerm" element
                }

                if (result.getWhereInList() != null) {
                    out.writeStartElement(SRU_NS, "whereInList");
                    switch (result.getWhereInList()) {
                    case FIRST:
                        out.writeCharacters("first");
                        break;
                    case LAST:
                        out.writeCharacters("last");
                        break;
                    case ONLY:
                        out.writeCharacters("only");
                        break;
                    case INNER:
                        out.writeCharacters("inner");
                        break;
                    } // switch
                    out.writeEndElement(); // "whereInList" element
                }

                if (result.hasExtraTermData()) {
                    out.writeStartElement(SRU_NS, "extraTermData");
                    result.writeExtraTermData(out);
                    out.writeEndElement(); // "extraTermData" element
                }

                out.writeEndElement(); // "term" element

                result.nextTerm();
            }
            out.writeEndElement(); // "terms" element
        } catch (NoSuchElementException e) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    "An internal error occurred while " +
                    "serializing scan results.");
        }

        // echoedScanRequest
        if (config.getEchoRequests()) {
            writeEchoedScanRequest(out, request, request.getScanClause());
        }

        // diagnostics
        writeDiagnosticList(out, request.getDiagnostics());

        // extraResponseData
        if (result.hasExtraResponseData()) {
            out.writeStartElement(SRU_NS, "extraResponseData");
            result.writeExtraResponseData(out);
            out.writeEndElement(); // "extraResponseData" element
        }

        endResponse(out);
    }


    private void search(SRURequestImpl request, HttpServletResponse response)
            throws IOException, XMLStreamException, SRUException {
        logger.info("searchRetrieve: query = \"{}\", startRecord = {}, " +
                "maximumRecords = {}, recordSchema = {}, resultSetTTL = {}",
                new Object[] { request.getRawQuery(), request.getStartRecord(),
                        request.getMaximumRecords(),
                        request.getRecordSchemaIdentifier(),
                        request.getResultSetTTL() });

        // commence search ...
        final SRUSearchResultSet result = searchEngine.search(config,
                request, request);
        if (result == null) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    "Database implementation returned invalid result (null).");
        }

        // send results
        SRUXMLStreamWriter out =
                createXMLStreamWriter(response.getOutputStream(),
                                      request.getRecordPacking(),
                                      request.getIndentResponse());

        beginResponse(out, request);

        // numberOfRecords
        out.writeStartElement(SRU_NS, "numberOfRecords");
        out.writeCharacters(Integer.toString(result.getTotalRecordCount()));
        out.writeEndElement(); // "numberOfRecords" element

        // resultSetId
        if (result.getResultSetId() != null) {
            out.writeStartElement(SRU_NS, "resultSetId");
            out.writeCharacters(result.getResultSetId());
            out.writeEndElement(); // "resultSetId" element
        }

        // resultSetIdleTime
        if (result.getResultSetIdleTime() > 0) {
            out.writeStartElement(SRU_NS, "resultSetIdleTime");
            out.writeCharacters(
                    Integer.toString(result.getResultSetIdleTime()));
            out.writeEndElement();  // "resultSetIdleTime" element
        }

        int position = (request.getStartRecord() > 0)
                     ? request.getStartRecord() : 1;
        if (result.getRecordCount() > 0) {
            final int maxPositionOffset =
                    (request.getMaximumRecords() != -1)
                    ? (position + request.getMaximumRecords() - 1) : - 1;
            try {
                out.writeStartElement(SRU_NS, "records");
                while (result.hasMoreRecords()) {
                    /*
                     * Sanity check: do not return more then the maximum
                     * requested records. If database implementation does
                     * not honor limit truncate the result set.
                     */
                    if ((maxPositionOffset != -1) &&
                            (position > maxPositionOffset)) {
                        logger.error("SRUSearchEngine implementation did not " +
                                "honor limit for the amount of requsted " +
                                "records. Result set truncated!");
                        break;
                    }

                    out.writeStartElement(SRU_NS, "record");

                    /*
                     *  We need to output either the record or a
                     *  surrogate diagnostic. In case of the latter, we need
                     *  to output the appropriate record schema ...
                     */
                    SRUDiagnostic diagnostic = result.getSurrogateDiagnostic();

                    out.writeStartElement(SRU_NS, "recordSchema");
                    if (diagnostic == null) {
                        out.writeCharacters(result.getRecordSchemaIdentifier());
                    } else {
                        out.writeCharacters(SRU_DIAGNOSTIC_RECORD_SCHEMA);
                    }
                    out.writeEndElement(); // "recordSchema" element

                    // recordPacking
                    writeRecordPacking(out, request.getRecordPacking());

                    /*
                     * Output either record data or surrogate diagnostic ...
                     */
                    out.writeStartElement(SRU_NS, "recordData");
                    out.startRecord();
                    if (diagnostic == null) {
                        result.writeRecord(out);
                    } else {
                        // write a surrogate diagnostic
                        writeDiagnostic(out, diagnostic, true);
                    }
                    out.endRecord();
                    out.writeEndElement(); // "recordData" element

                    /*
                     * recordIdentifier is version 1.2 only
                     */
                    if (request.isVersion(SRUVersion.VERSION_1_2)) {
                        final String identifier = result.getRecordIdentifier();
                        if (identifier != null) {
                            out.writeStartElement(SRU_NS, "recordIdentifier");
                            out.writeCharacters(identifier);
                            out.writeEndElement(); // "recordIdentifier" element
                        }
                    }

                    out.writeStartElement(SRU_NS, "recordPosition");
                    out.writeCharacters(Integer.toString(position));
                    out.writeEndElement(); // "recordPosition" element

                    if (result.hasExtraRecordData()) {
                        out.writeStartElement(SRU_NS, "extraRecordData");
                        result.writeExtraRecordData(out);
                        out.writeEndElement(); // "extraRecordData"
                    }

                    out.writeEndElement(); // "record" element

                    result.nextRecord();
                    position++;
                }
                out.writeEndElement(); // "records" element
            } catch (NoSuchElementException e) {
                throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                        "An internal error occurred while " +
                        "serializing search result set.");
            }
        }

        // nextRecordPosition
        if (position <= result.getTotalRecordCount()) {
            out.writeStartElement(SRU_NS, "nextRecordPosition");
            out.writeCharacters(Integer.toString(position));
            out.writeEndElement();
        }

        // echoedSearchRetrieveRequest
        if (config.getEchoRequests()) {
            writeEchoedSearchRetrieveRequest(out, request, request.getQuery());
        }

        // diagnostics
        writeDiagnosticList(out, request.getDiagnostics());

        // extraResponseData
        if (result.hasExtraResponseData()) {
            out.writeStartElement(SRU_NS, "extraResponseData");
            result.writeExtraResponseData(out);
            out.writeEndElement(); // "extraResponseData" element
        }

        endResponse(out);
    }


    private void beginResponse(SRUXMLStreamWriter out, SRUOperation operation,
            SRUVersion version, String stylesheet) throws XMLStreamException {
        out.writeStartDocument("utf-8", "1.0");

        if (stylesheet != null) {
            StringBuilder param = new StringBuilder();
            param.append("type=\"text/xsl\"");
            param.append(" ");
            param.append("href=\"");
            param.append(stylesheet);
            param.append("\"");
            out.writeProcessingInstruction("xml-stylesheet", param.toString());
        }

        out.setPrefix(SRU_PREFIX, SRU_NS);
        switch (operation) {
        case EXPLAIN:
            out.writeStartElement(SRU_NS, "explainResponse");
            break;
        case SCAN:
            out.writeStartElement(SRU_NS, "scanResponse");
            break;
        case SEARCH_RETRIEVE:
            out.writeStartElement(SRU_NS, "searchRetrieveResponse");
            break;
        }
        out.writeNamespace(SRU_PREFIX, SRU_NS);

        // version
        writeVersion(out, version);
    }


    private void beginResponse(SRUXMLStreamWriter out, SRURequest request)
            throws XMLStreamException {
        beginResponse(out, request.getOperation(), request.getVersion(),
                request.getStylesheet());
    }


    private void endResponse(SRUXMLStreamWriter out)
            throws XMLStreamException {
        out.writeEndElement(); // "root" element

        out.writeEndDocument();
        out.close();
        try {
            out.getWriter().close();
        } catch (IOException e) {
            /* IGNORE */
        }
    }


    private void writeFatalError(SRUXMLStreamWriter out,
            SRURequestImpl request, List<SRUDiagnostic> diagnotics)
            throws XMLStreamException {
        /*
         * if operation is unknown, default to 'explain'
         */
        SRUOperation operation = request.getOperation();
        if (operation == null) {
            operation = SRUOperation.EXPLAIN;
        }
        SRUVersion version = request.getVersion();
        if (version == null) {
            version = config.getDefaultVersion();
        }
        /*
         * write a response which conforms to the schema
         */
        beginResponse(out, operation, version, null);
        switch (operation) {
        case EXPLAIN:
            // 'explain' requires a complete explain record ...
            writeExplainRecord(out, request);
            break;
        case SCAN:
            // 'scan' fortunately does not need any elements ...
            break;
        case SEARCH_RETRIEVE:
            // 'searchRetrieve' needs numberOfRecords ..
            out.writeStartElement(SRU_NS, "numberOfRecords");
            out.writeCharacters("0");
            out.writeEndElement(); // "numberOfRecords" element
            break;
        }
        writeDiagnosticList(out, diagnotics);
        endResponse(out);
    }


    private void writeDiagnosticList(SRUXMLStreamWriter out,
            List<SRUDiagnostic> diagnostics) throws XMLStreamException {
        if ((diagnostics != null) && !diagnostics.isEmpty()) {
            out.setPrefix(SRU_DIAGNOSTIC_PREFIX, SRU_DIAGNOSIC_NS);
            out.writeStartElement(SRU_NS, "diagnostics");
            out.writeNamespace(SRU_DIAGNOSTIC_PREFIX, SRU_DIAGNOSIC_NS);
            for (SRUDiagnostic diagnostic : diagnostics) {
                writeDiagnostic(out, diagnostic, false);
            }
            out.writeEndElement(); // "diagnostics" element
        }
    }


    private void writeExplainRecord(SRUXMLStreamWriter out,
            SRURequestImpl request) throws XMLStreamException {
        out.writeStartElement(SRU_NS, "record");

        out.writeStartElement(SRU_NS, "recordSchema");
        out.writeCharacters(SRU_EXPLAIN_NS);
        out.writeEndElement(); // "recordSchema" element

        // recordPacking
        writeRecordPacking(out, request.getRecordPacking());

        out.writeStartElement(SRU_NS, "recordData");

        out.startRecord();

        // explain ...
        out.setPrefix(SRU_EXPLAIN_PREFIX, SRU_EXPLAIN_NS);
        out.writeStartElement(SRU_EXPLAIN_NS, "explain");
        out.writeNamespace(SRU_EXPLAIN_PREFIX, SRU_EXPLAIN_NS);

        // explain/serverInfo
        out.writeStartElement(SRU_EXPLAIN_NS, "serverInfo");
        out.writeAttribute("protocol", "SRU");
        switch (config.getDefaultVersion()) {
        case VERSION_1_1:
            out.writeAttribute("version", "1.1");
            break;
        case VERSION_1_2:
            out.writeAttribute("version", "1.2");
        } // switch
        out.writeAttribute("transport", config.getTransports());
        out.writeStartElement(SRU_EXPLAIN_NS, "host");
        out.writeCharacters(config.getHost());
        out.writeEndElement(); // "host" element
        out.writeStartElement(SRU_EXPLAIN_NS, "port");
        out.writeCharacters(Integer.toString(config.getPort()));
        out.writeEndElement(); // "port" element
        out.writeStartElement(SRU_EXPLAIN_NS, "database");
        out.writeCharacters(config.getDatabase());
        out.writeEndElement(); // "database" element
        out.writeEndElement(); // "serverInfo" element

        // explain/databaseInfo
        final DatabaseInfo dbinfo = config.getDatabaseInfo();
        if (dbinfo != null) {
            out.writeStartElement(SRU_EXPLAIN_NS, "databaseInfo");
            writeLocalizedStrings(out, "title", dbinfo.getTitle());
            writeLocalizedStrings(out, "description", dbinfo.getDescription());
            writeLocalizedStrings(out, "author", dbinfo.getAuthor());
            writeLocalizedStrings(out, "extent", dbinfo.getExtend());
            writeLocalizedStrings(out, "history", dbinfo.getHistory());
            writeLocalizedStrings(out, "langUsage", dbinfo.getLangUsage());
            writeLocalizedStrings(out, "restrictions", dbinfo.getRestrictions());
            writeLocalizedStrings(out, "subjects", dbinfo.getSubjects());
            writeLocalizedStrings(out, "links", dbinfo.getLinks());
            writeLocalizedStrings(out, "implementation",
                    dbinfo.getImplementation());
            out.writeEndElement(); // "databaseInfo" element
        }

        // explain/indexInfo
        final IndexInfo indexInfo = config.getIndexInfo();
        if (indexInfo != null) {
            out.writeStartElement(SRU_EXPLAIN_NS, "indexInfo");

            List<IndexInfo.Set> sets = indexInfo.getSets();
            if (sets != null) {
                for (IndexInfo.Set set : sets) {
                    out.writeStartElement(SRU_EXPLAIN_NS, "set");
                    out.writeAttribute("identifier", set.getIdentifier());
                    out.writeAttribute("name", set.getName());
                    writeLocalizedStrings(out, "title", set.getTitle());
                    out.writeEndElement(); // "set" element
                }
            }

            List<IndexInfo.Index> indexes = indexInfo.getIndexes();
            if (indexes != null) {
                for (IndexInfo.Index index : indexes) {
                    out.writeStartElement(SRU_EXPLAIN_NS, "index");
                    out.writeAttribute("search",
                            index.canSearch() ? "true" : "false");
                    out.writeAttribute("scan",
                            index.canScan() ? "true" : "false");
                    out.writeAttribute("sort",
                            index.canSort() ? "true" : "false");
                    writeLocalizedStrings(out, "title", index.getTitle());
                    List<IndexInfo.Index.Map> maps = index.getMaps();
                    if (maps != null) {
                        for (IndexInfo.Index.Map map : maps) {
                            out.writeStartElement(SRU_EXPLAIN_NS, "map");
                            if (map.isPrimary()) {
                                out.writeAttribute("primary", "true");
                            }
                            out.writeStartElement(SRU_EXPLAIN_NS, "name");
                            out.writeAttribute("set", map.getSet());
                            out.writeCharacters(map.getName());
                            out.writeEndElement(); // "name" element
                            out.writeEndElement(); // "map" element
                        }
                    }
                    out.writeEndElement(); // "index" element
                }
            }
            out.writeEndElement(); // "indexInfo" element
        }

        // explain/schemaInfo
        final List<SchemaInfo> schemaInfo =
                config.getSchemaInfo();
        if (schemaInfo != null) {
            out.writeStartElement(SRU_EXPLAIN_NS, "schemaInfo");
            for (SRUServerConfig.SchemaInfo schema : schemaInfo) {
                out.writeStartElement(SRU_EXPLAIN_NS, "schema");
                out.writeAttribute("identifier", schema.getIdentifier());
                out.writeAttribute("name", schema.getName());
                /*
                 * default is "false", so only add attribute if set to true
                 */
                if (schema.getSort() ) {
                    out.writeAttribute("sort", "true");
                }
                /*
                 * default is "true", so only add attribute if set to false
                 */
                if (!schema.getRetrieve()) {
                    out.writeAttribute("retrieve", "false");
                }
                writeLocalizedStrings(out, "title", schema.getTitle());
                out.writeEndElement(); // "schema" element
            }
            out.writeEndElement(); // "schemaInfo" element
        }

        // explain/configInfo
        out.writeStartElement(SRU_EXPLAIN_NS, "configInfo");
        // numberOfRecords (default)
        out.writeStartElement(SRU_EXPLAIN_NS, "default");
        out.writeAttribute("type", "numberOfRecords");
        out.writeCharacters(Integer.toString(config.getNumberOfRecords()));
        out.writeEndElement(); // default" element

        // maximumRecords (setting)
        out.writeStartElement(SRU_EXPLAIN_NS, "setting");
        out.writeAttribute("type", "maximumRecords");
        out.writeCharacters(Integer.toString(config.getMaximumRecords()));
        out.writeEndElement(); // "setting" element

        out.writeEndElement(); // "configInfo" element

        out.writeEndElement(); // "explain" element

        out.endRecord();

        out.writeEndElement(); // "recordData" element
        out.writeEndElement(); // "record" element
    }


    private void writeDiagnostic(SRUXMLStreamWriter out,
            SRUDiagnostic diagnostic, boolean writeNsDecl)
            throws XMLStreamException {
        if (writeNsDecl) {
            out.setPrefix(SRU_DIAGNOSTIC_PREFIX, SRU_DIAGNOSIC_NS);
        }
        out.writeStartElement(SRU_DIAGNOSIC_NS, "diagnostic");
        if (writeNsDecl) {
            out.writeNamespace(SRU_DIAGNOSTIC_PREFIX, SRU_DIAGNOSIC_NS);
        }
        out.writeStartElement(SRU_DIAGNOSIC_NS, "uri");
        out.writeCharacters(SRUConstants.SRU_DIAGNOSTIC_URI_PREFIX);
        out.writeCharacters(Integer.toString(diagnostic.getCode()));
        out.writeEndElement(); // "uri" element
        if (diagnostic.getDetails() != null) {
            out.writeStartElement(SRU_DIAGNOSIC_NS, "details");
            out.writeCharacters(diagnostic.getDetails());
            out.writeEndElement(); // "details" element
        }
        if (diagnostic.getMessage() != null) {
            out.writeStartElement(SRU_DIAGNOSIC_NS, "message");
            out.writeCharacters(diagnostic.getMessage());
            out.writeEndElement(); // "message" element
        }
        out.writeEndElement(); // "diagnostic" element
    }


    private void writeEchoedExplainRequest(SRUXMLStreamWriter out,
            SRURequestImpl request) throws XMLStreamException,
            SRUException {
        // echoedSearchRetrieveRequest
        out.writeStartElement(SRU_NS, "echoedExplainRequest");

        // echoedExplainRequest/version
        if (request.getRawVersion() != null) {
            writeVersion(out, request.getRawVersion());
        }

        // echoedExplainRequest/recordPacking
        if (request.getRawRecordPacking() != null) {
            writeRecordPacking(out, request.getRawRecordPacking());
        }

        // echoedExplainRequest/stylesheet
        if (request.getStylesheet() != null) {
            out.writeStartElement(SRU_NS, "stylesheet");
            out.writeCharacters(request.getStylesheet());
            out.writeEndElement(); // "stylesheet" element
        }

        // echoedExplainRequest/baseUrl (SRU 1.2 only)
        if (request.isVersion(SRUVersion.VERSION_1_2)) {
            writeBaseUrl(out, request);
        }

        out.writeEndElement(); // "echoedExplainRequest" element
    }


    private void writeEchoedScanRequest(SRUXMLStreamWriter out,
            SRURequestImpl request, CQLNode cql) throws XMLStreamException,
            SRUException {
        // echoedScanRequest
        out.writeStartElement(SRU_NS, "echoedScanRequest");

        // echoedScanRequest/version
        if (request.getRawVersion() != null) {
            writeVersion(out, request.getRawVersion());
        }

        // echoedScanRequest/scanClause
        out.writeStartElement(SRU_NS, "scanClause");
        out.writeCharacters(request.getRawScanClause());
        out.writeEndElement(); // "query"

        // echoedScanRequest/xScanClause
        out.setDefaultNamespace(SRU_XCQL_NS);
        out.writeStartElement(SRU_NS, "xScanClause");
        out.writeDefaultNamespace(SRU_XCQL_NS);
        out.writeXCQL(cql);
        out.writeEndElement(); // "xScanClause" element

        // echoedScanRequest/responsePosition
        if (request.getResponsePosition() != -1) {
            out.writeStartElement(SRU_NS, "responsePosition");
            out.writeCharacters(
                    Integer.toString(request.getResponsePosition()));
            out.writeEndElement(); // "responsePosition" element
        }

        // echoedScanRequest/maximumTerms
        if (request.getMaximumTerms() != -1) {
            out.writeStartElement(SRU_NS, "maximumTerms");
            out.writeCharacters(Integer.toString(request.getMaximumTerms()));
            out.writeEndElement(); // "maximumTerms" element
        }

        // echoedScanRequest/stylesheet
        if (request.getStylesheet() != null) {
            out.writeStartElement(SRU_NS, "stylesheet");
            out.writeCharacters(request.getStylesheet());
            out.writeEndElement(); // "stylesheet" element
        }

        // echoedScanRequest/baseUrl (SRU 1.2 only)
        if (request.isVersion(SRUVersion.VERSION_1_2)) {
            writeBaseUrl(out, request);
        }

        out.writeEndElement(); // "echoedScanRequest" element
    }


    private void writeEchoedSearchRetrieveRequest(SRUXMLStreamWriter out,
            SRURequestImpl request, CQLNode cql) throws XMLStreamException,
            SRUException {
        // echoedSearchRetrieveRequest
        out.writeStartElement(SRU_NS, "echoedSearchRetrieveRequest");

        // echoedSearchRetrieveRequest/version
        if (request.getRawVersion() != null) {
            writeVersion(out, request.getRawVersion());
        }

        // echoedSearchRetrieveRequest/query
        out.writeStartElement(SRU_NS, "query");
        out.writeCharacters(request.getRawQuery());
        out.writeEndElement(); // "query"

        // echoedSearchRetrieveRequest/xQuery
        out.setDefaultNamespace(SRU_XCQL_NS);
        out.writeStartElement(SRU_NS, "xQuery");
        out.writeDefaultNamespace(SRU_XCQL_NS);
        out.writeXCQL(cql);
        out.writeEndElement(); // "xQuery" element

        // echoedSearchRetrieveRequest/startRecord
        if (request.getStartRecord() > 0) {
            out.writeStartElement(SRU_NS, "startRecord");
            out.writeCharacters(Integer.toString(request.getStartRecord()));
            out.writeEndElement(); // "startRecord" element
        }

        // echoedSearchRetrieveRequest/maximumRecords
        if (request.getRawMaximumRecords() > 0) {
            out.writeStartElement(SRU_NS, "maximumRecords");
            out.writeCharacters(
                    Integer.toString(request.getRawMaximumRecords()));
            out.writeEndElement(); // "startRecord" element
        }

        // echoedSearchRetrieveRequest/recordPacking
        if (request.getRawRecordPacking() != null) {
            writeRecordPacking(out, request.getRawRecordPacking());
        }

        // echoedSearchRetrieveRequest/recordSchema
        if (request.getRecordSchemaName() != null) {
            out.writeStartElement(SRU_NS, "recordSchema");
            out.writeCharacters(request.getRecordSchemaName());
            out.writeEndElement(); // "recordSchema" element
        }

        // echoedSearchRetrieveRequest/recordXPath (1.1)
        if (request.isVersion(SRUVersion.VERSION_1_1) &&
                (request.getRecordXPath() != null)) {
            out.writeStartElement(SRU_NS, "recordXPath");
            out.writeCharacters(request.getRecordXPath());
            out.writeEndElement(); // "recordXPath" element
        }

        // echoedSearchRetrieveRequest/resultSetTTL
        if (request.getResultSetTTL() > 0) {
            out.writeStartElement(SRU_NS, "resultSetTTL");
            out.writeCharacters(Long.toString(request.getResultSetTTL()));
            out.writeEndElement(); // "resultSetTTL" element
        }

        // echoedSearchRetrieveRequest/sortKeys
        if (request.isVersion(SRUVersion.VERSION_1_1) &&
                (request.getSortKeys() != null)) {
            out.writeStartElement(SRU_NS, "sortKeys");
            out.writeCharacters(request.getSortKeys());
            out.writeEndElement(); // "sortKeys" element
        }

        // echoedSearchRetrieveRequest/xsortKeys

        // echoedSearchRetrieveRequest/stylesheet
        if (request.getStylesheet() != null) {
            out.writeStartElement(SRU_NS, "stylesheet");
            out.writeCharacters(request.getStylesheet());
            out.writeEndElement(); // "stylesheet" element
        }

        // echoedSearchRetrieveRequest/baseUrl (SRU 1.2 only)
        if (request.isVersion(SRUVersion.VERSION_1_2)) {
            writeBaseUrl(out, request);
        }

        out.writeEndElement(); // "echoedSearchRetrieveRequest" element
    }


    private void writeVersion(SRUXMLStreamWriter out, SRUVersion version)
            throws XMLStreamException {
        out.writeStartElement(SRU_NS, "version");
        switch (version) {
        case VERSION_1_1:
            out.writeCharacters("1.1");
            break;
        case VERSION_1_2:
            out.writeCharacters("1.2");
            break;
        } // switch
        out.writeEndElement(); // "version" element
    }


    private void writeRecordPacking(SRUXMLStreamWriter out,
            SRURecordPacking recordPacking) throws XMLStreamException {
        out.writeStartElement(SRU_NS, "recordPacking");
        switch (recordPacking) {
        case XML:
            out.writeCharacters("xml");
            break;
        case STRING:
            out.writeCharacters("string");
            break;
        } // switch
        out.writeEndElement(); // "recordPacking" element
    }


    private void writeBaseUrl(SRUXMLStreamWriter out,
            SRURequest request) throws XMLStreamException {
        out.writeStartElement(SRU_NS, "baseUrl");
        out.writeCharacters(request.getProtocolScheme());
        out.writeCharacters(config.getBaseUrl());
        out.writeEndElement(); // "baseUrl" element
    }


    private void writeLocalizedStrings(XMLStreamWriter writer, String name,
            List<LocalizedString> list) throws XMLStreamException {
        if ((list != null) && !list.isEmpty()) {
            for (LocalizedString item : list) {
                writer.writeStartElement(SRU_EXPLAIN_NS, name);
                if (item.getLang() != null) {
                    writer.writeAttribute("lang", item.getLang());
                }
                if (item.isPrimary()) {
                    writer.writeAttribute("primary", "true");
                }
                writer.writeCharacters(item.getValue());
                writer.writeEndElement();
            }
        }
    }


    private SRUXMLStreamWriter createXMLStreamWriter(OutputStream out,
            SRURecordPacking recordPacking, int indent) throws SRUException {
        try {
            return new SRUXMLStreamWriter(out, writerFactory, recordPacking,
                    indent);
        } catch (Exception e) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    "Error creating output stream.", e);

        }
    }

} // class SRUService
