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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;


final class SRURequestImpl implements SRURequest, SRUDiagnosticList {
    private static final String PARAM_OPERATION         = "operation";
    private static final String PARAM_VERSION           = "version";
    private static final String PARAM_RECORD_PACKING    = "recordPacking";
    private static final String PARAM_STYLESHEET        = "stylesheet";
    private static final String PARAM_QUERY             = "query";
    private static final String PARAM_START_RECORD      = "startRecord";
    private static final String PARAM_MAXIMUM_RECORDS   = "maximumRecords";
    private static final String PARAM_RECORD_SCHEMA     = "recordSchema";
    private static final String PARAM_RECORD_X_PATH     = "recordXPath";
    private static final String PARAM_RESULT_SET_TTL    = "resultSetTTL";
    private static final String PARAM_SORT_KEYS         = "sortKeys";
    private static final String PARAM_SCAN_CLAUSE       = "scanClause";
    private static final String PARAM_RESPONSE_POSITION = "responsePosition";
    private static final String PARAM_MAXIMUM_TERMS     = "maximumTerms";
    private static final String OP_EXPLAIN              = "explain";
    private static final String OP_SCAN                 = "scan";
    private static final String OP_SEARCH_RETRIEVE      = "searchRetrieve";
    private static final String VERSION_1_1             = "1.1";
    private static final String VERSION_1_2             = "1.2";
    private static final String RECORD_PACKING_XML      = "xml";
    private static final String RECORD_PACKING_STRING   = "string";
    private static final String PARAM_EXTENSION_PREFIX  = "x-";
    private static final String X_UNLIMITED_RESULTSET   = "x-unlimited-resultset";
    private static final String X_UNLIMITED_TERMLIST    = "x-unlimited-termlist";
    private static final String X_INDENT_RESPONSE       = "x-indent-response";
    private static final int DEFAULT_START_RECORD       = 1;
    private static final int DEFAULT_RESPONSE_POSITION  = 1;
    private final SRUServerConfig config;
    private final HttpServletRequest request;
    private List<SRUDiagnostic> diagnostics;
    private SRUOperation operation;
    private SRUVersion version;
    private SRURecordPacking recordPacking;
    private CQLNode query;
    private String rawQuery;
    private int startRecord = -1;
    private int maximumRecords = -1;
    private String recordSchemaName;
    private String recordSchemaIdentifier;
    private String stylesheet;
    private String recordXPath;
    private int resultSetTTL = -1;
    private String sortKeys;
    private CQLNode scanClause;
    private String rawScanClause;
    private int responsePosition = -1;
    private int maximumTerms = -1;

    private static enum Parameter {
        RECORD_PACKING,
        QUERY,
        START_RECORD,
        MAXIMUM_RECORDS,
        RECORD_SCHEMA,
        RECORD_XPATH,
        RESULT_SET_TTL,
        SORT_KEYS,
        SCAN_CLAUSE,
        RESPONSE_POSITION,
        MAXIMUM_TERMS,
        STYLESHEET
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

        public String getName() {
            switch (parameter) {
            case RECORD_PACKING:
                return PARAM_RECORD_PACKING;
            case QUERY:
                return PARAM_QUERY;
            case START_RECORD:
                return PARAM_START_RECORD;
            case MAXIMUM_RECORDS:
                return PARAM_MAXIMUM_RECORDS;
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
            case STYLESHEET:
                return PARAM_STYLESHEET;
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

    private static final ParameterInfo[] PARAMS_EXPLAIN = {
        new ParameterInfo(Parameter.RECORD_PACKING, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.STYLESHEET, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2)
    };
    private static final ParameterInfo[] PARAMS_SCAN = {
        new ParameterInfo(Parameter.SCAN_CLAUSE, true,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.RESPONSE_POSITION, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.MAXIMUM_TERMS, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.STYLESHEET, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2)

    };
    private static final ParameterInfo[] PARAMS_SEARCH_RETRIEVE = {
        new ParameterInfo(Parameter.QUERY, true,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.START_RECORD, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.MAXIMUM_RECORDS, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.RECORD_PACKING, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.RECORD_SCHEMA, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.RECORD_XPATH, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_1),
        new ParameterInfo(Parameter.RESULT_SET_TTL, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2),
        new ParameterInfo(Parameter.SORT_KEYS, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_1),
        new ParameterInfo(Parameter.STYLESHEET, false,
                SRUVersion.VERSION_1_1, SRUVersion.VERSION_1_2)
    };


    SRURequestImpl(SRUServerConfig config, HttpServletRequest request) {
        this.config        = config;
        this.request       = request;
    }


    boolean checkParameters() {
        // parse mandatory operation parameter
        final String op = getParameter(PARAM_OPERATION, false);
        if (op != null) {
            if (!op.isEmpty()) {
                if (op.equals(OP_EXPLAIN)) {
                    this.operation = SRUOperation.EXPLAIN;
                } else if (op.equals(OP_SCAN)) {
                    this.operation = SRUOperation.SCAN;
                } else if (op.equals(OP_SEARCH_RETRIEVE)) {
                    this.operation = SRUOperation.SEARCH_RETRIEVE;
                } else {
                    addDiagnostic(SRUConstants.SRU_UNSUPPORTED_OPERATION, null,
                            "Operation \"" + op + "\" is not supported.");
                }
            } else {
                addDiagnostic(SRUConstants.SRU_UNSUPPORTED_OPERATION, null,
                        "An empty parameter \"" + PARAM_OPERATION +
                                "\" is not supported.");
            }

            // parse and check version
            parseAndCheckVersionParameter();
        } else {
            /*
             * absent parameter should be interpreted as "explain"
             */
            this.operation = SRUOperation.EXPLAIN;

            // parse and check version
            parseAndCheckVersionParameter();
        }

        if (diagnostics == null) {
            // check mandatory/optional parameters for operation
            ParameterInfo[] parameters;
            switch (operation) {
            case EXPLAIN:
                parameters = PARAMS_EXPLAIN;
                break;
            case SCAN:
                parameters = PARAMS_SCAN;
                break;
            case SEARCH_RETRIEVE:
                parameters = PARAMS_SEARCH_RETRIEVE;
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
            for (ParameterInfo parameter : parameters) {
                String value = getParameter(parameter.getName(), true);
                if (value != null) {
                    // remove supported parameter from list
                    parameterNames.remove(parameter.getName());

                    /*
                     * if parameter is not supported in this version, skip
                     * it and create add an diagnostic.
                     */
                    if (!parameter.isForVersion(version)) {
                        addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER,
                                parameter.getName(),
                                "Version " + version.getVersionString() +
                                        " does not support parameter \"" +
                                        parameter.getName() + "\".");
                        continue;
                    }

                    // validate and parse parameters ...
                    switch (parameter.getParameter()) {
                    case RECORD_PACKING:
                        if (value.endsWith(RECORD_PACKING_XML)) {
                            recordPacking = SRURecordPacking.XML;
                        } else if (value.equals(RECORD_PACKING_STRING)) {
                            recordPacking = SRURecordPacking.STRING;
                        } else {
                            addDiagnostic(
                                    SRUConstants.SRU_UNSUPPORTED_RECORD_PACKING,
                                    null, "Record packing \"" + value +
                                            "\" is not supported.");
                        }
                        break;
                    case QUERY:
                        query = parseCQLParameter(parameter.getName(), value);
                        if (query != null) {
                            rawQuery = value;
                        }
                        break;
                    case START_RECORD:
                        startRecord = parseNumberedParameter(
                                parameter.getName(), value, 1);
                        break;
                    case MAXIMUM_RECORDS:
                        maximumRecords = parseNumberedParameter(
                                parameter.getName(), value, 0);
                        break;
                    case RECORD_SCHEMA:
                        if (value != null) {
                            /*
                             * If the recordSchema is supplied, check if it is
                             * supported by this endpoint. If not, raise
                             * an error. recoedSchema may contain either
                             * schema identifier or the short name.
                             */
                            SRUServerConfig.SchemaInfo schemaInfo =
                                    config.findSchemaInfo(value);
                            if (schemaInfo != null) {
                                recordSchemaIdentifier =
                                        schemaInfo.getIdentifier();
                                recordSchemaName =
                                        schemaInfo.getName();
                            } else {
                                addDiagnostic(
                                        SRUConstants.SRU_UNKNOWN_SCHEMA_FOR_RETRIEVAL,
                                        value,
                                        "Record schema \"" + value +
                                        "\" is not supported for retrieval.");
                            }
                        }
                        break;
                    case RECORD_XPATH:
                        recordXPath = value;
                        break;
                    case RESULT_SET_TTL:
                        resultSetTTL = parseNumberedParameter(
                                parameter.getName(), value, 0);
                        break;
                    case SORT_KEYS:
                        sortKeys = value;
                        break;
                    case SCAN_CLAUSE:
                        scanClause = parseCQLParameter(
                                parameter.getName(), value);
                        if (scanClause != null) {
                            rawScanClause = value;
                        }
                        break;
                    case RESPONSE_POSITION:
                        responsePosition = parseNumberedParameter(
                                parameter.getName(), value, 0);
                        break;
                    case MAXIMUM_TERMS:
                        maximumTerms = parseNumberedParameter(
                                parameter.getName(), value, 0);
                        break;
                    case STYLESHEET:
                        stylesheet = value;
                        break;
                    } // switch
                } else {
                    if (parameter.getMandatory()) {
                        addDiagnostic(
                                SRUConstants.SRU_MANDATORY_PARAMETER_NOT_SUPPLIED,
                                parameter.getName(), "Mandatory parameter \"" +
                                        parameter.getName() +
                                        "\" was not supplied.");
                    }
                }
            } // for

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

        // diagnostics != null -> consider as sucesss
        return (diagnostics == null);
    }


    List<SRUDiagnostic> getDiagnostics() {
        return diagnostics;
    }


    SRUVersion getRawVersion() {
        return version;
    }


    SRURecordPacking getRawRecordPacking() {
        return recordPacking;
    }


    String getRawQuery() {
        return rawQuery;
    }


    int getRawStartRecord() {
        return startRecord;
    }


    int getRawMaximumRecords() {
        return maximumRecords;
    }


    String getRawScanClause() {
        return rawScanClause;
    }


    int getRawResponsePosition() {
        return responsePosition;
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
    public SRURecordPacking getRecordPacking() {
        return (recordPacking != null)
                ? recordPacking
                : config.getDeaultRecordPacking();
    }


    @Override
    public CQLNode getQuery() {
        return query;
    }


    @Override
    public int getStartRecord() {
        return (startRecord > 0) ? startRecord : DEFAULT_START_RECORD;
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
    public String getRecordSchemaName() {
        return recordSchemaName;
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
        return (responsePosition >= 0)
                ? responsePosition : DEFAULT_RESPONSE_POSITION;
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
    public void addDiagnostic(int code, String details, String message) {
        if (diagnostics == null) {
            diagnostics = new ArrayList<SRUDiagnostic>();
        }
        diagnostics.add(new SRUDiagnostic(code, details, message));
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


    private String getParameter(String name, boolean nullify) {
        String s = request.getParameter(name);
        if (s != null) {
            s = s.trim();
            if (nullify && s.isEmpty()) {
                addDiagnostic(SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE,
                        name, "An empty parameter \"" + PARAM_OPERATION +
                                "\" is not supported.");
                s = null;
            }
        }
        return s;
    }


    private void parseAndCheckVersionParameter() {
        final String v = getParameter(PARAM_VERSION, true);
        if (v != null) {
            if (v.equals(VERSION_1_1)) {
                this.version = SRUVersion.VERSION_1_1;
            } else if (v.equals(VERSION_1_2)) {
                this.version = SRUVersion.VERSION_1_2;
            } else {
                addDiagnostic(SRUConstants.SRU_UNSUPPORTED_VERSION,
                        VERSION_1_2, "Version \"" + v +
                                "\" is not supported");
            }
        } else {
            /*
             * except for "explain" operation, complain if "version"
             * parameter was not supplied.
             */
            if (this.operation != SRUOperation.EXPLAIN) {
                addDiagnostic(
                        SRUConstants.SRU_MANDATORY_PARAMETER_NOT_SUPPLIED,
                        PARAM_VERSION, "Mandatory parameter \"" +
                                PARAM_VERSION + "\" was not supplied.");
            }

            /*
             * this is an explain operation, assume default version
             */
            this.version = config.getDefaultVersion();
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


    private CQLNode parseCQLParameter(String param, String value) {
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
