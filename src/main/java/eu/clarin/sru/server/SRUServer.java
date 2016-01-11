/**
 * This software is copyright (c) 2011-2013 by
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

import java.io.FilterOutputStream;
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
import eu.clarin.sru.server.utils.SRUServerServlet;


/**
 * SRU/CQL protocol implementation for the server-side (SRU/S). This class
 * implements SRU/CQL version 1.1 and and 1.2.
 *
 * @see SRUServerConfig
 * @see SRUSearchEngine
 * @see SRUServerServlet
 * @see <a href="http://www.loc.gov/standards/sru/">SRU/CQL protocol 1.2</a>
 */
public final class SRUServer {
    private static final String SRU_DIAGNOSTIC_RECORD_SCHEMA =
            "info:srw/schema/1/diagnostics-v1.1";
    static final String RESPONSE_ENCODING = "utf-8";
    private static final String RESPONSE_CONTENT_TYPE = "application/xml";
    private static final Logger logger =
            LoggerFactory.getLogger(SRUServer.class);
    private final SRUServerConfig config;
    private final SRUQueryParserRegistry queryParsers;
    private final SRUSearchEngine searchEngine;
    private final XMLOutputFactory writerFactory;


    /**
     * Constructor.
     *
     * @param config
     *            a {@link SRUServerConfig} object
     * @param queryParsers
     *            a {@link SRUQueryParserRegistry} object
     * @param searchEngine
     *            an object implementing the {@link SRUSearchEngine} interface
     * @throws NullPointerException
     *             if config, queryParserRegistry or searchEngine is
     *             <code>null</code>
     * @throws SRUException
     *             if an error occurred
     */
    public SRUServer(SRUServerConfig config,
            SRUQueryParserRegistry queryParsers,
            SRUSearchEngine searchEngine) throws SRUException {
        if (config == null) {
            throw new NullPointerException("config == null");
        }
        this.config = config;

        if (queryParsers == null) {
            throw new NullPointerException("queryParserRegistry == null");
        }
        this.queryParsers = queryParsers;

        if (searchEngine == null) {
            throw new NullPointerException("searchEngine == null");
        }
        this.searchEngine = searchEngine;

        this.writerFactory = XMLOutputFactory.newInstance();
    }


    /**
     * Handle a SRU request.
     *
     * @param request
     *            a HttpServletRequest request
     * @param response
     *            a HttpServletResponse request
     */
    public void handleRequest(HttpServletRequest request,
            HttpServletResponse response) {
        final SRURequestImpl req =
                new SRURequestImpl(config, queryParsers, request);
        try {
            // set response properties
            response.setContentType(RESPONSE_CONTENT_TYPE);
            response.setCharacterEncoding(RESPONSE_ENCODING);
            response.setStatus(HttpServletResponse.SC_OK);
            // make sure we can reset the stream later in case of error ...
            response.setBufferSize(config.getResponseBufferSize());
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
                                SRURecordXmlEscaping.XML, false,
                                req.getIndentResponse());
                    final SRUNamespaces ns = getNamespaces(req.getVersion());
                    writeFatalError(out, ns, req, req.getDiagnostics());
                }
            } catch (XMLStreamException e) {
                logger.error("An error occurred while serializing response", e);
                throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                        "An error occurred while serializing response.", e);
            } catch (IOException e) {
                /*
                 * Well, can't really do anything useful here ...
                 */
                logger.error("An unexpected exception occurred", e);
            }
        } catch (SRUException e) {
            if (!response.isCommitted()) {
                if (logger.isInfoEnabled()) {
                    final String message = e.getDiagnostic().getMessage();
                    if (message != null) {
                        logger.info("Sending fatal diagnostic '{}' with " +
                                "message '{}'",
                                e.getDiagnostic().getURI(),
                                message);
                    } else {
                        logger.info("Sending fatal diagnostic '{}'",
                                e.getDiagnostic().getURI());
                    }
                    logger.debug("Fatal diagnostic was caused by " +
                            "this exception", e);
                }
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
                                    SRURecordXmlEscaping.XML, false,
                                    req.getIndentResponse());
                    final SRUNamespaces ns = getNamespaces(req.getVersion());
                    writeFatalError(out, ns, req, diagnostics);
                } catch (Exception ex) {
                    logger.error("An exception occurred while in error state",
                            ex);
                }
            } else {
                /*
                 * The Servlet already flushed the output buffer, so cannot
                 * degrade gracefully anymore and, unfortunately, will produce
                 * ill-formed XML output.
                 * Increase the response buffer size, if you want to avoid
                 * this (at the cost of memory).
                 */
                logger.error("A fatal error occurred, but the response was "
                        + "already committed. Unable to recover gracefully.", e);
            }
        }
    }


    private void explain(SRURequestImpl request, HttpServletResponse response)
            throws IOException, XMLStreamException, SRUException {
        logger.info("explain");

        // commence explain ...
        final SRUExplainResult result =
                searchEngine.explain(config, request, request);

        try {
            final SRUNamespaces ns = getNamespaces(request.getVersion());

            // send results
            SRUXMLStreamWriter out =
                    createXMLStreamWriter(response.getOutputStream(),
                                          request.getRecordXmlEscaping(),
                                          true,
                                          request.getIndentResponse());

            beginResponse(out, ns, request);

            // write the explain record
            writeExplainRecord(out, ns, request);

            if (config.getEchoRequests()) {
                writeEchoedExplainRequest(out, ns, request);
            }

            // diagnostics
            writeDiagnosticList(out, ns, request.getDiagnostics());

            // extraResponseData
            if (result != null) {
                if (result.hasExtraResponseData()) {
                    out.writeStartElement(ns.getResponseNS(), "extraResponseData");
                    result.writeExtraResponseData(out);
                    out.writeEndElement(); // "extraResponseData" element
                }
            }

            endResponse(out);
        } finally {
            if (result != null) {
                result.close();
            }
        }
    }


    private void scan(SRURequestImpl request, HttpServletResponse response)
            throws IOException, XMLStreamException, SRUException {
        logger.info("scan: scanClause = \"{}\"",
                new Object[] { request.getRawScanClause() });

        // commence scan
        final SRUScanResultSet result =
                searchEngine.scan(config, request, request);
        if (result == null) {
            throw new SRUException(SRUConstants.SRU_UNSUPPORTED_OPERATION,
                    "The 'scan' operation is not supported by this endpoint.");
        }

        try {
            final SRUNamespaces ns = getNamespaces(request.getVersion());

            /*
             * FIXME: re-check, if while scan response needs to be put
             * in scan namespace for SRU 2.0!
             */
            // send results
            SRUXMLStreamWriter out =
                    createXMLStreamWriter(response.getOutputStream(),
                                          request.getRecordXmlEscaping(),
                                          true,
                                          request.getIndentResponse());

            beginResponse(out, ns, request);

            try {
                /*
                 * a scan result without a list of terms is a valid response;
                 * make sure, to produce the correct output and omit in that case
                 * the <terms> ...
                 */
                boolean wroteTerms = false;
                while (result.nextTerm()) {
                    if (!wroteTerms) {
                        final boolean needNsDecl =
                                !ns.getResponseNS().equals(ns.getScanNS());
                        if (needNsDecl) {
                            out.setPrefix(ns.getScanPrefix(), ns.getScanNS());
                        }
                        out.writeStartElement(ns.getScanNS(), "terms");
                        if (needNsDecl) {
                            out.writeNamespace(ns.getScanPrefix(),
                                    ns.getScanNS());
                        }
                        wroteTerms = true;
                    }
                    out.writeStartElement(ns.getScanNS(), "term");

                    out.writeStartElement(ns.getScanNS(), "value");
                    out.writeCharacters(result.getValue());
                    out.writeEndElement(); // "value" element

                    if (result.getNumberOfRecords() > -1) {
                        out.writeStartElement(ns.getScanNS(), "numberOfRecords");
                        out.writeCharacters(
                                Integer.toString(result.getNumberOfRecords()));
                        out.writeEndElement(); // "numberOfRecords" element
                    }

                    if (result.getDisplayTerm() != null) {
                        out.writeStartElement(ns.getScanNS(), "displayTerm");
                        out.writeCharacters(result.getDisplayTerm());
                        out.writeEndElement(); // "displayTerm" element
                    }

                    if (result.getWhereInList() != null) {
                        out.writeStartElement(ns.getScanNS(), "whereInList");
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
                        out.writeStartElement(ns.getScanNS(), "extraTermData");
                        result.writeExtraTermData(out);
                        out.writeEndElement(); // "extraTermData" element
                    }

                    out.writeEndElement(); // "term" element
                } // while
                if (wroteTerms) {
                    out.writeEndElement(); // "terms" element
                }
            } catch (NoSuchElementException e) {
                throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                        "An internal error occurred while "
                                + "serializing scan results.");
            }

            // echoedScanRequest
            if (config.getEchoRequests()) {
                writeEchoedScanRequest(out, ns, request, request.getScanClause());
            }

            // diagnostics
            writeDiagnosticList(out, ns, request.getDiagnostics());

            // extraResponseData
            if (result.hasExtraResponseData()) {
                out.writeStartElement(ns.getResponseNS(), "extraResponseData");
                result.writeExtraResponseData(out);
                out.writeEndElement(); // "extraResponseData" element
            }

            endResponse(out);
        } finally {
            result.close();
        }
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
        final SRUSearchResultSet result =
                searchEngine.search(config, request, request);
        if (result == null) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    "SRUSearchEngine implementation returned invalid result (null).");
        }


        // check, of startRecord position is greater than total record set
        if ((result.getTotalRecordCount() >= 0) &&
            (request.getStartRecord() > 1) &&
            (request.getStartRecord() > result.getTotalRecordCount())) {
            throw new SRUException(
                    SRUConstants.SRU_FIRST_RECORD_POSITION_OUT_OF_RANGE);
        }

        try {
            final SRUNamespaces ns = getNamespaces(request.getVersion());

            // send results
            SRUXMLStreamWriter out =
                    createXMLStreamWriter(response.getOutputStream(),
                                          request.getRecordXmlEscaping(),
                                          true,
                                          request.getIndentResponse());

            beginResponse(out, ns, request);

            // numberOfRecords
            out.writeStartElement(ns.getResponseNS(), "numberOfRecords");
            out.writeCharacters(
                    Integer.toString(result.getTotalRecordCount()));
            out.writeEndElement(); // "numberOfRecords" element

            // resultSetId
            if (result.getResultSetId() != null) {
                out.writeStartElement(ns.getResponseNS(), "resultSetId");
                out.writeCharacters(result.getResultSetId());
                out.writeEndElement(); // "resultSetId" element
            }

            // resultSetIdleTime (SRU 1.1 and SRU 1.2)
            if (!request.isVersion(SRUVersion.VERSION_2_0) &&
                    (result.getResultSetTTL() >= 0)) {
                out.writeStartElement(ns.getResponseNS(), "resultSetIdleTime");
                out.writeCharacters(Integer.toString(result
                        .getResultSetTTL()));
                out.writeEndElement(); // "resultSetIdleTime" element
            }

            int position = (request.getStartRecord() > 0)
                    ? request.getStartRecord() : 1;
            if (result.getRecordCount() > 0) {
                final int maxPositionOffset =
                        (request.getMaximumRecords() != -1)
                        ? (position + request.getMaximumRecords() - 1)
                        : -1;
                try {
                    out.writeStartElement(ns.getResponseNS(), "records");
                    while (result.nextRecord()) {
                        /*
                         * Sanity check: do not return more then the maximum
                         * requested records. If the search engine
                         * implementation does not honor limit truncate the
                         * result set.
                         */
                        if ((maxPositionOffset != -1) &&
                                (position > maxPositionOffset)) {
                            logger.error("SRUSearchEngine implementation did " +
                                    "not honor limit for the amount of " +
                                    "requsted records. Result set truncated!");
                            break;
                        }

                        out.writeStartElement(ns.getResponseNS(), "record");

                        /*
                         * We need to output either the record or a surrogate
                         * diagnostic. In case of the latter, we need to output
                         * the appropriate record schema ...
                         */
                        final SRUDiagnostic diagnostic =
                                result.getSurrogateDiagnostic();

                        out.writeStartElement(ns.getResponseNS(), "recordSchema");
                        if (diagnostic == null) {
                            out.writeCharacters(
                                    result.getRecordSchemaIdentifier());
                        } else {
                            out.writeCharacters(SRU_DIAGNOSTIC_RECORD_SCHEMA);
                        }
                        out.writeEndElement(); // "recordSchema" element

                        /*
                         *  recordPacking (SRU 2.0)
                         *  Only serialize, of it was in request.
                         */
                        if (request.isVersion(SRUVersion.VERSION_2_0) &&
                                (request.getRawRecordPacking() != null)) {
                            writeRecordPacking(out, ns,
                                    request.getRecordPacking());
                        }

                        /*
                         * recordXMLEscaping (SRU 2.0) or
                         *   recordPacking (SRU 1.1 and 1.2)
                         */
                        writeRecordXmlEscaping(out, ns, request);

                        /*
                         * Output either record data or surrogate diagnostic ...
                         */
                        out.writeStartElement(ns.getResponseNS(), "recordData");
                        out.startRecord();
                        if (diagnostic == null) {
                            result.writeRecord(out);
                        } else {
                            // write a surrogate diagnostic
                            writeDiagnostic(out, ns, diagnostic, true);
                        }
                        out.endRecord();
                        out.writeEndElement(); // "recordData" element

                        /*
                         * recordIdentifier is version 1.2+ only
                         */
                        if (request.isVersion(SRUVersion.VERSION_1_2,
                                SRUVersion.VERSION_2_0)) {
                            final String identifier =
                                    result.getRecordIdentifier();
                            if (identifier != null) {
                                out.writeStartElement(ns.getResponseNS(),
                                                      "recordIdentifier");
                                out.writeCharacters(identifier);
                                out.writeEndElement(); // "recordIdentifier" element
                            }
                        }

                        out.writeStartElement(ns.getResponseNS(),
                                "recordPosition");
                        out.writeCharacters(Integer.toString(position));
                        out.writeEndElement(); // "recordPosition" element

                        if (result.hasExtraRecordData()) {
                            out.writeStartElement(ns.getResponseNS(),
                                    "extraRecordData");
                            result.writeExtraRecordData(out);
                            out.writeEndElement(); // "extraRecordData"
                        }

                        out.writeEndElement(); // "record" element

                        position++;
                    } // while
                    out.writeEndElement(); // "records" element
                } catch (NoSuchElementException e) {
                    throw new SRUException(
                            SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                            "An internal error occurred while " +
                            "serializing search result set.");
                }
            }

            // nextRecordPosition
            if (position <= result.getTotalRecordCount()) {
                out.writeStartElement(ns.getResponseNS(), "nextRecordPosition");
                out.writeCharacters(Integer.toString(position));
                out.writeEndElement();
            }

            // echoedSearchRetrieveRequest
            if (config.getEchoRequests()) {
                writeEchoedSearchRetrieveRequest(out, ns, request,
                                                 request.getQuery());
            }

            // diagnostics
            writeDiagnosticList(out, ns, request.getDiagnostics());

            // extraResponseData
            if (result.hasExtraResponseData()) {
                out.writeStartElement(ns.getResponseNS(), "extraResponseData");
                result.writeExtraResponseData(out);
                out.writeEndElement(); // "extraResponseData" element
            }

            // SRU 2.0 stuff ...
            if (request.isVersion(SRUVersion.VERSION_2_0)) {
                // resultSetTTL
                if (result.getResultSetTTL() >= 0) {
                    out.writeStartElement(ns.getResponseNS(), "resultSetTTL");
                    out.writeCharacters(
                            Integer.toString(result.getResultSetTTL()));
                    out.writeEndElement(); // "resultSetTTL" element
                }

                // resultCountPrecision
                final SRUResultCountPrecision precision =
                        result.getResultCountPrecision();
                if (precision != null) {
                    out.writeStartElement(ns.getResponseNS(), "resultCountPrecision");
                    switch (precision) {
                    case EXACT:
                        out.writeCharacters("info:srw/vocabulary/resultCountPrecision/1/exact");
                        break;
                    case UNKNOWN:
                        out.writeCharacters("info:srw/vocabulary/resultCountPrecision/1/unknown");
                        break;
                    case ESTIMATE:
                        out.writeCharacters("info:srw/vocabulary/resultCountPrecision/1/estimate");
                        break;
                    case MAXIMUM:
                        out.writeCharacters("info:srw/vocabulary/resultCountPrecision/1/maximum");
                        break;
                    case MINIMUM:
                        out.writeCharacters("info:srw/vocabulary/resultCountPrecision/1/minimum");
                        break;
                    case CURRENT:
                        out.writeCharacters("info:srw/vocabulary/resultCountPrecision/1/current");
                        break;
                    } // switch
                    out.writeEndElement(); // "resultCountPrecision" element
                }

                // facetedResults
                // NOT YET SUPPORTED

                // searchResultAnalysis
                // NOT YET SUPPORTED
            }
            endResponse(out);
        } finally {
            result.close();
        }
    }


    private void beginResponse(SRUXMLStreamWriter out, SRUNamespaces ns,
            SRUOperation operation, SRUVersion version, String stylesheet)
            throws XMLStreamException {
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

        out.setPrefix(ns.getResponsePrefix(), ns.getResponseNS());
        switch (operation) {
        case EXPLAIN:
            out.writeStartElement(ns.getResponseNS(), "explainResponse");
            break;
        case SCAN:
            out.writeStartElement(ns.getResponseNS(), "scanResponse");
            break;
        case SEARCH_RETRIEVE:
            out.writeStartElement(ns.getResponseNS(), "searchRetrieveResponse");
            break;
        }
        out.writeNamespace(ns.getResponsePrefix(), ns.getResponseNS());

        // version
        writeVersion(out, ns, version);
    }


    private void beginResponse(SRUXMLStreamWriter out, SRUNamespaces ns,
            SRURequest request) throws XMLStreamException {
        beginResponse(out, ns, request.getOperation(), request.getVersion(),
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


    private void writeFatalError(SRUXMLStreamWriter out, SRUNamespaces ns,
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
        beginResponse(out, ns, operation, version, null);
        switch (operation) {
        case EXPLAIN:
            // 'explain' requires a complete explain record ...
            writeExplainRecord(out, ns, request);
            break;
        case SCAN:
            // 'scan' fortunately does not need any elements ...
            break;
        case SEARCH_RETRIEVE:
            // 'searchRetrieve' needs numberOfRecords ..
            out.writeStartElement(ns.getResponseNS(), "numberOfRecords");
            out.writeCharacters("0");
            out.writeEndElement(); // "numberOfRecords" element
            break;
        }
        writeDiagnosticList(out, ns, diagnotics);
        endResponse(out);
    }


    private void writeDiagnosticList(SRUXMLStreamWriter out, SRUNamespaces ns,
            List<SRUDiagnostic> diagnostics) throws XMLStreamException {
        if ((diagnostics != null) && !diagnostics.isEmpty()) {
            out.setPrefix(ns.getDiagnosticPrefix(), ns.getDiagnosticNS());
            out.writeStartElement(ns.getDiagnosticNS(), "diagnostics");
            out.writeNamespace(ns.getDiagnosticPrefix(), ns.getDiagnosticNS());
            for (SRUDiagnostic diagnostic : diagnostics) {
                writeDiagnostic(out, ns, diagnostic, false);
            }
            out.writeEndElement(); // "diagnostics" element
        }
    }


    private void writeExplainRecord(SRUXMLStreamWriter out, SRUNamespaces ns,
            SRURequestImpl request) throws XMLStreamException {
        out.writeStartElement(ns.getResponseNS(), "record");

        out.writeStartElement(ns.getResponseNS(), "recordSchema");
        out.writeCharacters(ns.getExplainNS());
        out.writeEndElement(); // "recordSchema" element

        /*
         *  recordPacking (SRU 2.0)
         *  Only serialize, of it was in request.
         *
         *  XXX: not sure, if this makes sense for explain
         */
        if (request.isVersion(SRUVersion.VERSION_2_0) &&
                (request.getRawRecordPacking() != null)) {
            writeRecordPacking(out, ns, request.getRecordPacking());
        }

        /*
         * recordXMLEscaping (SRU 2.0) or
         *   recordPacking (SRU 1.1 and 1.2)
         */
        writeRecordXmlEscaping(out, ns, request);

        out.writeStartElement(ns.getResponseNS(), "recordData");

        out.startRecord();

        // explain ...
        out.setPrefix(ns.getExplainPrefix(), ns.getExplainNS());
        out.writeStartElement(ns.getExplainNS(), "explain");
        out.writeNamespace(ns.getExplainPrefix(), ns.getExplainNS());

        // explain/serverInfo
        out.writeStartElement(ns.getExplainNS(), "serverInfo");
        out.writeAttribute("protocol", "SRU");
        switch (config.getDefaultVersion()) {
        case VERSION_1_1:
            out.writeAttribute("version", "1.1");
            break;
        case VERSION_1_2:
            out.writeAttribute("version", "1.2");
            break;
        case VERSION_2_0:
            out.writeAttribute("version", "2.0");
            break;
        } // switch
        out.writeAttribute("transport", config.getTransports());
        out.writeStartElement(ns.getExplainNS(), "host");
        out.writeCharacters(config.getHost());
        out.writeEndElement(); // "host" element
        out.writeStartElement(ns.getExplainNS(), "port");
        out.writeCharacters(Integer.toString(config.getPort()));
        out.writeEndElement(); // "port" element
        out.writeStartElement(ns.getExplainNS(), "database");
        out.writeCharacters(config.getDatabase());
        out.writeEndElement(); // "database" element
        out.writeEndElement(); // "serverInfo" element

        // explain/databaseInfo
        final DatabaseInfo dbinfo = config.getDatabaseInfo();
        if (dbinfo != null) {
            out.writeStartElement(ns.getExplainNS(), "databaseInfo");
            writeLocalizedStrings(out, ns, "title",
                    dbinfo.getTitle());
            writeLocalizedStrings(out, ns, "description",
                    dbinfo.getDescription());
            writeLocalizedStrings(out, ns, "author",
                    dbinfo.getAuthor());
            writeLocalizedStrings(out, ns, "extent",
                    dbinfo.getExtend());
            writeLocalizedStrings(out, ns, "history",
                    dbinfo.getHistory());
            writeLocalizedStrings(out, ns, "langUsage",
                    dbinfo.getLangUsage());
            writeLocalizedStrings(out, ns, "restrictions",
                    dbinfo.getRestrictions());
            writeLocalizedStrings(out, ns, "subjects",
                    dbinfo.getSubjects());
            writeLocalizedStrings(out, ns, "links",
                    dbinfo.getLinks());
            writeLocalizedStrings(out, ns, "implementation",
                    dbinfo.getImplementation());
            out.writeEndElement(); // "databaseInfo" element
        }

        // explain/indexInfo
        final IndexInfo indexInfo = config.getIndexInfo();
        if (indexInfo != null) {
            out.writeStartElement(ns.getExplainNS(), "indexInfo");

            List<IndexInfo.Set> sets = indexInfo.getSets();
            if (sets != null) {
                for (IndexInfo.Set set : sets) {
                    out.writeStartElement(ns.getExplainNS(), "set");
                    out.writeAttribute("identifier", set.getIdentifier());
                    out.writeAttribute("name", set.getName());
                    writeLocalizedStrings(out, ns, "title", set.getTitle());
                    out.writeEndElement(); // "set" element
                }
            }

            List<IndexInfo.Index> indexes = indexInfo.getIndexes();
            if (indexes != null) {
                for (IndexInfo.Index index : indexes) {
                    out.writeStartElement(ns.getExplainNS(), "index");
                    out.writeAttribute("search",
                            index.canSearch() ? "true" : "false");
                    out.writeAttribute("scan",
                            index.canScan() ? "true" : "false");
                    out.writeAttribute("sort",
                            index.canSort() ? "true" : "false");
                    writeLocalizedStrings(out, ns, "title", index.getTitle());
                    List<IndexInfo.Index.Map> maps = index.getMaps();
                    if (maps != null) {
                        for (IndexInfo.Index.Map map : maps) {
                            out.writeStartElement(ns.getExplainNS(), "map");
                            if (map.isPrimary()) {
                                out.writeAttribute("primary", "true");
                            }
                            out.writeStartElement(ns.getExplainNS(), "name");
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
            out.writeStartElement(ns.getExplainNS(), "schemaInfo");
            for (SRUServerConfig.SchemaInfo schema : schemaInfo) {
                out.writeStartElement(ns.getExplainNS(), "schema");
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
                writeLocalizedStrings(out, ns, "title", schema.getTitle());
                out.writeEndElement(); // "schema" element
            }
            out.writeEndElement(); // "schemaInfo" element
        }

        // explain/configInfo
        out.writeStartElement(ns.getExplainNS(), "configInfo");
        // numberOfRecords (default)
        out.writeStartElement(ns.getExplainNS(), "default");
        out.writeAttribute("type", "numberOfRecords");
        out.writeCharacters(Integer.toString(config.getNumberOfRecords()));
        out.writeEndElement(); // default" element

        // maximumRecords (setting)
        out.writeStartElement(ns.getExplainNS(), "setting");
        out.writeAttribute("type", "maximumRecords");
        out.writeCharacters(Integer.toString(config.getMaximumRecords()));
        out.writeEndElement(); // "setting" element

        out.writeEndElement(); // "configInfo" element

        out.writeEndElement(); // "explain" element

        out.endRecord();

        out.writeEndElement(); // "recordData" element
        out.writeEndElement(); // "record" element
    }


    private void writeDiagnostic(SRUXMLStreamWriter out, SRUNamespaces ns,
            SRUDiagnostic diagnostic, boolean writeNsDecl)
            throws XMLStreamException {
        if (writeNsDecl) {
            out.setPrefix(ns.getDiagnosticPrefix(), ns.getDiagnosticNS());
        }
        out.writeStartElement(ns.getDiagnosticNS(), "diagnostic");
        if (writeNsDecl) {
            out.writeNamespace(ns.getDiagnosticPrefix(), ns.getDiagnosticNS());
        }
        out.writeStartElement(ns.getDiagnosticNS(), "uri");
        out.writeCharacters(diagnostic.getURI());
        out.writeEndElement(); // "uri" element
        if (diagnostic.getDetails() != null) {
            out.writeStartElement(ns.getDiagnosticNS(), "details");
            out.writeCharacters(diagnostic.getDetails());
            out.writeEndElement(); // "details" element
        }
        if (diagnostic.getMessage() != null) {
            out.writeStartElement(ns.getDiagnosticNS(), "message");
            out.writeCharacters(diagnostic.getMessage());
            out.writeEndElement(); // "message" element
        }
        out.writeEndElement(); // "diagnostic" element
    }


    private void writeEchoedExplainRequest(SRUXMLStreamWriter out,
            SRUNamespaces ns, SRURequestImpl request)
            throws XMLStreamException, SRUException {
        // echoedSearchRetrieveRequest
        out.writeStartElement(ns.getResponseNS(), "echoedExplainRequest");

        // echoedExplainRequest/version
        if (request.getRawVersion() != null) {
            writeVersion(out, ns, request.getRawVersion());
        }

        // echoedExplainRequest/recordXmlEscpaing / recordPacking
        if (request.getRawRecordPacking() != null) {
            writeRecordXmlEscaping(out, ns, request);
        }

        // echoedExplainRequest/stylesheet
        if (request.getStylesheet() != null) {
            out.writeStartElement(ns.getResponseNS(), "stylesheet");
            out.writeCharacters(request.getStylesheet());
            out.writeEndElement(); // "stylesheet" element
        }

        out.writeEndElement(); // "echoedExplainRequest" element
    }


    private void writeEchoedScanRequest(SRUXMLStreamWriter out,
            SRUNamespaces ns, SRURequestImpl request, CQLNode cql)
            throws XMLStreamException, SRUException {
        // echoedScanRequest
        out.writeStartElement(ns.getResponseNS(), "echoedScanRequest");

        // echoedScanRequest/version
        if (request.getRawVersion() != null) {
            writeVersion(out, ns, request.getRawVersion());
        }

        // echoedScanRequest/scanClause
        out.writeStartElement(ns.getResponseNS(), "scanClause");
        out.writeCharacters(request.getRawScanClause());
        out.writeEndElement(); // "query"

        // echoedScanRequest/xScanClause
        out.setDefaultNamespace(ns.getXcqlNS());
        out.writeStartElement(ns.getResponseNS(), "xScanClause");
        out.writeDefaultNamespace(ns.getXcqlNS());
        out.writeXCQL(cql, false);
        out.writeEndElement(); // "xScanClause" element

        // echoedScanRequest/responsePosition
        if (request.getResponsePosition() != -1) {
            out.writeStartElement(ns.getResponseNS(), "responsePosition");
            out.writeCharacters(
                    Integer.toString(request.getResponsePosition()));
            out.writeEndElement(); // "responsePosition" element
        }

        // echoedScanRequest/maximumTerms
        if (request.getMaximumTerms() != -1) {
            out.writeStartElement(ns.getResponseNS(), "maximumTerms");
            out.writeCharacters(Integer.toString(request.getMaximumTerms()));
            out.writeEndElement(); // "maximumTerms" element
        }

        // echoedScanRequest/stylesheet
        if (request.getStylesheet() != null) {
            out.writeStartElement(ns.getResponseNS(), "stylesheet");
            out.writeCharacters(request.getStylesheet());
            out.writeEndElement(); // "stylesheet" element
        }

        out.writeEndElement(); // "echoedScanRequest" element
    }


    private void writeEchoedSearchRetrieveRequest(SRUXMLStreamWriter out,
            SRUNamespaces ns, SRURequestImpl request, SRUQuery<?> query)
            throws XMLStreamException, SRUException {
        // echoedSearchRetrieveRequest
        out.writeStartElement(ns.getResponseNS(),
                "echoedSearchRetrieveRequest");

        // echoedSearchRetrieveRequest/version
        if (request.getRawVersion() != null) {
            writeVersion(out, ns, request.getRawVersion());
        }

        /*
         * XXX: unclear, if <query> should only be echoed if queryType is CQL!?
         */
        if (SRUConstants.SRU_QUERY_TYPE_CQL.equals(query.getQueryType())) {
            final CQLQueryParser.CQLQuery cql = (CQLQueryParser.CQLQuery) query;
            // echoedSearchRetrieveRequest/query
            out.writeStartElement(ns.getResponseNS(), "query");
            out.writeCharacters(cql.getRawQuery());
            out.writeEndElement(); // "query"

            // echoedSearchRetrieveRequest/xQuery
            out.setDefaultNamespace(ns.getXcqlNS());
            out.writeStartElement(ns.getResponseNS(), "xQuery");
            out.writeDefaultNamespace(ns.getXcqlNS());
            out.writeXCQL(cql.getParsedQuery(), true);
            out.writeEndElement(); // "xQuery" element
        }

        // echoedSearchRetrieveRequest/startRecord
        if (request.getStartRecord() > 0) {
            out.writeStartElement(ns.getResponseNS(), "startRecord");
            out.writeCharacters(Integer.toString(request.getStartRecord()));
            out.writeEndElement(); // "startRecord" element
        }

        // echoedSearchRetrieveRequest/maximumRecords
        if (request.getRawMaximumRecords() > 0) {
            out.writeStartElement(ns.getResponseNS(), "maximumRecords");
            out.writeCharacters(
                    Integer.toString(request.getRawMaximumRecords()));
            out.writeEndElement(); // "startRecord" element
        }

        // (SRU 2.0) echoedSearchRetrieveRequest/recordPacking
        if (request.isVersion(SRUVersion.VERSION_2_0) &&
                (request.getRawRecordPacking() != null)) {
            out.writeStartElement(ns.getResponseNS(), "recordPacking");
            out.writeCharacters(request.getRawRecordPacking());
            out.writeEndElement(); // "recordPacking" element
        }

        // echoedSearchRetrieveRequest/recordXmlEscaping / recordPacking
        if (request.getRawRecordXmlEscaping() != null) {
            if (request.isVersion(SRUVersion.VERSION_2_0)) {
                out.writeStartElement(ns.getResponseNS(), "recordXMLEscaping");

            } else {
                out.writeStartElement(ns.getResponseNS(), "recordPacking");
            }
            out.writeCharacters(request.getRawRecordXmlEscaping());
            out.writeEndElement(); // "recordXmlEscaping"  / "recordPacking" element
        }

        // echoedSearchRetrieveRequest/recordSchema
        if (request.getRawRecordSchemaIdentifier() != null) {
            out.writeStartElement(ns.getResponseNS(), "recordSchema");
            out.writeCharacters(request.getRawRecordSchemaIdentifier());
            out.writeEndElement(); // "recordSchema" element
        }

        // echoedSearchRetrieveRequest/recordXPath (1.1)
        if (request.isVersion(SRUVersion.VERSION_1_1) &&
                (request.getRecordXPath() != null)) {
            out.writeStartElement(ns.getResponseNS(), "recordXPath");
            out.writeCharacters(request.getRecordXPath());
            out.writeEndElement(); // "recordXPath" element
        }

        // echoedSearchRetrieveRequest/resultSetTTL
        if (request.getResultSetTTL() > 0) {
            out.writeStartElement(ns.getResponseNS(), "resultSetTTL");
            out.writeCharacters(Long.toString(request.getResultSetTTL()));
            out.writeEndElement(); // "resultSetTTL" element
        }

        // echoedSearchRetrieveRequest/sortKeys
        if (request.isVersion(SRUVersion.VERSION_1_1) &&
                (request.getSortKeys() != null)) {
            out.writeStartElement(ns.getResponseNS(), "sortKeys");
            out.writeCharacters(request.getSortKeys());
            out.writeEndElement(); // "sortKeys" element
        }

        // echoedSearchRetrieveRequest/xsortKeys

        // echoedSearchRetrieveRequest/stylesheet
        if (request.getStylesheet() != null) {
            out.writeStartElement(ns.getResponseNS(), "stylesheet");
            out.writeCharacters(request.getStylesheet());
            out.writeEndElement(); // "stylesheet" element
        }

        // echoedSearchRetrieveRequest/renderedBy
        if (request.isVersion(SRUVersion.VERSION_2_0) && (request.getRenderBy() != null)) {
            out.writeStartElement(ns.getResponseNS(), "renderedBy");
            switch (request.getRenderBy()) {
            case SERVER:
                out.writeCharacters("server");
                break;
            case CLIENT:
                out.writeCharacters("client");
                break;
            }
            out.writeEndElement(); // "renderedBy" element
        }

        // echoedSearchRetrieveRequest/extraRequestParameter
        // FIXME: NOT YET IMPLEMENTED

        // echoedSearchRetrieveRequest/httpAccept
        if (request.isVersion(SRUVersion.VERSION_2_0) && (request.getRawHttpAccept() != null)) {
            out.writeStartElement(ns.getResponseNS(), "renderedBy");
            out.writeCharacters(request.getRawHttpAccept());
            out.writeEndElement(); // "renderedBy" element
        }

        // echoedSearchRetrieveRequest/responseType
        if (request.isVersion(SRUVersion.VERSION_2_0) && (request.getResponeType() != null)) {
            out.writeStartElement(ns.getResponseNS(), "responseType");
            out.writeCharacters(request.getResponeType());
            out.writeEndElement(); // "responseType" element
        }

        out.writeEndElement(); // "echoedSearchRetrieveRequest" element
    }


    private void writeVersion(SRUXMLStreamWriter out, SRUNamespaces ns,
            SRUVersion version) throws XMLStreamException {
        out.writeStartElement(ns.getResponseNS(), "version");
        switch (version) {
        case VERSION_1_1:
            out.writeCharacters("1.1");
            break;
        case VERSION_1_2:
            out.writeCharacters("1.2");
            break;
        case VERSION_2_0:
            out.writeCharacters("2.0");
            break;
        } // switch
        out.writeEndElement(); // "version" element
    }


    private void writeRecordXmlEscaping(SRUXMLStreamWriter out,
            SRUNamespaces ns, SRURequest request) throws XMLStreamException {
        if (request.isVersion(SRUVersion.VERSION_2_0)) {
            out.writeStartElement(ns.getResponseNS(), "recordXMLEscaping");
        } else {
            out.writeStartElement(ns.getResponseNS(), "recordPacking");
        }
        switch (request.getRecordXmlEscaping()) {
        case XML:
            out.writeCharacters("xml");
            break;
        case STRING:
            out.writeCharacters("string");
            break;
        } // switch
        out.writeEndElement(); // "recordXMLEscaping" / "recordPacking" element
    }


    private void writeRecordPacking(SRUXMLStreamWriter out, SRUNamespaces ns,
            SRURecordPacking recordPacking) throws XMLStreamException {
        out.writeStartElement(ns.getResponseNS(), "recordPacking");
        switch (recordPacking) {
        case PACKED:
            out.writeCharacters("packed");
            break;
        case UNPACKED:
            out.writeCharacters("unpacked");
            break;
        }
        out.writeEndElement(); // (SRU 2.0) "recordPacking" element
    }


    private void writeLocalizedStrings(XMLStreamWriter writer, SRUNamespaces ns,
            String name, List<LocalizedString> list) throws XMLStreamException {
        if ((list != null) && !list.isEmpty()) {
            for (LocalizedString item : list) {
                writer.writeStartElement(ns.getExplainNS(), name);
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
            SRURecordXmlEscaping recordPacking, boolean skipFlush, int indent)
            throws SRUException {
        try {
            if (skipFlush) {
                /*
                 * Add a FilterOutputStream to delay flush() as long as
                 * possible. Doing so, enabled us to send an appropriate SRU
                 * diagnostic in case an error occurs during the serialization
                 * of the response.
                 * Of course, if an error occurs when the Servlet response
                 * buffer already had been flushed, because it was to large,
                 * we cannot fail gracefully and we will produce ill-formed
                 * XML output.
                 */
                out = new FilterOutputStream(out) {
                    @Override
                    public void flush() throws IOException {
                    }


                    @Override
                    public void close() throws IOException {
                        super.flush();
                        super.close();
                    }
                };
            }
            return new SRUXMLStreamWriter(out, writerFactory,
                    recordPacking, indent);
        } catch (Exception e) {
            throw new SRUException(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    "Error creating output stream.", e);

        }
    }


    private SRUNamespaces getNamespaces(SRUVersion version) {
        if (version == null) {
            throw new NullPointerException("version == null");
        }
        switch (version) {
        case VERSION_1_1:
            /* FALL-THROUGH */
        case VERSION_1_2:
            switch (config.getLegacyNamespaceMode()) {
            case LOC:
                return NAMESPACES_LEGACY_LOC;
            case OASIS:
                return NAMESPACES_1_2_OASIS;
            default:
                // FIXME: better exception?
                throw new IllegalAccessError("invalid legacy mode: " + version);
            } // switch
        case VERSION_2_0:
            return NAMESPACES_2_0;
        default:
            // FIXME: better exception?
            throw new IllegalAccessError("invalid version: " + version);
        }
    }




    private static final SRUNamespaces NAMESPACES_LEGACY_LOC = new SRUNamespaces() {
        private static final String SRU_NS =
                "http://www.loc.gov/zing/srw/";
        private static final String SRU_PREFIX =
                "sru";
        private static final String SRU_DIAGNOSIC_NS =
                "http://www.loc.gov/zing/srw/diagnostic/";
        private static final String SRU_DIAGNOSTIC_PREFIX =
                "diag";
        private static final String SRU_EXPLAIN_NS =
                "http://explain.z3950.org/dtd/2.0/";
        private static final String SRU_EXPLAIN_PREFIX =
                "zr";
        private static final String SRU_XCQL_NS =
                "http://www.loc.gov/zing/cql/xcql/";


        @Override
        public String getResponseNS() {
            return SRU_NS;
        }


        @Override
        public String getResponsePrefix() {
            return SRU_PREFIX;
        }


        @Override
        public String getScanNS() {
            return SRU_NS;
        }


        @Override
        public String getScanPrefix() {
            return SRU_PREFIX;
        }


        @Override
        public String getDiagnosticNS() {
            return SRU_DIAGNOSIC_NS;
        }


        @Override
        public String getDiagnosticPrefix() {
            return SRU_DIAGNOSTIC_PREFIX;
        }


        @Override
        public String getExplainNS() {
            return SRU_EXPLAIN_NS;
        }


        @Override
        public String getExplainPrefix() {
            return SRU_EXPLAIN_PREFIX;
        }


        @Override
        public String getXcqlNS() {
            return SRU_XCQL_NS;
        }
    };


    private static final SRUNamespaces NAMESPACES_1_2_OASIS = new SRUNamespaces() {
        private static final String SRU_RESPONSE_NS =
                "http://docs.oasis-open.org/ns/search-ws/sruResponse";
        private static final String SRU_RESPONSE_PREFIX =
                "sruResponse";
        private static final String SRU_SCAN_NS =
                "http://docs.oasis-open.org/ns/search-ws/scan";
        private static final String SRU_SCAN_PREFIX =
                "scan";
        private static final String SRU_DIAGNOSIC_NS =
                "http://docs.oasis-open.org/ns/search-ws/diagnostic";
        private static final String SRU_DIAGNOSTIC_PREFIX =
                "diag";
        private static final String SRU_EXPLAIN_NS =
                "http://explain.z3950.org/dtd/2.0/";
        private static final String SRU_EXPLAIN_PREFIX =
                "zr";
        private static final String SRU_XCQL_NS =
                "http://docs.oasis-open.org/ns/search-ws/xcql";


        @Override
        public String getResponseNS() {
            return SRU_RESPONSE_NS;
        }


        @Override
        public String getResponsePrefix() {
            return SRU_RESPONSE_PREFIX;
        }


        @Override
        public String getScanNS() {
            return SRU_SCAN_NS;
        }


        @Override
        public String getScanPrefix() {
            return SRU_SCAN_PREFIX;
        }


        @Override
        public String getDiagnosticNS() {
            return SRU_DIAGNOSIC_NS;
        }


        @Override
        public String getDiagnosticPrefix() {
            return SRU_DIAGNOSTIC_PREFIX;
        }


        @Override
        public String getExplainNS() {
            return SRU_EXPLAIN_NS;
        }


        @Override
        public String getExplainPrefix() {
            return SRU_EXPLAIN_PREFIX;
        }


        @Override
        public String getXcqlNS() {
            return SRU_XCQL_NS;
        }
    };


    private static final SRUNamespaces NAMESPACES_2_0 = new SRUNamespaces() {
        private static final String SRU_RESPONSE_NS =
                "http://docs.oasis-open.org/ns/search-ws/sruResponse";
        private static final String SRU_RESPONSE_PREFIX =
                "sruResponse";
        private static final String SRU_SCAN_NS =
                "http://docs.oasis-open.org/ns/search-ws/scan";
        private static final String SRU_SCAN_PREFIX =
                "scan";
        private static final String SRU_DIAGNOSIC_NS =
                "http://docs.oasis-open.org/ns/search-ws/diagnostic";
        private static final String SRU_DIAGNOSTIC_PREFIX =
                "diag";
        private static final String SRU_EXPLAIN_NS =
                "http://explain.z3950.org/dtd/2.0/";
        private static final String SRU_EXPLAIN_PREFIX =
                "zr";
        private static final String SRU_XCQL_NS =
                "http://docs.oasis-open.org/ns/search-ws/xcql";


        @Override
        public String getResponseNS() {
            return SRU_RESPONSE_NS;
        }


        @Override
        public String getResponsePrefix() {
            return SRU_RESPONSE_PREFIX;
        }


        @Override
        public String getScanNS() {
            return SRU_SCAN_NS;
        }


        @Override
        public String getScanPrefix() {
            return SRU_SCAN_PREFIX;
        }


        @Override
        public String getDiagnosticNS() {
            return SRU_DIAGNOSIC_NS;
        }


        @Override
        public String getDiagnosticPrefix() {
            return SRU_DIAGNOSTIC_PREFIX;
        }


        @Override
        public String getExplainNS() {
            return SRU_EXPLAIN_NS;
        }


        @Override
        public String getExplainPrefix() {
            return SRU_EXPLAIN_PREFIX;
        }


        @Override
        public String getXcqlNS() {
            return SRU_XCQL_NS;
        }
    };

} // class SRUServer
