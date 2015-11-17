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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * SRU server configuration.
 *
 * <p>
 * Example:
 * </p>
 * <pre>
 * URL url = MySRUServlet.class.getClassLoader()
 *               .getResource("META-INF/sru-server-config.xml");
 * if (url == null) {
 *     throw new ServletException("not found, url == null");
 * }
 *
 * // other runtime configuration, usually obtained from Servlet context
 * HashMap&lt;String, String&gt; params = new HashMap&lt;String, String&gt;();
 * params.put(SRUServerConfig.SRU_TRANSPORT, "http");
 * params.put(SRUServerConfig.SRU_HOST, "127.0.0.1");
 * params.put(SRUServerConfig.SRU_PORT, "80");
 * params.put(SRUServerConfig.SRU_DATABASE, "sru-server");
 *
 * SRUServerConfig config = SRUServerConfig.parse(params, url);
 * </pre>
 *
 * <p>
 * The XML configuration file must validate against the "sru-server-config.xsd"
 * W3C schema bundled with the package and need to have the
 * <code>http://www.clarin.eu/sru-server/1.0/</code> XML namespace.
 * </p>
 */
public final class SRUServerConfig {
    /**
     * Parameter constant for setting the minimum supported SRU version for this
     * SRU server. Must be smaller or equal to {@link #SRU_SUPPORTED_VERSION_MAX}.
     * <p>
     * Valid values: "<code>1.1</code>", "<code>1.2</code>" or "
     * <code>2.0</code>" (without quotation marks)
     * </p>
     */
    public static final String SRU_SUPPORTED_VERSION_MIN =
            "eu.clarin.sru.server.sruSupportedVersionMin";
    /**
     * Parameter constant for setting the maximum supported SRU version for this
     * SRU server. Must be larger or equal to {@link #SRU_SUPPORTED_VERSION_MIN}.
     * <p>
     * Valid values: "<code>1.1</code>", "<code>1.2</code>" or "
     * <code>2.0</code>" (without quotation marks)
     * </p>
     */
    public static final String SRU_SUPPORTED_VERSION_MAX =
            "eu.clarin.sru.server.sruSupportedVersionMax";
    /**
     * Parameter constant for setting the default SRU version for this SRU
     * server, e.g. for an <em>Explain</em> request without explicit version.
     * Must not me less than {@link #SRU_SUPPORTED_VERSION_MIN} or larger than
     * {@link #SRU_SUPPORTED_VERSION_MAX}. Defaults to
     * {@link #SRU_SUPPORTED_VERSION_MAX}.
     * <p>
     * Valid values: "<code>1.1</code>", "<code>1.2</code>" or "
     * <code>2.0</code>" (without quotation marks)
     * </p>
     */
    public static final String SRU_SUPPORTED_VERSION_DEFAULT =
            "eu.clarin.sru.server.sruSupportedVersionDefault";
    /**
     * Parameter constant for setting the namespace URIs for SRU 1.1 and SRU
     * 1.2.
     * <p>
     * Valid values: "<code>loc"</code> for Library Of Congress URI or "
     * <code>oasis</code> for OASIS URIs (without quotation marks).
     * </p>
     */
    public static final String SRU_LEGACY_NAMESPACE_MODE =
            "eu.clarin.sru.server.legacyNamespaceMode";
    /**
     * Parameter constant for configuring the transports for this SRU server.
     * <p>
     * Valid values: "<code>http</code>", "<code>https</code>" or "
     * <code>http https</code>" (without quotation marks) <br>
     * <p>
     * Used as part of the <em>Explain</em> response.
     * </p>
     */
    public static final String SRU_TRANSPORT =
            "eu.clarin.sru.server.transport";
    /**
     * Parameter constant for configuring the host of this SRU server.
     * <p>
     * Valid values: any fully qualified hostname, e.g.
     * <code>sru.example.org</code> <br>
     * Used as part of the <em>Explain</em> response.
     * </p>
     */
    public static final String SRU_HOST =
            "eu.clarin.sru.server.host";
    /**
     * Parameter constant for configuring the port number of this SRU server.
     * <p>
     * Valid values: number between 1 and 65535 (typically 80 or 8080) <br>
     * Used as part of the <em>Explain</em> response.
     * </p>
     */
    public static final String SRU_PORT =
            "eu.clarin.sru.server.port";
    /**
     * Parameter constant for configuring the database of this SRU server. This
     * is usually the path component of the SRU servers URI.
     * <p>
     * Valid values: typically the path component if the SRU server URI. <br>
     * Used as part of the <em>Explain</em> response.
     * </p>
     */
    public static final String SRU_DATABASE =
            "eu.clarin.sru.server.database";
    /**
     * Parameter constant for configuring the <em>default</em> number of records
     * the SRU server will provide in the response to a <em>searchRetrieve</em>
     * request if the client does not provide this value.
     * <p>
     * Valid values: a integer greater than 0 (default value is 100)
     * </p>
     */
    public static final String SRU_NUMBER_OF_RECORDS =
            "eu.clarin.sru.server.numberOfRecords";
    /**
     * Parameter constant for configuring the <em>maximum</em> number of records
     * the SRU server will support in the response to a <em>searchRetrieve</em>
     * request. If a client requests more records, the number will be limited to
     * this value.
     * <p>
     * Valid values: a integer greater than 0 (default value is 250)
     * </p>
     */
    public static final String SRU_MAXIMUM_RECORDS =
            "eu.clarin.sru.server.maximumRecords";
    /**
     * Parameter constant for configuring the <em>default</em> number of terms
     * the SRU server will provide in the response to a <em>scan</em> request if
     * the client does not provide this value.
     * <p>
     * Valid values: a integer greater than 0 (default value is 250)
     * </p>
     */
    public static final String SRU_NUMBER_OF_TERMS =
            "eu.clarin.sru.server.numberOfTerms";
    /**
     * Parameter constant for configuring the <em>maximum</em> number of terms
     * the SRU server will support in the response to a <em>scan</em> request.
     * If a client requests more records, the number will be limited to this
     * value.
     * <p>
     * Valid values: a integer greater than 0 (default value is 500)
     * </p>
     */
    public static final String SRU_MAXIMUM_TERMS =
            "eu.clarin.sru.server.maximumTerms";
    /**
     * Parameter constant for configuring, if the SRU server will echo the
     * request.
     * <p>
     * Valid values: <code>true</code> or <code>false</code>
     * </p>
     */
    public static final String SRU_ECHO_REQUESTS =
            "eu.clarin.sru.server.echoRequests";
    /**
     * Parameter constant for configuring, if the SRU server pretty-print the
     * XML response. Setting this parameter can be useful for manual debugging
     * of the XML response, however it is <em>not recommended</em> for
     * production setups.
     * <p>
     * Valid values: any integer greater or equal to <code>-1</code> (default)
     * and less or equal to <code>8</code>
     * </p>
     */
    public static final String SRU_INDENT_RESPONSE =
            "eu.clarin.sru.server.indentResponse";
    /**
     * Parameter constant for configuring, if the SRU server will allow the
     * client to override the maximum number of records the server supports.
     * This parameter is solely intended for debugging and setting it to
     * <code>true</code> is <em>strongly</em> discouraged for production setups.
     * <p>
     * Valid values: <code>true</code> or <code>false</code> (default)
     * </p>
     */
    public static final String SRU_ALLOW_OVERRIDE_MAXIMUM_RECORDS =
            "eu.clarin.sru.server.allowOverrideMaximumRecords";
    /**
     * Parameter constant for configuring, if the SRU server will allow the
     * client to override the maximum number of terms the server supports. This
     * parameter is solely intended for debugging and setting it to
     * <code>true</code> it is <em>strongly</em> discouraged for production
     * setups.
     * <p>
     * Valid values: <code>true</code> or <code>false</code> (default)
     * </p>
     */
    public static final String SRU_ALLOW_OVERRIDE_MAXIMUM_TERMS =
            "eu.clarin.sru.server.allowOverrideMaximumTerms";
    /**
     * Parameter constant for configuring, if the SRU server will allow the
     * client to override the pretty-printing setting of the server. This
     * parameter is solely intended for debugging and setting it to
     * <code>true</code> it is <em>strongly</em> discouraged for production
     * setups.
     * <p>
     * Valid values: <code>true</code> or <code>false</code> (default)
     * </p>
     */
    public static final String SRU_ALLOW_OVERRIDE_INDENT_RESPONSE =
            "eu.clarin.sru.server.allowOverrideIndentResponse";
    /**
     * Parameter constant for configuring the size of response buffer. The
     * Servlet will buffer up to this amount of data before sending a response
     * to the client. This value specifies the size of the buffer in bytes.
     * <p>
     * Valid values: any positive integer (default 65536)
     * </p>
     */
    public static final String SRU_RESPONSE_BUFFER_SIZE =
            "eu.clarin.sru.server.responseBufferSize";
    private static final SRUVersion DEFAULT_SRU_VERSION_MIN =
            SRUVersion.VERSION_1_1;
    private static final SRUVersion DEFAULT_SRU_VERSION_MAX =
            SRUVersion.VERSION_1_2;
    private static final LegacyNamespaceMode DEFAULT_LEGACY_NAMESPACE_MODE =
            LegacyNamespaceMode.LOC;
    private static final int DEFAULT_NUMBER_OF_RECORDS    = 100;
    private static final int DEFAULT_MAXIMUM_RECORDS      = 250;
    private static final int DEFAULT_NUMBER_OF_TERMS      = 250;
    private static final int DEFAULT_MAXIMUM_TERMS        = 500;
    private static final int DEFAULT_RESPONSE_BUFFER_SIZE = 64 * 1024;
    private static final String CONFIG_FILE_NAMESPACE_URI =
            "http://www.clarin.eu/sru-server/1.0/";
    private static final String CONFIG_FILE_SCHEMA_URL =
            "META-INF/sru-server-config.xsd";
    public static enum LegacyNamespaceMode {
        LOC, OASIS
    } // enum LegacyNamespaceMode

    public static final class LocalizedString {
        private final boolean primary;
        private final String lang;
        private final String value;

        private LocalizedString(String value, String lang, boolean primary) {
            this.value   = value;
            this.lang    = lang;
            this.primary = primary;
        }

        private LocalizedString(String value, String lang) {
            this(value, lang, false);
        }

        public boolean isPrimary() {
            return primary;
        }

        public String getLang() {
            return lang;
        }

        public String getValue() {
            return value;
        }
    } // class LocalizedString

    public static final class DatabaseInfo {
        private final List<LocalizedString> title;
        private final List<LocalizedString> description;
        private final List<LocalizedString> author;
        private final List<LocalizedString> extent;
        private final List<LocalizedString> history;
        private final List<LocalizedString> langUsage;
        private final List<LocalizedString> restrictions;
        private final List<LocalizedString> subjects;
        private final List<LocalizedString> links;
        private final List<LocalizedString> implementation;

        private DatabaseInfo(List<LocalizedString> title,
                List<LocalizedString> description,
                List<LocalizedString> author, List<LocalizedString> extent,
                List<LocalizedString> history, List<LocalizedString> langUsage,
                List<LocalizedString> restrictions,
                List<LocalizedString> subjects, List<LocalizedString> links,
                List<LocalizedString> implementation) {
            if ((title != null) && !title.isEmpty()) {
                this.title = Collections.unmodifiableList(title);
            } else {
                this.title = null;
            }
            if ((description != null) && !description.isEmpty()) {
                this.description = Collections.unmodifiableList(description);
            } else {
                this.description = null;
            }
            if ((author != null) && !author.isEmpty()) {
                this.author = Collections.unmodifiableList(author);
            } else {
                this.author = null;
            }
            if ((extent != null) && !extent.isEmpty()) {
                this.extent = Collections.unmodifiableList(extent);
            } else {
                this.extent = null;
            }
            if ((history != null) && !history.isEmpty()) {
                this.history = Collections.unmodifiableList(history);
            } else {
                this.history = null;
            }
            if ((langUsage != null) && !langUsage.isEmpty()) {
                this.langUsage = Collections.unmodifiableList(langUsage);
            } else {
                this.langUsage = null;
            }
            if ((restrictions != null) && !restrictions.isEmpty()) {
                this.restrictions = Collections.unmodifiableList(restrictions);
            } else {
                this.restrictions = null;
            }
            if ((subjects != null) && !subjects.isEmpty()) {
                this.subjects = Collections.unmodifiableList(subjects);
            } else {
                this.subjects = null;
            }
            if ((links != null) && !links.isEmpty()) {
                this.links = Collections.unmodifiableList(links);
            } else {
                this.links = null;
            }
            if ((implementation != null) && !implementation.isEmpty()) {
                this.implementation =
                        Collections.unmodifiableList(implementation);
            } else {
                this.implementation = null;
            }
        }

        public List<LocalizedString> getTitle() {
            return title;
        }

        public List<LocalizedString> getDescription() {
            return description;
        }

        public List<LocalizedString> getAuthor() {
            return author;
        }

        public List<LocalizedString> getExtend() {
            return extent;
        }

        public List<LocalizedString> getHistory() {
            return history;
        }

        public List<LocalizedString> getLangUsage() {
            return langUsage;
        }

        public List<LocalizedString> getRestrictions() {
            return restrictions;
        }

        public List<LocalizedString> getSubjects() {
            return subjects;
        }

        public List<LocalizedString> getLinks() {
            return links;
        }

        public List<LocalizedString> getImplementation() {
            return implementation;
        }
    } // class DatabaseInfo

    public static class SchemaInfo {
        private final String identifier;
        private final String name;
        private final String location;
        private final boolean sort;
        private final boolean retrieve;
        private final List<LocalizedString> title;

        private SchemaInfo(String identifier, String name, String location,
                boolean sort, boolean retieve, List<LocalizedString> title) {
            this.identifier = identifier;
            this.name       = name;
            this.location   = location;
            this.sort       = sort;
            this.retrieve   = retieve;
            if ((title != null) && !title.isEmpty()) {
                this.title = Collections.unmodifiableList(title);
            } else {
                this.title = null;
            }
        }


        public String getIdentifier() {
            return identifier;
        }


        public String getName() {
            return name;
        }


        public String getLocation() {
            return location;
        }


        public boolean getSort() {
            return sort;
        }


        public boolean getRetrieve() {
            return retrieve;
        }


        public List<LocalizedString> getTitle() {
            return title;
        }
    } // class SchemaInfo


    public static class IndexInfo {
        public static class Set {
            private final String identifier;
            private final String name;
            private final List<LocalizedString> title;

            private Set(String identifier, String name, List<LocalizedString> title) {
                this.identifier = identifier;
                this.name       = name;
                if ((title != null) && !title.isEmpty()) {
                    this.title = Collections.unmodifiableList(title);
                } else {
                    this.title = null;
                }
            }

            public String getIdentifier() {
                return identifier;
            }

            public String getName() {
                return name;
            }

            public List<LocalizedString> getTitle() {
                return title;
            }
        } // class IndexInfo.Set

        public static class Index {
            public static class Map {
                private final boolean primary;
                private final String set;
                private final String name;

                private Map(boolean primary, String set, String name) {
                    this.primary = primary;
                    this.set     = set;
                    this.name    = name;
                }

                public boolean isPrimary() {
                    return primary;
                }

                public String getSet() {
                    return set;
                }

                public String getName() {
                    return name;
                }
            } // class IndexInfo.Index.Map
            private final List<LocalizedString> title;
            private final boolean can_search;
            private final boolean can_scan;
            private final boolean can_sort;
            private final List<Index.Map> maps;


            public Index(List<LocalizedString> title, boolean can_search,
                    boolean can_scan, boolean can_sort, List<Map> maps) {
                if ((title != null) && !title.isEmpty()) {
                    this.title = Collections.unmodifiableList(title);
                } else {
                    this.title = null;
                }
                this.can_search = can_search;
                this.can_scan   = can_scan;
                this.can_sort   = can_sort;
                this.maps       = maps;
            }


            public List<LocalizedString> getTitle() {
                return title;
            }


            public boolean canSearch() {
                return can_search;
            }


            public boolean canScan() {
                return can_scan;
            }


            public boolean canSort() {
                return can_sort;
            }


            public List<Index.Map> getMaps() {
                return maps;
            }

        } // class Index

        private final List<IndexInfo.Set> sets;
        private final List<IndexInfo.Index> indexes;

        private IndexInfo(List<IndexInfo.Set> sets, List<IndexInfo.Index> indexes) {
            if ((sets != null) && !sets.isEmpty()) {
                this.sets = Collections.unmodifiableList(sets);
            } else {
                this.sets = null;
            }
            if ((indexes != null) && !indexes.isEmpty()) {
                this.indexes = Collections.unmodifiableList(indexes);
            } else {
                this.indexes = null;
            }
        }


        public List<IndexInfo.Set> getSets() {
            return sets;
        }


        public List<IndexInfo.Index> getIndexes() {
            return indexes;
        }
    } // IndexInfo

    private final SRUVersion minVersion;
    private final SRUVersion maxVersion;
    private final SRUVersion defaultVersion;
    private final LegacyNamespaceMode legacyNamespaceMode;
    private final String transport;
    private final String host;
    private final int port;
    private final String database;
    private final int numberOfRecords;
    private final int maximumRecords;
    private final int numberOfTerms;
    private final int maximumTerms;
    private final boolean echoRequests;
    private final int indentResponse;
    private final int responseBufferSize;
    private final boolean allowOverrideMaximumRecords;
    private final boolean allowOverrideMaximumTerms;
    private final boolean allowOverrideIndentResponse;
    private final String baseUrl;
    private final DatabaseInfo databaseInfo;
    private final IndexInfo indexInfo;
    private final List<SchemaInfo> schemaInfo;
    private final List<SRUQueryParser<?>> queryParsers;


    private SRUServerConfig(SRUVersion minVersion,
            SRUVersion maxVersion,
            SRUVersion defaultVersion,
            LegacyNamespaceMode legacyNamespaceMode,
            String transport,
            String host,
            int port,
            String database,
            int numberOfRecords,
            int maximumRecords,
            int numberOfTerms,
            int maximumTerms,
            boolean echoRequests,
            int indentResponse,
            int responseBufferSize,
            boolean allowOverrideMaximumRecords,
            boolean allowOverrideMaximumTerms,
            boolean allowOverrideIndentResponse,
            DatabaseInfo databaseinfo,
            IndexInfo indexInfo,
            List<SchemaInfo> schemaInfo,
            List<SRUQueryParser<?>> queryParsers) {
        this.minVersion                  = minVersion;
        this.maxVersion                  = maxVersion;
        this.defaultVersion              = defaultVersion;
        this.legacyNamespaceMode         = legacyNamespaceMode;
        this.transport                   = transport;
        this.host                        = host;
        this.port                        = port;
        this.database                    = database;
        this.numberOfRecords             = numberOfRecords;
        this.maximumRecords              = maximumRecords;
        this.numberOfTerms               = numberOfTerms;
        this.maximumTerms                = maximumTerms;
        this.echoRequests                = echoRequests;
        this.indentResponse              = indentResponse;
        this.responseBufferSize          = responseBufferSize;
        this.allowOverrideMaximumRecords = allowOverrideMaximumRecords;
        this.allowOverrideMaximumTerms   = allowOverrideMaximumTerms;
        this.allowOverrideIndentResponse = allowOverrideIndentResponse;
        this.databaseInfo                = databaseinfo;
        this.indexInfo                   = indexInfo;
        if ((schemaInfo != null) && !schemaInfo.isEmpty()) {
            this.schemaInfo = Collections.unmodifiableList(schemaInfo);
        } else {
            this.schemaInfo = null;
        }
        this.queryParsers = Collections.unmodifiableList(queryParsers);

        // build baseUrl
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        if (port != 80) {
            sb.append(":").append(port);
        }
        sb.append("/").append(database);
        this.baseUrl = sb.toString();
    }


    public SRUVersion getMinVersion() {
        return minVersion;
    }


    public SRUVersion getMaxVersion() {
        return maxVersion;
    }


    public SRUVersion getDefaultVersion() {
        return defaultVersion;
    }


    public LegacyNamespaceMode getLegacyNamespaceMode() {
        return legacyNamespaceMode;
    }


    public SRURecordXmlEscaping getDefaultRecordXmlEscaping() {
        return SRURecordXmlEscaping.XML;
    }


    public SRURecordPacking getDefaultRecordPacking() {
        return SRURecordPacking.PACKED;
    }


    public boolean getEchoRequests() {
        return echoRequests;
    }


    public String getTransports() {
        return transport;
    }


    public String getHost() {
        return host;
    }


    public int getPort() {
        return port;
    }


    public String getDatabase() {
        return database;
    }


    public String getBaseUrl() {
        return baseUrl;
    }


    public int getNumberOfRecords() {
        return numberOfRecords;
    }


    public int getMaximumRecords() {
        return maximumRecords;
    }


    public int getNumberOfTerms() {
        return numberOfTerms;
    }


    public int getMaximumTerms() {
        return maximumTerms;
    }


    public int getIndentResponse() {
        return indentResponse;
    }


    public boolean allowOverrideMaximumRecords() {
        return allowOverrideMaximumRecords;
    }


    public boolean allowOverrideMaximumTerms() {
        return allowOverrideMaximumTerms;
    }


    public boolean allowOverrideIndentResponse() {
        return allowOverrideIndentResponse;
    }


    public int getResponseBufferSize() {
        return responseBufferSize;
    }


    public DatabaseInfo getDatabaseInfo() {
        return databaseInfo;
    }


    public IndexInfo getIndexInfo() {
        return indexInfo;
    }


    public List<SchemaInfo> getSchemaInfo() {
        return schemaInfo;
    }


    public String getRecordSchemaIdentifier(String recordSchemaName) {
        if (recordSchemaName != null) {
            if ((schemaInfo != null) && !schemaInfo.isEmpty()) {
                for (SchemaInfo schema : schemaInfo) {
                    if (schema.getName().equals(recordSchemaName)) {
                        return schema.getIdentifier();
                    }
                }
            }
        }
        return null;
    }


    public String getRecordSchemaName(String schemaIdentifier) {
        if (schemaIdentifier != null) {
           if ((schemaInfo != null) && !schemaInfo.isEmpty()) {
               for (SchemaInfo schema : schemaInfo) {
                   if (schema.getIdentifier().equals(schemaIdentifier)) {
                       return schema.getName();
                   }
               }
           }
        }
        return null;
    }


    public SchemaInfo findSchemaInfo(String value) {
        if (value != null) {
            if ((schemaInfo != null) && !schemaInfo.isEmpty()) {
                for (SchemaInfo schema : schemaInfo) {
                    if (schema.getIdentifier().equals(value) ||
                            schema.getName().equals(value)) {
                        return schema;
                    }
                }
            }
        }
        return null;
    }


    public SRUQueryParser<?> findQueryParser(String queryType) {
        if (queryType != null) {
            for (SRUQueryParser<?> queryParser : queryParsers) {
                if (queryParser.getQueryType().equals(queryType)) {
                    return queryParser;
                }
            }
        }
        return null;
    }


    /**
     * Parse a SRU server XML configuration file and create an configuration
     * object from it.
     *
     * @param params
     *            additional settings
     * @param configFile
     *            an {@link URL} pointing to the XML configuration file
     * @return a initialized <code>SRUEndpointConfig</code> instance
     * @throws NullPointerException
     *             if <em>params</em> or <em>configFile</em> is
     *             <code>null</code>
     * @throws SRUConfigException
     *             if an error occurred
     */
    public static SRUServerConfig parse(Map<String, String> params,
            URL configFile) throws SRUConfigException {
        if (params == null) {
            throw new NullPointerException("params == null");
        }
        if (configFile == null) {
            throw new NullPointerException("in == null");
        }
        try {
            URL url = SRUServerConfig.class.getClassLoader()
                    .getResource(CONFIG_FILE_SCHEMA_URL);
            if (url == null) {
                throw new SRUConfigException("cannot open \"" +
                        CONFIG_FILE_SCHEMA_URL + "\"");
            }
            SchemaFactory sfactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sfactory.newSchema(url);
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            factory.setValidating(false);

            // parse input
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(configFile.openStream());
            input.setPublicId(CONFIG_FILE_NAMESPACE_URI);
            input.setSystemId(CONFIG_FILE_NAMESPACE_URI);
            Document doc = builder.parse(input);

            // validate
            Source source = new DOMSource(doc);
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new DefaultHandler() {
                @Override
                public void error(SAXParseException e) throws SAXException {
                    fatalError(e);
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw new SAXException(
                            "error parsing endpoint configuration file", e);
                }
            });
            validator.validate(source);


            XPathFactory xfactory = XPathFactory.newInstance();
            XPath xpath = xfactory.newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                @Override
                public Iterator<?> getPrefixes(String namespaceURI) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getNamespaceURI(String prefix) {
                    if (prefix == null) {
                        throw new NullPointerException("prefix == null");
                    }
                    if (prefix.equals("sru")) {
                        return CONFIG_FILE_NAMESPACE_URI;
                    } else if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
                        return XMLConstants.XML_NS_URI;
                    } else {
                        return XMLConstants.NULL_NS_URI;
                    }
                }
            });

            DatabaseInfo databaseInfo = buildDatabaseInfo(xpath, doc);

            IndexInfo indexInfo = buildIndexInfo(xpath, doc);

            List<SchemaInfo> schemaInfo = buildSchemaInfo(xpath, doc);

            /*
             * fetch parameters more parameters (usually passed from Servlet
             * context)
             */

            SRUVersion minVersion = parseVersionNumber(params,
                    SRU_SUPPORTED_VERSION_MIN, false, DEFAULT_SRU_VERSION_MIN);

            SRUVersion maxVersion = parseVersionNumber(params,
                    SRU_SUPPORTED_VERSION_MAX, false, DEFAULT_SRU_VERSION_MAX);
            if (maxVersion.compareTo(minVersion) < 0) {
                throw new SRUConfigException(
                        "parameter value \"" + SRU_SUPPORTED_VERSION_MAX +
                                "\" (" + maxVersion.getVersionString() +
                                ") must be equal or larger than value of parameter \"" +
                                SRU_SUPPORTED_VERSION_MIN + "\" (" +
                                minVersion.getVersionString() + ")");
            }

            SRUVersion defaultVersion = parseVersionNumber(params,
                    SRU_SUPPORTED_VERSION_DEFAULT, false, maxVersion);
            if ((defaultVersion.compareTo(minVersion) < 0) ||
                    (defaultVersion.compareTo(maxVersion) > 0)) {
                throw new SRUConfigException(
                        "parameter value \"" + SRU_SUPPORTED_VERSION_DEFAULT +
                                "\" (" + defaultVersion.getVersionString() +
                                ") must be between value of parameter \"" +
                                SRU_SUPPORTED_VERSION_MIN + "\" (" +
                                minVersion.getVersionString() + ") and \"" +
                                SRU_SUPPORTED_VERSION_MAX + "\" (" +
                                maxVersion.getVersionString() + ")");
            }

            LegacyNamespaceMode legacyNamespaceMode =
                    DEFAULT_LEGACY_NAMESPACE_MODE;
            String mode = params.get(SRU_LEGACY_NAMESPACE_MODE);
            if ((mode != null) && !mode.isEmpty()) {
                if ("loc".equals(mode)) {
                    legacyNamespaceMode = LegacyNamespaceMode.LOC;
                } else if ("oasis".equals(mode)) {
                    legacyNamespaceMode = LegacyNamespaceMode.OASIS;
                } else {
                    throw new SRUConfigException(
                            "invalid value for parameter \"" +
                                    SRU_LEGACY_NAMESPACE_MODE + "\": " + mode);
                }
            }

            String transport = params.get(SRU_TRANSPORT);
            if ((transport == null) || transport.isEmpty()) {
                throw new SRUConfigException("parameter \"" + SRU_TRANSPORT +
                        "\" is mandatory");
            } else {
                StringBuilder sb = new StringBuilder();
                StringTokenizer st = new StringTokenizer(transport);
                while (st.hasMoreTokens()) {
                    String s = st.nextToken().trim().toLowerCase();
                    if (!("http".equals(s) || "https".equals(s))) {
                        throw new SRUConfigException(
                                "unsupported transport \"" + s + "\"");
                    }
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(s);
                } // while
                transport = sb.toString();
            }

            String host = params.get(SRU_HOST);
            if ((host == null) || host.isEmpty()) {
                throw new SRUConfigException("parameter \"" + SRU_HOST +
                        "\" is mandatory");
            }

            int port = parseNumber(params, SRU_PORT, true, -1, 1, 65535);

            String database = params.get(SRU_DATABASE);
            if ((database == null) || database.isEmpty()) {
                throw new SRUConfigException("parameter \"" + SRU_DATABASE +
                        "\" is mandatory");
            }

            // cleanup: remove leading slashed
            while (database.startsWith("/")) {
                database = database.substring(1);
            }


            int numberOfRecords = parseNumber(params, SRU_NUMBER_OF_RECORDS,
                    false, DEFAULT_NUMBER_OF_RECORDS, 1, -1);

            int maximumRecords = parseNumber(params, SRU_MAXIMUM_RECORDS,
                    false, DEFAULT_MAXIMUM_RECORDS, numberOfRecords, -1);

            int numberOfTerms = parseNumber(params, SRU_NUMBER_OF_TERMS,
                    false, DEFAULT_NUMBER_OF_TERMS, 0, -1);

            int maximumTerms = parseNumber(params, SRU_MAXIMUM_TERMS, false,
                        DEFAULT_MAXIMUM_TERMS, numberOfTerms, -1);

            boolean echoRequests = parseBoolean(params, SRU_ECHO_REQUESTS,
                    false, true);

            int indentResponse = parseNumber(params, SRU_INDENT_RESPONSE,
                    false, -1, -1, 8);

            boolean allowOverrideMaximumRecords = parseBoolean(params,
                    SRU_ALLOW_OVERRIDE_MAXIMUM_RECORDS, false, false);

            boolean allowOverrideMaximumTerms = parseBoolean(params,
                    SRU_ALLOW_OVERRIDE_MAXIMUM_TERMS, false, false);

            boolean allowOverrideIndentResponse = parseBoolean(params,
                    SRU_ALLOW_OVERRIDE_INDENT_RESPONSE, false, false);

            int responseBufferSize = parseNumber(params,
                    SRU_RESPONSE_BUFFER_SIZE, false,
                    DEFAULT_RESPONSE_BUFFER_SIZE, 0, -1);

            /*
             * FIXME: add interface to register additional query parsers
             */
            List<SRUQueryParser<?>> queryParsers =
                    new ArrayList<SRUQueryParser<?>>();
            queryParsers.add(new CQLQueryParser());

            return new SRUServerConfig(minVersion,
                    maxVersion,
                    defaultVersion,
                    legacyNamespaceMode,
                    transport,
                    host,
                    port,
                    database,
                    numberOfRecords,
                    maximumRecords,
                    numberOfTerms,
                    maximumTerms,
                    echoRequests,
                    indentResponse,
                    responseBufferSize,
                    allowOverrideMaximumRecords,
                    allowOverrideMaximumTerms,
                    allowOverrideIndentResponse,
                    databaseInfo,
                    indexInfo,
                    schemaInfo,
                    queryParsers);
        } catch (IOException e) {
            throw new SRUConfigException("error reading configuration file", e);
        } catch (XPathException e) {
            throw new SRUConfigException("error parsing configuration file", e);
        } catch (ParserConfigurationException e) {
            throw new SRUConfigException("error parsing configuration file", e);
        } catch (SAXException e) {
            throw new SRUConfigException("error parsing configuration file", e);
        }
    }


    private static SRUVersion parseVersionNumber(Map<String, String> params,
            String name, boolean mandatory, SRUVersion defaultValue)
                    throws SRUConfigException {
        String value = params.get(name);
        if ((value == null) || value.isEmpty()) {
            if (mandatory) {
                throw new SRUConfigException(
                        "parameter \"" + name + "\" is mandatory");
            } else {
                return defaultValue;
            }
        } else {
            if ("1.1".equals(value)) {
                return SRUVersion.VERSION_1_1;
            } else if ("1.2".equals(value)) {
                return SRUVersion.VERSION_1_2;
            } else if ("2.0".equals(value)) {
                return SRUVersion.VERSION_2_0;
            } else {
                throw new SRUConfigException("invalid value for parameter \"" +
                        name + "\": " + value);
            }
        }
    }


    private static int parseNumber(Map<String, String> params, String name,
            boolean mandatory, int defaultValue, int minValue, int maxValue)
            throws SRUConfigException {
        String value = params.get(name);
        if ((value == null) || value.isEmpty()) {
            if (mandatory) {
                throw new SRUConfigException("parameter \"" + name +
                        "\" is mandatory");
            } else {
                return defaultValue;
            }
        } else {
            try {
                int num = Integer.parseInt(value);

                // sanity checks
                if ((minValue != -1) && (maxValue != -1)) {
                    if ((num < minValue) || (num > maxValue)) {
                        throw new SRUConfigException("parameter \"" + name +
                                "\" must be between " + minValue + " and " +
                                maxValue + ": " + num);
                    }
                } else {
                    if ((minValue != -1) && (num < minValue)) {
                        throw new SRUConfigException("parameter \"" + name +
                                "\" must be larger than " + minValue + ": " +
                                num);

                    }
                    if ((maxValue != -1) && (num > maxValue)) {
                        throw new SRUConfigException("parameter \"" + name +
                                "\" must be smaller than " + maxValue + ": " +
                                num);
                    }
                }
                return num;
            } catch (NumberFormatException e) {
                throw new SRUConfigException("parameter \"" + name +
                        "\" must be nummerical and less than " +
                        Integer.MAX_VALUE + ": " + value);
            }
        }
    }


    private static boolean parseBoolean(Map<String, String> params,
            String name, boolean mandatory, boolean defaultValue)
            throws SRUConfigException {
        String value = params.get(name);
        if ((value == null) || value.isEmpty()) {
            if (mandatory) {
                throw new SRUConfigException("parameter \"" + name +
                        "\" is mandatory");
            } else {
                return defaultValue;
            }
        } else {
            return Boolean.valueOf(value);
        }
    }


    private static DatabaseInfo buildDatabaseInfo(XPath xpath, Document doc)
            throws SRUConfigException, XPathExpressionException {
        List<LocalizedString> title = buildList(xpath, doc,
                "//sru:databaseInfo/sru:title");
        List<LocalizedString> description = buildList(xpath, doc,
                "//sru:databaseInfo/sru:description");
        List<LocalizedString> author = buildList(xpath, doc,
                "//sru:databaseInfo/sru:author");
        List<LocalizedString> extent = buildList(xpath, doc,
                "//sru:databaseInfo/sru:extent");
        List<LocalizedString> history = buildList(xpath, doc,
                "//sru:databaseInfo/sru:history");
        List<LocalizedString> langUsage = buildList(xpath, doc,
                "//sru:databaseInfo/sru:langUsage");
        List<LocalizedString> restrictions = buildList(xpath, doc,
                "//sru:databaseInfo/sru:restrictions");
        List<LocalizedString> subjects = buildList(xpath, doc,
                "//sru:databaseInfo/sru:subjects");
        List<LocalizedString> links = buildList(xpath, doc,
                "//sru:databaseInfo/sru:links");
        List<LocalizedString> implementation = buildList(xpath, doc,
                "//sru:databaseInfo/sru:implementation");
        return new DatabaseInfo(title, description, author, extent, history,
                langUsage, restrictions, subjects, links, implementation);
    }


    private static IndexInfo buildIndexInfo(XPath xpath, Document doc)
            throws SRUConfigException, XPathExpressionException {
        List<IndexInfo.Set> sets      = null;
        List<IndexInfo.Index> indexes = null;

        XPathExpression expr = xpath.compile("//sru:indexInfo/sru:set");
        NodeList result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        if (result.getLength() > 0) {
            sets = new ArrayList<IndexInfo.Set>(result.getLength());
            for (int i = 0; i < result.getLength(); i++) {
                Element e = (Element) result.item(i);
                String identifier = e.getAttribute("identifier");
                String name       = e.getAttribute("name");
                if (identifier.isEmpty()) {
                    throw new SRUConfigException("attribute 'identifier' may "+
                            "on element '/indexInfo/set' may not be empty");
                }
                if (name.isEmpty()) {
                    throw new SRUConfigException("attribute 'name' may on " +
                            "element '/indexInfo/set' may not be empty");
                }
                List<LocalizedString> title =
                        fromNodeList(e.getElementsByTagName("title"));
                sets.add(new IndexInfo.Set(identifier, name, title));
            }
        } // sets

        expr = xpath.compile("//sru:indexInfo/sru:index");
        result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        if (result.getLength() > 0) {
            indexes = new ArrayList<IndexInfo.Index>(result.getLength());
            for (int i = 0; i < result.getLength(); i++) {
                Element e = (Element) result.item(i);
                List<LocalizedString> title =
                        fromNodeList(e.getElementsByTagName("title"));
                boolean can_search = getBooleanAttr(e, "search", false);
                boolean can_scan   = getBooleanAttr(e, "scan", false);
                boolean can_sort   = getBooleanAttr(e, "sort", false);
                List<IndexInfo.Index.Map> maps = null;
                NodeList result2 = e.getElementsByTagName("map");
                if ((result2 != null) && (result2.getLength() > 0)) {
                    maps = new ArrayList<IndexInfo.Index.Map>(
                            result2.getLength());
                    boolean foundPrimary = false;
                    for (int j = 0; j < result2.getLength(); j++) {
                        Element e2 = (Element) result2.item(j);
                        boolean primary = getBooleanAttr(e2, "primary", false);
                        if (primary) {
                            if (foundPrimary) {
                                throw new SRUConfigException("only one map " +
                                        "may be 'primary' in index");
                            }
                            foundPrimary = true;
                        }
                        String set  = null;
                        String name = null;
                        NodeList result3 = e2.getElementsByTagName("name");
                        if ((result3 != null) && (result3.getLength() > 0)) {
                            Element e3 = (Element) result3.item(0);
                            set  = e3.getAttribute("set");
                            name = e3.getTextContent();
                            if (set.isEmpty()) {
                                throw new SRUConfigException("attribute 'set'" +
                                        " on element '/indexInfo/index/map/" +
                                        "name' may not be empty");
                            }
                            if ((name == null) || name.isEmpty()) {
                                throw new SRUConfigException("element " +
                                        "'/indexInfo/index/map/name' may not " +
                                        "be empty");
                            }
                        }
                        maps.add(new IndexInfo.Index.Map(primary, set, name));
                    }
                }
                indexes.add(new IndexInfo.Index(title, can_search, can_scan,
                        can_sort, maps));
            } // for

            // sanity check (/index/map/name/@set exists in any set/@name)
            if (sets != null) {
                for (IndexInfo.Index index : indexes) {
                    if (index.getMaps() != null) {
                        for (IndexInfo.Index.Map maps : index.getMaps()) {
                            if (findSetByName(sets, maps.getSet()) == null) {
                                throw new SRUConfigException("/index/map/" +
                                        "name refers to nonexitsing set (" +
                                        maps.getSet() + ")");
                            }
                        }
                    }
                }
            }
        } // if
        return new IndexInfo(sets, indexes);
    }


    private static List<SchemaInfo> buildSchemaInfo(XPath xpath, Document doc)
            throws SRUConfigException, XPathExpressionException {
        List<SchemaInfo> schemaInfos = null;
        XPathExpression expr = xpath.compile("//sru:schemaInfo/sru:schema");
        NodeList result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        if (result.getLength() > 0) {
            schemaInfos = new ArrayList<SchemaInfo>(result.getLength());
            for (int i = 0; i < result.getLength(); i++) {
                Element e = (Element) result.item(i);
                String identifier = e.getAttribute("identifier");
                String name       = e.getAttribute("name");
                String location   = e.getAttribute("location");
                if ((location != null) && location.isEmpty()) {
                    location = null;
                }
                boolean sort     = getBooleanAttr(e, "sort", false);
                boolean retrieve = getBooleanAttr(e, "retrieve", true);
                List<LocalizedString> title =
                        fromNodeList(e.getElementsByTagName("title"));
                schemaInfos.add(new SchemaInfo(identifier, name, location,
                        sort, retrieve, title));
            }
        }
        return schemaInfos;
    }


    private static IndexInfo.Set findSetByName(List<IndexInfo.Set> sets,
            String name) {
        for (IndexInfo.Set set : sets) {
            if (set.getName().equals(name)) {
                return set;
            }
        }
        return null;
    }


    private static List<LocalizedString> buildList(XPath xpath, Document doc,
            String expression) throws SRUConfigException,
            XPathExpressionException {
        XPathExpression expr = xpath.compile(expression);
        NodeList result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        return fromNodeList(result);
    }


    private static List<LocalizedString> fromNodeList(NodeList nodes)
            throws SRUConfigException {
        List<LocalizedString> list = null;
        if (nodes.getLength() > 0) {
            list = new ArrayList<LocalizedString>(nodes.getLength());
            boolean foundPrimary = false;
            for (int i = 0; i < nodes.getLength(); i++) {
                Element e = (Element) nodes.item(i);
                boolean primary = getBooleanAttr(e, "primary", false);
                if (primary) {
                    if (foundPrimary) {
                        throw new SRUConfigException("list may only contain "
                                + "one element as primary");
                    }
                    foundPrimary = true;
                }
                list.add(new LocalizedString(e.getTextContent(),
                        e.getAttributeNS(XMLConstants.XML_NS_URI, "lang"),
                        primary));
            }
        }
        return list;
    }


    private static boolean getBooleanAttr(Element e, String localName,
            boolean defaultValue) {
        boolean result = defaultValue;
        Attr attr = e.getAttributeNode(localName);
        if ((attr != null) && attr.getSpecified()) {
            result = Boolean.valueOf(attr.getValue());
        }
        return result;
    }

} // class SRUEndpointConfig
