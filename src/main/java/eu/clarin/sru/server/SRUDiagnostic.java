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

/**
 * Class to hold a SRU diagnostic.
 *
 * @see SRUConstants
 * @see SRUDiagnosticList
 * @see <a href="http://www.loc.gov/standards/sru/specs/diagnostics.html">SRU Diagnostics</a>
 * @see <a href="http://www.loc.gov/standards/sru/resources/diagnostics-list.html">SRU Diagnostics List</a>
 */
public final class SRUDiagnostic {
    private final int code;
    private final String details;
    private final String message;


    /**
     * Constructor.
     *
     * @param code
     *            numerical diagnostic code
     * @param details
     *            supplementary information available, often in a format
     *            specified by the diagnostic or <code>null</code>
     * @param message
     *            human readable message to display to the end user or
     *            <code>null</code>
     */
    public SRUDiagnostic(int code, String details, String message) {
        this.code = code;
        this.details = details;
        this.message = message;
    }


    /**
     * Constructor.
     *
     * @param code
     *            numerical diagnostic code
     * @param details
     *            supplementary information available, often in a format
     *            specified by the diagnostic or <code>null</code>
     */
    public SRUDiagnostic(int code, String details) {
        this(code, details, null);
    }


    /**
     * Constructor.
     *
     * @param code
     *            numerical diagnostic code
     */
    public SRUDiagnostic(int code) {
        this(code, null, null);
    }


    /**
     * Get code for this diagnostic.
     *
     * @return diagnostic code
     * @see SRUConstants
     */
    public int getCode() {
        return code;
    }


    /**
     * Get supplementary information for this diagnostic. The format for this
     * value is often specified by the diagnostic code.
     *
     * @return supplementary information
     */
    public String getDetails() {
        return details;
    }


    /**
     * Get human readable message.
     *
     * @return human readable message
     */
    public String getMessage() {
        return message != null ? message : getDefaultErrorMessage(code);
    }


    private static String getDefaultErrorMessage(int code) {
        switch (code) {
        case SRUConstants.SRU_GENERAL_SYSTEM_ERROR:
            return "General system error";
        case SRUConstants.SRU_SYSTEM_TEMPORARILY_UNAVAILABLE:
            return "System temporarily unavailable";
        case SRUConstants.SRU_AUTHENTICATION_ERROR:
            return "Authentication error";
        case SRUConstants.SRU_UNSUPPORTED_OPERATION:
            return "Unsupported operation";
        case SRUConstants.SRU_UNSUPPORTED_VERSION:
            return "Unsupported version";
        case SRUConstants.SRU_UNSUPPORTED_PARAMETER_VALUE:
            return "Unsupported parameter value";
        case SRUConstants.SRU_MANDATORY_PARAMETER_NOT_SUPPLIED:
            return "Mandatory parameter not supplied";
        case SRUConstants.SRU_UNSUPPORTED_PARAMETER:
            return "Unsupported Parameter";
        case SRUConstants.SRU_QUERY_SYNTAX_ERROR:
            return "Query syntax error";
        case SRUConstants.SRU_TOO_MANY_CHARACTERS_IN_QUERY:
            return "Too many characters in query";
        case SRUConstants.SRU_INVALID_OR_UNSUPPORTED_USE_OF_PARENTHESES:
            return "Invalid or unsupported use of parentheses";
        case SRUConstants.SRU_INVALID_OR_UNSUPPORTED_USE_OF_QUOTES:
            return "Invalid or unsupported use of quotes";
        case SRUConstants.SRU_UNSUPPORTED_CONTEXT_SET:
            return "Unsupported context set";
        case SRUConstants.SRU_UNSUPPORTED_INDEX:
            return "Unsupported index";
        case SRUConstants.SRU_UNSUPPORTED_COMBINATION_OF_INDEXES:
            return "Unsupported combination of indexes";
        case SRUConstants.SRU_UNSUPPORTED_RELATION:
            return "Unsupported relation";
        case SRUConstants.SRU_UNSUPPORTED_RELATION_MODIFIER:
            return "Unsupported relation modifier";
        case SRUConstants.SRU_UNSUPPORTED_COMBINATION_OF_RELATION_MODIFERS:
            return "Unsupported combination of relation modifers";
        case SRUConstants.SRU_UNSUPPORTED_COMBINATION_OF_RELATION_AND_INDEX:
            return "Unsupported combination of relation and index";
        case SRUConstants.SRU_TOO_MANY_CHARACTERS_IN_TERM:
            return "Too many characters in term";
        case SRUConstants.SRU_UNSUPPORTED_COMBINATION_OF_RELATION_AND_TERM:
            return "Unsupported combination of relation and term";
        case SRUConstants.SRU_NON_SPECIAL_CHARACTER_ESCAPED_IN_TERM:
            return "Non special character escaped in term";
        case SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED:
            return "Empty term unsupported";
        case SRUConstants.SRU_MASKING_CHARACTER_NOT_SUPPORTED:
            return "Masking character not supported";
        case SRUConstants.SRU_MASKED_WORDS_TOO_SHORT:
            return "Masked words too short";
        case SRUConstants.SRU_TOO_MANY_MASKING_CHARACTERS_IN_TERM:
            return "Too many masking characters in term";
        case SRUConstants.SRU_ANCHORING_CHARACTER_NOT_SUPPORTED:
            return "Anchoring character not supported";
        case SRUConstants.SRU_ANCHORING_CHARACTER_IN_UNSUPPORTED_POSITION:
            return "Anchoring character in unsupported position";
        case SRUConstants.SRU_COMBINATION_OF_PROXIMITY_ADJACENCY_AND_MASKING_CHARACTERS_NOT_SUPPORTED:
            return "Combination of proximity adjacency and masking characters not supported";
        case SRUConstants.SRU_COMBINATION_OF_PROXIMITY_ADJACENCY_AND_ANCHORING_CHARACTERS_NOT_SUPPORTED:
            return "Combination of proximity adjacency and anchoring characters not supported";
        case SRUConstants.SRU_TERM_CONTAINS_ONLY_STOPWORDS:
            return "Term contains only stopwords";
        case SRUConstants.SRU_TERM_IN_INVALID_FORMAT_FOR_INDEX_OR_RELATION:
            return "Term in invalid format for index or relation";
        case SRUConstants.SRU_UNSUPPORTED_BOOLEAN_OPERATOR:
            return "Unsupported boolean operator";
        case SRUConstants.SRU_TOO_MANY_BOOLEAN_OPERATORS_IN_QUERY:
            return "Too many boolean operators in query";
        case SRUConstants.SRU_PROXIMITY_NOT_SUPPORTED:
            return "Proximity not supported";
        case SRUConstants.SRU_UNSUPPORTED_PROXIMITY_RELATION:
            return "Unsupported proximity relation";
        case SRUConstants.SRU_UNSUPPORTED_PROXIMITY_DISTANCE:
            return "Unsupported proximity distance";
        case SRUConstants.SRU_UNSUPPORTED_PROXIMITY_UNIT:
            return "Unsupported proximity unit";
        case SRUConstants.SRU_UNSUPPORTED_PROXIMITY_ORDERING:
            return "Unsupported proximity ordering";
        case SRUConstants.SRU_UNSUPPORTED_COMBINATION_OF_PROXIMITY_MODIFIERS:
            return "Unsupported combination of proximity modifiers";
        case SRUConstants.SRU_UNSUPPORTED_BOOLEAN_MODIFIER:
            return "Unsupported boolean modifier";
        case SRUConstants.SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN:
            return "Cannot process query; reason unknown";
        case SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED:
            return "Query feature unsupported";
        case SRUConstants.SRU_MASKING_CHARACTER_IN_UNSUPPORTED_POSITION:
            return "Masking character in unsupported position";
        case SRUConstants.SRU_RESULT_SETS_NOT_SUPPORTED:
            return "Result sets not supported";
        case SRUConstants.SRU_RESULT_SET_DOES_NOT_EXIST:
            return "Result set does not exist";
        case SRUConstants.SRU_RESULT_SET_TEMPORARILY_UNAVAILABLE:
            return "Result set temporarily unavailable";
        case SRUConstants.SRU_RESULT_SETS_ONLY_SUPPORTED_FOR_RETRIEVAL:
            return "Result sets only supported for retrieval";
        case SRUConstants.SRU_COMBINATION_OF_RESULT_SETS_WITH_SEARCH_TERMS_NOT_SUPPORTED:
            return "Combination of result sets with search terms not supported";
        case SRUConstants.SRU_RESULT_SET_CREATED_WITH_UNPREDICTABLE_PARTIAL_RESULTS_AVAILABLE:
            return "Result set created with unpredictable partial results available";
        case SRUConstants.SRU_RESULT_SET_CREATED_WITH_VALID_PARTIAL_RESULTS_AVAILABLE:
            return "Result set created with valid partial results available";
        case SRUConstants.SRU_RESULT_SET_NOT_CREATED_TOO_MANY_MATCHING_RECORDS:
            return "Result set not created: too many matching records";
        case SRUConstants.SRU_FIRST_RECORD_POSITION_OUT_OF_RANGE:
            return "First record position out of range";
        case SRUConstants.SRU_RECORD_TEMPORARILY_UNAVAILABLE:
            return "Record temporarily unavailable";
        case SRUConstants.SRU_RECORD_DOES_NOT_EXIST:
            return "Record does not exist";
        case SRUConstants.SRU_UNKNOWN_SCHEMA_FOR_RETRIEVAL:
            return "Unknown schema for retrieval";
        case SRUConstants.SRU_RECORD_NOT_AVAILABLE_IN_THIS_SCHEMA:
            return "Record not available in this schema";
        case SRUConstants.SRU_NOT_AUTHORISED_TO_SEND_RECORD:
            return "Not authorised to send record";
        case SRUConstants.SRU_NOT_AUTHORISED_TO_SEND_RECORD_IN_THIS_SCHEMA:
            return "Not authorised to send record in this schema";
        case SRUConstants.SRU_RECORD_TOO_LARGE_TO_SEND:
            return "Record too large to send";
        case SRUConstants.SRU_UNSUPPORTED_RECORD_PACKING:
            return "Unsupported record packing";
        case SRUConstants.SRU_XPATH_RETRIEVAL_UNSUPPORTED:
            return "XPath retrieval unsupported";
        case SRUConstants.SRU_XPATH_EXPRESSION_CONTAINS_UNSUPPORTED_FEATURE:
            return "XPath expression contains unsupported feature";
        case SRUConstants.SRU_UNABLE_TO_EVALUATE_XPATH_EXPRESSION:
            return "Unable to evaluate XPath expression";
        case SRUConstants.SRU_SORT_NOT_SUPPORTED:
            return "Sort not supported";
        case SRUConstants.SRU_UNSUPPORTED_SORT_SEQUENCE:
            return "Unsupported sort sequence";
        case SRUConstants.SRU_TOO_MANY_RECORDS_TO_SORT:
            return "Too many records to sort";
        case SRUConstants.SRU_TOO_MANY_SORT_KEYS_TO_SORT:
            return "Too many sort keys to sort";
        case SRUConstants.SRU_CANNOT_SORT_INCOMPATIBLE_RECORD_FORMATS:
            return "Cannot sort incompatible record formats";
        case SRUConstants.SRU_UNSUPPORTED_SCHEMA_FOR_SORT:
            return "Unsupported schema for sort";
        case SRUConstants.SRU_UNSUPPORTED_PATH_FOR_SORT:
            return "Unsupported path for sort";
        case SRUConstants.SRU_PATH_UNSUPPORTED_FOR_SCHEMA:
            return "Path unsupported for schema";
        case SRUConstants.SRU_UNSUPPORTED_DIRECTION:
            return "Unsupported direction";
        case SRUConstants.SRU_UNSUPPORTED_CASE:
            return "Unsupported case";
        case SRUConstants.SRU_UNSUPPORTED_MISSING_VALUE_ACTION:
            return "Unsupported missing value action";
        case SRUConstants.SRU_SORT_ENDED_DUE_TO_MISSING_VALUE:
            return "Sort ended due to missing value";
        case SRUConstants.SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_QUERY_PREVAILS:
            return "Sort spec included both in query and protocol query prevails";
        case SRUConstants.SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_PROTOCOL_PREVAILS:
            return "Sort spec included both in query and protocol protocol prevails";
        case SRUConstants.SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_ERROR:
            return "Sort spec included both in query and protocol error";
        case SRUConstants.SRU_STYLESHEETS_NOT_SUPPORTED:
            return "Stylesheets not supported";
        case SRUConstants.SRU_UNSUPPORTED_STYLESHEET:
            return "Unsupported stylesheet";
        case SRUConstants.SRU_RESPONSE_POSITION_OUT_OF_RANGE:
            return "Response position out of range";
        case SRUConstants.SRU_TOO_MANY_TERMS_REQUESTED:
            return "Too many terms requested";
        default:
            return null;
        } // switch
    }

} // class SRUDiagnostic
