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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;


final class SRURequestImpl implements SRURequest, SRUDiagnosticList {
    private static final Logger logger =
            LoggerFactory.getLogger(SRURequest.class);
    /* general / explain related parameter names */
    private static final String PARAM_OPERATION            = "operation";
    private static final String PARAM_VERSION              = "version";
    private static final String PARAM_STYLESHEET           = "stylesheet";
    private static final String PARAM_RENDER_BY            = "renderedBy";
    private static final String PARAM_HTTP_ACCEPT          = "httpAccept";
    private static final String PARAM_RESPONSE_TYPE        = "responseType";
    /* searchRetrieve related parameter names */
    private static final String PARAM_QUERY                = "query";
    private static final String PARAM_QUERY_TYPE           = "queryType";
    private static final String PARAM_START_RECORD         = "startRecord";
    private static final String PARAM_MAXIMUM_RECORDS      = "maximumRecords";
    private static final String PARAM_RECORD_XML_ESCAPING  = "recordXMLEscaping";
    private static final String PARAM_RECORD_PACKING       = "recordPacking";
    private static final String PARAM_RECORD_SCHEMA        = "recordSchema";
    private static final String PARAM_RECORD_X_PATH        = "recordXPath";
    private static final String PARAM_RESULT_SET_TTL       = "resultSetTTL";
    private static final String PARAM_SORT_KEYS            = "sortKeys";
    /* scan related parameter names */
    private static final String PARAM_SCAN_CLAUSE          = "scanClause";
    private static final String PARAM_RESPONSE_POSITION    = "responsePosition";
    private static final String PARAM_MAXIMUM_TERMS        = "maximumTerms";
    /* operations */
    private static final String OP_EXPLAIN                 = "explain";
    private static final String OP_SCAN                    = "scan";
    private static final String OP_SEARCH_RETRIEVE         = "searchRetrieve";
    private static final String VERSION_1_1                = "1.1";
    private static final String VERSION_1_2                = "1.2";
    /* various parameter values */
    private static final String RECORD_XML_ESCAPING_XML    = "xml";
    private static final String RECORD_XML_ESCPAING_STRING = "string";
    private static final String RECORD_PACKING_PACKED      = "packed";
    private static final String RECORD_PACKING_UNPACKED    = "unpacked";
    private static final String RENDER_BY_CLIENT           = "client";
    private static final String RENDER_BY_SERVER           = "server";
    private static final String PARAM_EXTENSION_PREFIX     = "x-";
    private static final String X_UNLIMITED_RESULTSET      = "x-unlimited-resultset";
    private static final String X_UNLIMITED_TERMLIST       = "x-unlimited-termlist";
    private static final String X_INDENT_RESPONSE          = "x-indent-response";
    private static final int DEFAULT_START_RECORD          = 1;
    private static final int DEFAULT_RESPONSE_POSITION     = 1;
    private final SRUServerConfig config;
    private final SRUQueryParserRegistry queryParsers;
    private final HttpServletRequest request;
    private List<SRUDiagnostic> diagnostics;
    private SRUOperation operation;
    private SRUVersion version;
    private SRURecordXmlEscaping recordXmlEscaping;
    private SRURecordPacking recordPacking;
    private SRUQuery<?> query;
    private int startRecord = DEFAULT_START_RECORD;
    private int maximumRecords = -1;
    private String recordSchemaIdentifier;
    private String stylesheet;
    private SRURenderBy renderBy;
    private String responseType;
    private String httpAccept;
    private String recordXPath;
    private int resultSetTTL = -1;
    private String sortKeys;
    private CQLNode scanClause;
    private int responsePosition = DEFAULT_RESPONSE_POSITION;
    private int maximumTerms = -1;

    private static enum Parameter {
        STYLESHEET,
        RENDER_BY,
        HTTP_ACCEPT,
        RESPONSE_TYPE,
        START_RECORD,
        MAXIMUM_RECORDS,
        RECORD_XML_ESCAPING,
        RECORD_PACKING,
        RECORD_SCHEMA,
        RECORD_XPATH,
        RESULT_SET_TTL,
        SORT_KEYS,
        SCAN_CLAUSE,
        RESPONSE_POSITION,
        MAXIMUM_TERMS,
    }

    private static final class ParameterInfo {
        private final Parameter parameter;
        private final SRUVersion min;
        private final SRUVersion max;
        private final boolean mandatory;

        private ParameterInfo(Parameter name, boolean mandatory,
                SRUVersion min, SRUVersion max) {
            this.parameter = name;
            this.mandatory = mandatory;
            this.min       = min;
            this.max       = max;
        }

        public Parameter getParameter() {
            return parameter;
        }

        public String getName(SRUVersion version) {
            switch (parameter) {
            case STYLESHEET:
                return PARAM_STYLESHEET;
            case RENDER_BY:
                return PARAM_RENDER_BY;
            case HTTP_ACCEPT:
                return PARAM_HTTP_ACCEPT;
            case RESPONSE_TYPE:
                return PARAM_RESPONSE_TYPE;
            case START_RECORD:
                return PARAM_START_RECORD;
            case MAXIMUM_RECORDS:
                return PARAM_MAXIMUM_RECORDS;
            case RECORD_XML_ESCAPING:
                /*
                 * 'recordPacking' was renamed to 'recordXMLEscaping' in SRU
                 * 2.0. For library API treat 'recordPacking' parameter as
                 * 'recordPacking' for SRU 1.1 and SRU 1.2.
                 */
                if (version == SRUVersion.VERSION_2_0) {
                    return PARAM_RECORD_XML_ESCAPING;
                } else {
                    return PARAM_RECORD_PACKING;
                }
            case RECORD_PACKING:
                /*
                 * 'recordPacking' only exists in SRU 2.0; the old variant is
                 * handled by the case for RECORD_XML_ESCAPING
                 */
                if (version == SRUVersion.VERSION_2_0) {
                    return PARAM_RECORD_PACKING;
                } else {
                    return null;
                }
            case RECORD_SCHEMA:
                return PARAM_RECORD_SCHEMA;
            case RECORD_XPATH:
                return PARAM_RECORD_X_PATH;
            case RESULT_SET_TTL:
                return PARAM_RESULT_SET_TTL;
            case SORT_KEYS:
                return PARAM_SORT_KEYS;
            case SCAN_CLAUSE:
                return PARAM_SCAN_CLAUSE;
            case RESPONSE_POSITION:
                return PARAM_RESPONSE_POSITION;
            case MAXIMUM_TERMS:
                return PARAM_MAXIMUM_TERMS;
            default:
                throw new InternalError();
            } // switch
        }

        public boolean getMandatory() {
            return mandatory;
        }

        public boolean isForVersion(SRUVersion version) {
            return (min.getVersionNumber() <= version.getVersionNumber()) &&
                    (version.getVersionNumber() <= max.getVersionNumber());
        }
    } // class ParameterInfo

    private static final ParameterInfo[] PARAMETER_SET_EXPLAIN = {
            new ParameterInfo(Parameter.STYLESHEET, false,
                    SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
            new ParameterInfo(Parameter.RECORD_XML_ESCAPING, false,
                    SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2)
    };
    private static final ParameterInfo[] PARAMETER_SET_SCAN = {
            new ParameterInfo(Parameter.STYLESHEET, false,
                    SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
            new ParameterInfo(Parameter.HTTP_ACCEPT, false,
                    SRUVersion.VERSION_2_0, SRUVersion.VERSION_2_0),
            new ParameterInfo(Parameter.SCAN_CLAUSE, true,
                    SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
            new ParameterInfo(Parameter.RESPONSE_POSITION, false,
                    SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
            new ParameterInfo(Parameter.MAXIMUM_TERMS, false,
                    SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0)
    };
    private static final ParameterInfo[] PARAMETER_SET_SEARCH_RETRIEVE = {
        new ParameterInfo(Parameter.STYLESHEET, false,
                    SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.HTTP_ACCEPT, false,
                SRUVersion.VERSION_2_0, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.RENDER_BY, false,
                SRUVersion.VERSION_2_0, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.RESPONSE_TYPE, false,
                SRUVersion.VERSION_2_0, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.START_RECORD, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.MAXIMUM_RECORDS, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.RECORD_XML_ESCAPING, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.RECORD_PACKING, false,
                SRUVersion.VERSION_2_0, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.RECORD_SCHEMA, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.RESULT_SET_TTL, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
        new ParameterInfo(Parameter.RECORD_XPATH, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.SORT_KEYS, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_2_0),
    };


    SRURequestImpl(SRUServerConfig config,
            SRUQueryParserRegistry queryParsers,
            HttpServletRequest request) {
        this.config       = config;
        this.queryParsers = queryParsers;
        this.request      = request;
    }


    /**
     * Validate incoming request parameters.
     *
     * @return <code>true</code> if successful, <code>false</code> if something
     *         went wrong
     */
    boolean checkParameters() {
        final SRUVersion minVersion = config.getMinVersion();
        final SRUVersion maxVersion = config.getMaxVersion();

        /*
         * generally assume, we will also allow processing of SRU 1.1 or 1.2
         */
        boolean processSruOld = true;

        /*
         * Heuristic to detect SRU version and operation ...
         */
        SRUOperation operation = null;
        SRUVersion version = null;
        if (maxVersion.compareTo(SRUVersion.VERSION_2_0) >= 0) {
            if (getParameter(PARAM_VERSION, false, false) == null) {
                /*
                 * Ok, we're committed to SRU 2.0 now, so don't allow processing
                 * of SRU 1.1 and 1.2 ...
                 */
                processSruOld = false;

                logger.debug("handling request as SRU 2.0, because no '{}' " +
                        "parameter was found in the request", PARAM_VERSION);
                if ((getParameter(PARAM_QUERY, false, false) != null) ||
                        (getParameter(PARAM_QUERY_TYPE, false, false) != null)) {
                    logger.debug("found parameter '{}' or '{}' therefore " +
                            "assuming '{}' operation",
                            PARAM_QUERY, PARAM_QUERY_TYPE,
                            SRUOperation.SEARCH_RETRIEVE);
                    operation = SRUOperation.SEARCH_RETRIEVE;
                } else if (getParameter(PARAM_SCAN_CLAUSE, false, false) != null) {
                    logger.debug("found parameter '{}' therefore " +
                            "assuming '{}' operation",
                            PARAM_SCAN_CLAUSE, SRUOperation.SCAN);
                    operation = SRUOperation.SCAN;
                } else {
                    logger.debug("no special parameter found therefore " +
                            "assuming '{}' operation",
                            SRUOperation.EXPLAIN);
                    operation = SRUOperation.EXPLAIN;
                }

                /* record version ... */
                version = SRUVersion.VERSION_2_0;

                /* do pedantic check for 'operation' parameter */
                final String op = getParameter(PARAM_OPERATION, false, false);
                if (op != null) {
                    /*
                     * XXX: if operation is searchRetrive and the 'operation'
                     * parameter is also searchRetrieve, should the server just
                     * ignore it?
                     */
                    if (!(operation == SRUOperation.SEARCH_RETRIEVE) &&
                            op.equals(OP_SEARCH_RETRIEVE)) {
                        addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER,
                                PARAM_OPERATION, "Parameter \"" +
                                        PARAM_OPERATION +
                                        "\" is not valid for SRU version 2.0");
                    }
                }
            } else {
                logger.debug("handling request as legacy SRU, because found " +
                        "parameter '{}' in request", PARAM_VERSION);
            }
        }

        if (processSruOld) {
            // parse mandatory operation parameter
            final String op = getParameter(PARAM_OPERATION, false, false);
            if (op != null) {
                if (!op.isEmpty()) {
                    if (op.equals(OP_EXPLAIN)) {
                        operation = SRUOperation.EXPLAIN;
                    } else if (op.equals(OP_SCAN)) {
                        operation = SRUOperation.SCAN;
                    } else if (op.equals(OP_SEARCH_RETRIEVE)) {
                        operation = SRUOperation.SEARCH_RETRIEVE;
                    } else {
                        addDiagnostic(SRUConstants.SRU_UNSUPPORTED_OPERATION,
                                null, "Operation \"" + op + "\" is not supported.");
                    }
                } else {
                    addDiagnostic(SRUConstants.SRU_UNSUPPORTED_OPERATION,
                            null, "An empty parameter \"" +
                                    PARAM_OPERATION +
                                  "\" is not supported.");
                }

                // parse and check version
                version = parseAndCheckVersionParameter(operation);
            } else {
                /*
                 * absent parameter should be interpreted as "explain"
                 */
                operation = SRUOperation.EXPLAIN;

                // parse and check version
                version = parseAndCheckVersionParameter(operation);
            }
        }


        /*
         * sanity check
         */
        if ((version != null) && (operation != null)) {
            logger.debug("min = {}, min? = {}, max = {}, max? = {}, version = {}",
                    minVersion,
                    version.compareTo(minVersion),
                    maxVersion,
                    version.compareTo(maxVersion),
                    version);
            if ((version.compareTo(minVersion) >= 0) &&
                    (version.compareTo(maxVersion) <= 0)) {
                /*
                 * FIXME: re-factor to make this nicer ...
                 */
                this.version   = version;
                this.operation = operation;
                return checkParameters2();
            } else {
                addDiagnostic(SRUConstants.SRU_UNSUPPORTED_VERSION,
                        maxVersion.getVersionString(),
                        "Version \"" + version.getVersionString() +
                        "\" is not supported by this endpoint.");
            }
        }
        logger.debug("bailed");
        return false;
    }


    private boolean checkParameters2() {
        if (diagnostics == null) {
            // check mandatory/optional parameters for operation
            ParameterInfo[] parameter_set;
            switch (operation) {
            case EXPLAIN:
                parameter_set = PARAMETER_SET_EXPLAIN;
                break;
            case SCAN:
                parameter_set = PARAMETER_SET_SCAN;
                break;
            case SEARCH_RETRIEVE:
                parameter_set = PARAMETER_SET_SEARCH_RETRIEVE;
                break;
            default:
                /* actually cannot happen */
                addDiagnostic(SRUConstants.SRU_GENERAL_SYSTEM_ERROR, null,
                        "internal error (invalid operation)");
                return false;
            }

            /*
             * keep list of all submitted parameters (except "operation" and
             * "version"), so we can later warn if an unsupported parameter
             * was sent (= not all parameters were consumed).
             */
            List<String> parameterNames = getParameterNames();

            // check parameters ...
            for (ParameterInfo parameter : parameter_set) {
                final String name = parameter.getName(version);
                if (name == null) {
                    /*
                     * this parameter is not supported in the SRU version that
                     * was used for the request
                     */
                    continue;
                }
                final String value = getParameter(name, true, true);
                if (value != null) {
                    // remove supported parameter from list
                    parameterNames.remove(name);

                    /*
                     * if parameter is not supported in this version, skip
                     * it and create add an diagnostic.
                     */
                    if (!parameter.isForVersion(version)) {
                        addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER,
                                name,
                                "Version " + version.getVersionString() +
                                        " does not support parameter \"" +
                                        name + "\".");
                        continue;
                    }

                    // validate and parse parameters ...
                    switch (parameter.getParameter()) {
                    case RECORD_XML_ESCAPING:
                        if (value.equals(RECORD_XML_ESCAPING_XML)) {
                            recordXmlEscaping = SRURecordXmlEscaping.XML;
                        } else if (value.equals(RECORD_XML_ESCPAING_STRING)) {
                            recordXmlEscaping = SRURecordXmlEscaping.STRING;
                        } else {
                            addDiagnostic(
                                    SRUConstants.SRU_UNSUPPORTED_XML_ESCAPING_VALUE,
                                    null, "Record XML escaping \"" + value +
                                            "\" is not supported.");
                        }
                        break;
                    case RECORD_PACKING:
                        if (value.equals(RECORD_PACKING_PACKED)) {
                            recordPacking = SRURecordPacking.PACKED;
                        } else if (value.equals(RECORD_PACKING_UNPACKED)) {
                            recordPacking = SRURecordPacking.UNPACKED;
                        } else {
                            addDiagnostic(
                                    SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE,
                                    null, "Record packing \"" + value +
                                            "\" is not supported.");
                        }
                        break;
                    case START_RECORD:
                        startRecord = parseNumberedParameter(name, value, 1);
                        break;
                    case MAXIMUM_RECORDS:
                        maximumRecords = parseNumberedParameter(name, value, 0);
                        break;
                    case RECORD_SCHEMA:
                        /*
                         * The parameter recordSchema may contain either schema
                         * identifier or the short name. If available, set to
                         * appropriate schema identifier in the request object.
                         */
                        SRUServerConfig.SchemaInfo schemaInfo =
                            config.findSchemaInfo(value);
                        if (schemaInfo != null) {
                            recordSchemaIdentifier = schemaInfo.getIdentifier();
                        } else {
                            /*
                             * SRU servers are supposed to raise a non-surrogate
                             * (fatal) diagnostic in case the record schema is
                             * not known to the server.
                             */
                            addDiagnostic(
                                    SRUConstants.SRU_UNKNOWN_SCHEMA_FOR_RETRIEVAL,
                                    value, "Record schema \"" + value +
                                    "\" is not supported  for retrieval.");
                        }
                        break;
                    case RECORD_XPATH:
                        recordXPath = value;
                        break;
                    case RESULT_SET_TTL:
                        resultSetTTL = parseNumberedParameter(name, value, 0);
                        break;
                    case SORT_KEYS:
                        sortKeys = value;
                        break;
                    case SCAN_CLAUSE:
                        scanClause = parseScanQueryParameter(name, value);
                        break;
                    case RESPONSE_POSITION:
                        responsePosition = parseNumberedParameter(
                                name, value, 0);
                        break;
                    case MAXIMUM_TERMS:
                        maximumTerms = parseNumberedParameter(name, value, 0);
                        break;
                    case STYLESHEET:
                        stylesheet = value;
                        break;
                    case RENDER_BY:
                        if (value.equals(RENDER_BY_CLIENT)) {
                            renderBy = SRURenderBy.CLIENT;
                        } else if (value.equals(RENDER_BY_SERVER)) {
                            renderBy = SRURenderBy.SERVER;
                        } else {
                            addDiagnostic(
                                    SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE,
                                    null,
                                    "Value \"" + value + "\" for parameter '" +
                                            name + "' is not supported.");
                        }
                        break;
                    case RESPONSE_TYPE:
                        /*
                         * FIXME: check parameter validity?!
                         */
                        responseType = value;
                        break;
                    case HTTP_ACCEPT:
                        /*
                         * FIXME: check parameter validity?!
                         */
                        httpAccept = value;
                        break;
                    } // switch
                } else {
                    if (parameter.getMandatory()) {
                        addDiagnostic(
                                SRUConstants.SRU_MANDATORY_PARAMETER_NOT_SUPPLIED,
                                name, "Mandatory parameter \"" + name +
                                        "\" was not supplied.");
                    }
                }
            } // for

            /*
             * handle query and queryType
             */
            if (operation == SRUOperation.SEARCH_RETRIEVE) {
                /*
                 * determine queryType
                 */
                String queryType = null;
                if (version == SRUVersion.VERSION_2_0) {
                    parameterNames.remove(PARAM_QUERY_TYPE);
                    String value = getParameter(PARAM_QUERY_TYPE, true, true);
                    if (value == null) {
                        queryType = SRUConstants.SRU_QUERY_TYPE_CQL;
                    } else {
                        boolean badCharacters = false;
                        for (int i = 0; i < value.length(); i++) {
                            final char ch = value.charAt(i);
                            if (!((ch >= 'a' && ch <= 'z') ||
                                    (ch >= 'A' && ch <= 'Z') ||
                                    (ch >= '0' && ch <= '9') ||
                                    ((i > 0) && ((ch == '-') || ch == '_')))) {
                                addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE,
                                        PARAM_QUERY_TYPE, "Value contains illegal characters.");
                                badCharacters = true;
                                break;
                            }
                        }
                        if (!badCharacters) {
                            queryType = value;
                        }
                    }
                } else {
                    // SRU 1.1 and SRU 1.2 only support CQL
                    queryType = SRUConstants.SRU_QUERY_TYPE_CQL;
                }


                if (queryType != null) {
                    logger.debug("looking for query parser for query type '{}'",
                            queryType);
                    final SRUQueryParser<?> queryParser =
                            queryParsers.findQueryParser(queryType);
                    if (queryParser != null) {
                        /*
                         * gather query parameters (as required by QueryParser
                         * implementation
                         */
                        final Map<String, String> queryParameters =
                                new HashMap<String, String>();
                        List<String> missingParameter = null;
                        for (String name : queryParser.getQueryParameterNames()) {
                            parameterNames.remove(name);
                            final String value = getParameter(name, true, false);
                            if (value != null) {
                                queryParameters.put(name, value);
                            } else {
                                if (missingParameter == null) {
                                    missingParameter = new ArrayList<String>();
                                }
                                missingParameter.add(name);
                            }
                        }

                        if (missingParameter == null) {
                            logger.debug("parsing query with parser for " +
                                    "type '{}' and parameters {}",
                                    queryParser.getQueryType(),
                                    queryParameters);
                            query = queryParser.parseQuery(version,
                                    queryParameters, this);
                        } else {
                            logger.debug("parameters {} missing, cannot parse query",
                                    missingParameter);
                            for (String name : missingParameter) {
                                addDiagnostic(
                                        SRUConstants.SRU_MANDATORY_PARAMETER_NOT_SUPPLIED,
                                        name, "Mandatory parameter '" + name +
                                                "' is missing or empty. " +
                                                "Required to perform query " +
                                                "of query type '" +
                                                queryType + "'.");
                            }
                        }
                    } else {
                        logger.debug("no parser for query type '{}' found", queryType);
                        addDiagnostic(SRUConstants.SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN, null,
                                "Cannot find query parser for query type '" + queryType + "'.");
                    }
                } else {
                    logger.debug("cannot determine query type");
                    addDiagnostic(SRUConstants.SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN, null,
                            "Cannot determine query type.");
                }
            }


            /*
             *  check if any parameters where not consumed and
             *  add appropriate warnings
             */
            if (!parameterNames.isEmpty()) {
                for (String name : parameterNames) {
                    // skip extraRequestData (aka extensions)
                    if (!name.startsWith(PARAM_EXTENSION_PREFIX)) {
                        addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER,
                                name, "Parameter \"" + name + "\" is not " +
                                        "supported for this operation.");
                    }
                }
            }
        }

        /*
         *  diagnostics == null -> consider as success
         *  FIXME: this should ne done nicer!
         */
        return (diagnostics == null);
    }


    List<SRUDiagnostic> getDiagnostics() {
        return diagnostics;
    }


    SRUVersion getRawVersion() {
        return version;
    }


    SRURecordXmlEscaping getRawRecordPacking() {
        return recordXmlEscaping;
    }


    String getRawRecordSchemaIdentifier() {
        return getParameter(PARAM_RECORD_SCHEMA, true, false);
    }


    String getRawQuery() {
        return getParameter(PARAM_QUERY, true, false);
    }


    int getRawMaximumRecords() {
        return maximumRecords;
    }


    String getRawScanClause() {
        return getParameter(PARAM_SCAN_CLAUSE, true, false);
    }


    String getRawHttpAccept() {
        return getParameter(PARAM_HTTP_ACCEPT, true, false);
    }


    int getIndentResponse() {
        if (config.allowOverrideIndentResponse()) {
            String s = getExtraRequestData(X_INDENT_RESPONSE);
            if (s != null) {
                try {
                    int x = Integer.parseInt(s);
                    if ((x > -2) && (x < 9)) {
                        return x;
                    }
                } catch (NumberFormatException e) {
                    /* IGNORE */
                }
            }
        }
        return config.getIndentResponse();
    }


    @Override
    public SRUOperation getOperation() {
        return operation;
    }


    @Override
    public SRUVersion getVersion() {
        return (version != null) ? version : config.getDefaultVersion();
    }


    @Override
    public boolean isVersion(SRUVersion version) {
        if (version == null) {
            throw new NullPointerException("version == null");
        }
        return getVersion().equals(version);
    }


    @Override
    public boolean isVersion(SRUVersion min, SRUVersion max) {
        if (min == null) {
            throw new NullPointerException("min == null");
        }
        if (max == null) {
            throw new NullPointerException("max == null");
        }
        if (min.getVersionNumber() > max.getVersionNumber()) {
            throw new IllegalArgumentException("min > max");
        }
        final SRUVersion v = getVersion();
        return (min.getVersionNumber() >= v.getVersionNumber()) &&
                (v.getVersionNumber() <= max.getVersionNumber());
    }


    @Override
    public SRURecordXmlEscaping getRecordXmlEscaping() {
        return (recordXmlEscaping != null)
                ? recordXmlEscaping
                : config.getDefaultRecordXmlEscaping();
    }


    @Override
    public SRURecordPacking getRecordPacking() {
        return (recordPacking != null)
                ? recordPacking
                : config.getDefaultRecordPacking();
    }


    @Override
    public SRUQuery<?> getQuery() {
        return query;
    }


    @Override
    public boolean isQueryType(String queryType) {
        if ((queryType != null) && (query != null)) {
            return query.getQueryType().equals(queryType);
        }
        return false;
    }


    @Override
    public String getQueryType() {
        if (query != null) {
            return query.getQueryType();
        }
        return null;
    }


    @Override
    public int getStartRecord() {
        return startRecord;
    }


    @Override
    public int getMaximumRecords() {
        if (config.allowOverrideMaximumRecords() &&
                (getExtraRequestData(X_UNLIMITED_RESULTSET) != null)) {
            return -1;
        }
        if (maximumRecords == -1) {
            return config.getNumberOfRecords();
        } else {
            if (maximumRecords > config.getMaximumRecords()) {
                return config.getMaximumRecords();
            } else {
                return maximumRecords;
            }
        }
    }


    @Override
    public String getRecordSchemaIdentifier() {
        return recordSchemaIdentifier;
    }


    @Override
    public String getRecordXPath() {
        return recordXPath;
    }


    @Override
    public int getResultSetTTL() {
        return resultSetTTL;
    }


    @Override
    public String getSortKeys() {
        return sortKeys;
    }


    @Override
    public CQLNode getScanClause() {
        return scanClause;
    }


    @Override
    public int getResponsePosition() {
        return responsePosition;
    }


    @Override
    public int getMaximumTerms() {
        if (config.allowOverrideMaximumTerms() &&
                (getExtraRequestData(X_UNLIMITED_TERMLIST) != null)) {
            return -1;
        }
        if (maximumTerms == -1) {
            return config.getNumberOfTerms();
        } else {
            if (maximumTerms > config.getMaximumTerms()) {
                return config.getMaximumTerms();
            } else {
                return maximumTerms;
            }
        }
    }


    @Override
    public String getStylesheet() {
        return stylesheet;
    }


    @Override
    public SRURenderBy getRenderBy() {
        return renderBy;
    }


    @Override
    public String getResponeType() {
        return responseType;
    }


    @Override
    public String getHttpAccept() {
        if (httpAccept != null) {
            return httpAccept;
        } else {
            return request.getHeader("ACCEPT");
        }
    }


    @Override
    public String getProtocolScheme() {
        return request.isSecure() ? "https://" : "http://";
    }


    @Override
    public List<String> getExtraRequestDataNames() {
        List<String> result = null;
        for (Enumeration<?> i = request.getParameterNames(); i.hasMoreElements(); ) {
            String name = (String) i.nextElement();
            if (name.startsWith(PARAM_EXTENSION_PREFIX)) {
                if (result == null) {
                    result = new ArrayList<String>();
                }
                result.add(name);
            }
        }

        if (result != null) {
            return result;
        } else {
            return Collections.emptyList();
        }
    }


    @Override
    public String getExtraRequestData(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (!name.startsWith(PARAM_EXTENSION_PREFIX)) {
            throw new IllegalArgumentException(
                    "name must start with \"" + PARAM_EXTENSION_PREFIX + "\"");
        }
        return request.getParameter(name);
    }


    @Override
    public HttpServletRequest getServletRequest() {
        return request;
    }


    @Override
    public void addDiagnostic(String uri, String details, String message) {
        final SRUDiagnostic diagnostic =
                new SRUDiagnostic(uri, details, message);
        if (diagnostics == null) {
            diagnostics = new ArrayList<SRUDiagnostic>();
        }
        diagnostics.add(diagnostic);
    }


    private List<String> getParameterNames() {
        List<String> list = new ArrayList<String>();
        for (Enumeration<?> i = request.getParameterNames();
                i.hasMoreElements();) {
            String name = (String) i.nextElement();
            if (!(name.equals(PARAM_OPERATION) || name.equals(PARAM_VERSION))) {
                list.add(name);
            }
        }
        return list;
    }


    private String getParameter(String name, boolean nullify,
            boolean diagnosticIfEmpty) {
        String s = request.getParameter(name);
        if (s != null) {
            s = s.trim();
            if (nullify && s.isEmpty()) {
                s = null;
                if (diagnosticIfEmpty) {
                    addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE,
                            name, "An empty parameter \"" + name +
                            "\" is not supported.");
                }
            }
        }
        return s;
    }


    private SRUVersion parseAndCheckVersionParameter(SRUOperation operation) {
        final String v = getParameter(PARAM_VERSION, true, true);
        if (v != null) {
            SRUVersion version = null;
            if (v.equals(VERSION_1_1)) {
                version = SRUVersion.VERSION_1_1;
            } else if (v.equals(VERSION_1_2)) {
                version = SRUVersion.VERSION_1_2;
            } else {
                addDiagnostic(SRUConstants.SRU_UNSUPPORTED_VERSION,
                        VERSION_1_2, "Version \"" + v +
                                "\" is not supported");
            }
            return version;
        } else {
            /*
             * except for "explain" operation, complain if "version"
             * parameter was not supplied.
             */
            if (operation != SRUOperation.EXPLAIN) {
                addDiagnostic(
                        SRUConstants.SRU_MANDATORY_PARAMETER_NOT_SUPPLIED,
                        PARAM_VERSION, "Mandatory parameter \"" +
                                PARAM_VERSION + "\" was not supplied.");
            }

            /*
             * this is an explain operation, assume default version
             */
            return config.getDefaultVersion();
        }
    }


    private int parseNumberedParameter(String param, String value,
            int minValue) {
        int result = -1;

        if (value != null) {
            try {
                result = Integer.parseInt(value);
                if (result < minValue) {
                    addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE,
                            param, "Value is less than " + minValue + ".");
                }
            } catch (NumberFormatException e) {
                addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE,
                        param, "Invalid number format.");
            }
        }
        return result;
    }


    private CQLNode parseScanQueryParameter(String param, String value) {
        CQLNode result = null;

        /*
         * XXX: maybe query length against limit and return
         * "Too many characters in query" error?
         */
        try {
            int compat = -1;
            switch (version) {
            case VERSION_1_1:
                compat = CQLParser.V1POINT1;
                break;
            case VERSION_1_2:
                /* FALL-THROUGH */
            case VERSION_2_0:
                compat = CQLParser.V1POINT2;
            }
            result = new CQLParser(compat).parse(value);
        } catch (CQLParseException e) {
            addDiagnostic(SRUConstants.SRU_QUERY_SYNTAX_ERROR,
                    null, "error parsing query");
        } catch (IOException e) {
            addDiagnostic(SRUConstants.SRU_QUERY_SYNTAX_ERROR,
                    null, "error parsing query");
        }
        return result;
    }

} // class SRURequestImpl
