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

import java.io.InputStream;
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
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
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
 * Endpoint configuration. Most of the endpoint configuration is created from
 * the XML file.
 * 
 * <p>Example:</p>
 * <pre>
 * URL url = MySRUServlet.class.getClassLoader().getResource("META-INF/endpoint-config.xml");
 * if (url == null) {
 *     throw new ServletException("not found, url == null");
 * }
 * 
 * HashMap&lt;String, String&gt; params = new HashMap&lt;String, String&gt;();
 * SRUEndpointConfig config = SRUEndpointConfig.parse(params, url.openStream());
 * </pre>
 * 
 * <p>The XML configuration file must validate against the "endpoint-config.xsd"
 * schema bundled with the package and need to have the
 * <code>http://www.clarin.eu/sru-server/1.0/</code> XML namespace.
 * </p> 
 */
public final class SRUEndpointConfig {
    public static final String SRU_TRANSPORT     = "sru.transport";
    public static final String SRU_HOST          = "sru.host";
    public static final String SRU_PORT          = "sru.port";
    public static final String SRU_DATABASE      = "sru.database";
    public static final String SRU_ECHO_REQUESTS = "sru.echoRequests";
    private static final String CONFIG_FILE_NAMESPACE_URI =
            "http://www.clarin.eu/sru-server/1.0/";

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

    private final String transport;
    private final String host;
    private final String port;
    private final String database;
    private final boolean echoRequests;
    private final String baseUrl;
    private final DatabaseInfo databaseInfo;
    private final IndexInfo indexInfo;
    private final List<SchemaInfo> schemaInfo;


    private SRUEndpointConfig(String transport, String host, String port,
            String database, boolean echoRequests, DatabaseInfo databaseinfo,
            IndexInfo indexInfo, List<SchemaInfo> schemaInfo) {
        this.transport    = transport;
        this.host         = host;
        this.port         = port;
        this.database     = database;
        this.databaseInfo = databaseinfo;
        this.indexInfo    = indexInfo;
        if ((schemaInfo != null) && !schemaInfo.isEmpty()) {
            this.schemaInfo = Collections.unmodifiableList(schemaInfo);
        } else {
            this.schemaInfo = null;
        }
        this.echoRequests = echoRequests;

        // build baseUrl
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        if (!"80".equals(port)) {
            sb.append(":").append(port);
        }
        sb.append("/").append(database);
        this.baseUrl = sb.toString();
    }


    public SRUVersion getDefaultVersion() {
        return SRUVersion.VERSION_1_2;
    }


    public SRURecordPacking getDeaultRecordPacking() {
        return SRURecordPacking.XML;
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


    public String getPort() {
        return port;
    }


    public String getDatabase() {
        return database;
    }


    public String getBaseUrl() {
        return baseUrl;
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
                    if (recordSchemaName.equals(schema.getName())) {
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
                   if (schemaIdentifier.equals(schema.getIdentifier())) {
                       return schema.getName();
                   }
               }
           }
        }
        return null;
    }


    /**
     * Parse the XML endpoint configuration.
     * 
     * @param params
     *            additional settings
     * @param in
     *            an {@link InputSource} to the XML configuration file
     * @return a initialized <code>SRUEndpointConfig</code> instance
     * @throws NullPointerException
     *             if <em>params</em> or <em>in</em> is <code>null</code>
     * @throws SRUConfigException
     *             if an error occured
     */
    public static SRUEndpointConfig parse(Map<String, String> params,
            InputStream in) throws SRUConfigException {
        if (params == null) {
            throw new NullPointerException("params == null");
        }
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        try {
            URL url = SRUEndpointConfig.class.getClassLoader()
                    .getResource("META-INF/endpoint-config.xsd");
            if (url == null) {
                throw new SRUConfigException("cannot open " +
                        "\"META-INF/endpoint-config.xsd\"");
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
            InputSource input = new InputSource(in);
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
                        return "http://www.ids-mannheim.de/ns/sru/1.0/";
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

            String port = params.get(SRU_PORT);
            if ((port == null) || port.isEmpty()) {
                throw new SRUConfigException("parameter \"" + SRU_PORT +
                        "\" is mandatory");
            }

            String database = params.get(SRU_DATABASE);
            if ((database == null) || database.isEmpty()) {
                throw new SRUConfigException("parameter \"" + SRU_DATABASE +
                        "\" is mandatory");
            }

            String s;
            boolean echoRequests = false;
            if ((s = params.get(SRU_ECHO_REQUESTS)) != null) {
                echoRequests = Boolean.valueOf(s).booleanValue();
            }

            return new SRUEndpointConfig(transport, host, port, database,
                    echoRequests, databaseInfo, indexInfo,
                    schemaInfo);
        } catch (SRUConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new SRUConfigException("error parsing config", e);
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
                        String set = e2.getAttribute("set");
                        String name = e2.getTextContent();
                        maps.add(new IndexInfo.Index.Map(primary, set, name));
                    }
                }
                indexes.add(new IndexInfo.Index(title, can_search, can_scan, can_sort, maps));
            } // for
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
            result = Boolean.valueOf(attr.getValue()).booleanValue();
        }
        return result;
    }

} // class SRUEndpointConfig
