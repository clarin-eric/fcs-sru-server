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
 * Constants for SRU diagnostics.
 *
 * @see <a href="http://www.loc.gov/standards/sru/specs/diagnostics.html"> SRU
 *      Diagnostics</a>
 * @see <a
 *      href="http://www.loc.gov/standards/sru/resources/diagnostics-list.html">
 *      SRU Diagnostics List</a>
 */
public final class SRUConstants {

    // general diagnostics
    public static final int SRU_GENERAL_SYSTEM_ERROR = 1;
    public static final int SRU_SYSTEM_TEMPORARILY_UNAVAILABLE = 2;
    public static final int SRU_AUTHENTICATION_ERROR = 3;
    public static final int SRU_UNSUPPORTED_OPERATION = 4;
    public static final int SRU_UNSUPPORTED_VERSION = 5;
    public static final int SRU_UNSUPPORTED_PARAMETER_VALUE = 6;
    public static final int SRU_MANDATORY_PARAMETER_NOT_SUPPLIED = 7;
    public static final int SRU_UNSUPPORTED_PARAMETER = 8;
    // diagnostics relating to CQL
    public static final int SRU_QUERY_SYNTAX_ERROR = 10;
    public static final int SRU_TOO_MANY_CHARACTERS_IN_QUERY = 12;
    public static final int SRU_INVALID_OR_UNSUPPORTED_USE_OF_PARENTHESES = 13;
    public static final int SRU_INVALID_OR_UNSUPPORTED_USE_OF_QUOTES = 14;
    public static final int SRU_UNSUPPORTED_CONTEXT_SET = 15;
    public static final int SRU_UNSUPPORTED_INDEX = 16;
    public static final int SRU_UNSUPPORTED_COMBINATION_OF_INDEXES = 18;
    public static final int SRU_UNSUPPORTED_RELATION = 19;
    public static final int SRU_UNSUPPORTED_RELATION_MODIFIER = 20;
    public static final int SRU_UNSUPPORTED_COMBINATION_OF_RELATION_MODIFERS = 21;
    public static final int SRU_UNSUPPORTED_COMBINATION_OF_RELATION_AND_INDEX = 22;
    public static final int SRU_TOO_MANY_CHARACTERS_IN_TERM = 23;
    public static final int SRU_UNSUPPORTED_COMBINATION_OF_RELATION_AND_TERM = 24;
    public static final int SRU_NON_SPECIAL_CHARACTER_ESCAPED_IN_TERM = 26;
    public static final int SRU_EMPTY_TERM_UNSUPPORTED = 27;
    public static final int SRU_MASKING_CHARACTER_NOT_SUPPORTED = 28;
    public static final int SRU_MASKED_WORDS_TOO_SHORT = 29;
    public static final int SRU_TOO_MANY_MASKING_CHARACTERS_IN_TERM = 30;
    public static final int SRU_ANCHORING_CHARACTER_NOT_SUPPORTED = 31;
    public static final int SRU_ANCHORING_CHARACTER_IN_UNSUPPORTED_POSITION = 32;
    public static final int SRU_COMBINATION_OF_PROXIMITY_ADJACENCY_AND_MASKING_CHARACTERS_NOT_SUPPORTED = 33;
    public static final int SRU_COMBINATION_OF_PROXIMITY_ADJACENCY_AND_ANCHORING_CHARACTERS_NOT_SUPPORTED = 34;
    public static final int SRU_TERM_CONTAINS_ONLY_STOPWORDS = 35;
    public static final int SRU_TERM_IN_INVALID_FORMAT_FOR_INDEX_OR_RELATION = 36;
    public static final int SRU_UNSUPPORTED_BOOLEAN_OPERATOR = 37;
    public static final int SRU_TOO_MANY_BOOLEAN_OPERATORS_IN_QUERY = 38;
    public static final int SRU_PROXIMITY_NOT_SUPPORTED = 39;
    public static final int SRU_UNSUPPORTED_PROXIMITY_RELATION = 40;
    public static final int SRU_UNSUPPORTED_PROXIMITY_DISTANCE = 41;
    public static final int SRU_UNSUPPORTED_PROXIMITY_UNIT = 42;
    public static final int SRU_UNSUPPORTED_PROXIMITY_ORDERING = 43;
    public static final int SRU_UNSUPPORTED_COMBINATION_OF_PROXIMITY_MODIFIERS = 44;
    public static final int SRU_UNSUPPORTED_BOOLEAN_MODIFIER = 46;
    public static final int SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN = 47;
    public static final int SRU_QUERY_FEATURE_UNSUPPORTED = 48;
    public static final int SRU_MASKING_CHARACTER_IN_UNSUPPORTED_POSITION = 49;
    // diagnostics relating to result sets
    public static final int SRU_RESULT_SETS_NOT_SUPPORTED = 50;
    public static final int SRU_RESULT_SET_DOES_NOT_EXIST = 51;
    public static final int SRU_RESULT_SET_TEMPORARILY_UNAVAILABLE = 52;
    public static final int SRU_RESULT_SETS_ONLY_SUPPORTED_FOR_RETRIEVAL = 53;
    public static final int SRU_COMBINATION_OF_RESULT_SETS_WITH_SEARCH_TERMS_NOT_SUPPORTED = 55;
    public static final int SRU_RESULT_SET_CREATED_WITH_UNPREDICTABLE_PARTIAL_RESULTS_AVAILABLE = 58;
    public static final int SRU_RESULT_SET_CREATED_WITH_VALID_PARTIAL_RESULTS_AVAILABLE = 59;
    public static final int SRU_RESULT_SET_NOT_CREATED_TOO_MANY_MATCHING_RECORDS = 60;
    // diagnostics relating to records
    public static final int SRU_FIRST_RECORD_POSITION_OUT_OF_RANGE = 61;
    public static final int SRU_RECORD_TEMPORARILY_UNAVAILABLE = 64;
    public static final int SRU_RECORD_DOES_NOT_EXIST = 65;
    public static final int SRU_UNKNOWN_SCHEMA_FOR_RETRIEVAL = 66;
    public static final int SRU_RECORD_NOT_AVAILABLE_IN_THIS_SCHEMA = 67;
    public static final int SRU_NOT_AUTHORISED_TO_SEND_RECORD = 68;
    public static final int SRU_NOT_AUTHORISED_TO_SEND_RECORD_IN_THIS_SCHEMA = 69;
    public static final int SRU_RECORD_TOO_LARGE_TO_SEND = 70;
    public static final int SRU_UNSUPPORTED_RECORD_PACKING = 71;
    public static final int SRU_XPATH_RETRIEVAL_UNSUPPORTED = 72;
    public static final int SRU_XPATH_EXPRESSION_CONTAINS_UNSUPPORTED_FEATURE = 73;
    public static final int SRU_UNABLE_TO_EVALUATE_XPATH_EXPRESSION = 74;
    // diagnostics relating to sorting
    public static final int SRU_SORT_NOT_SUPPORTED = 80;
    public static final int SRU_UNSUPPORTED_SORT_SEQUENCE = 82;
    public static final int SRU_TOO_MANY_RECORDS_TO_SORT = 83;
    public static final int SRU_TOO_MANY_SORT_KEYS_TO_SORT = 84;
    public static final int SRU_CANNOT_SORT_INCOMPATIBLE_RECORD_FORMATS = 86;
    public static final int SRU_UNSUPPORTED_SCHEMA_FOR_SORT = 87;
    public static final int SRU_UNSUPPORTED_PATH_FOR_SORT = 88;
    public static final int SRU_PATH_UNSUPPORTED_FOR_SCHEMA = 89;
    public static final int SRU_UNSUPPORTED_DIRECTION = 90;
    public static final int SRU_UNSUPPORTED_CASE = 91;
    public static final int SRU_UNSUPPORTED_MISSING_VALUE_ACTION = 92;
    public static final int SRU_SORT_ENDED_DUE_TO_MISSING_VALUE = 93;
    public static final int SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_QUERY_PREVAILS = 94;
    public static final int SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_PROTOCOL_PREVAILS = 95;
    public static final int SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_ERROR = 96;
    // diagnostics relating to stylesheets
    public static final int SRU_STYLESHEETS_NOT_SUPPORTED = 110;
    public static final int SRU_UNSUPPORTED_STYLESHEET = 111;
    // diagnostics relating to scan
    public static final int SRU_RESPONSE_POSITION_OUT_OF_RANGE = 120;
    public static final int SRU_TOO_MANY_TERMS_REQUESTED = 121;

    public static final String SRU_DIAGNOSTIC_URI_PREFIX = "info:srw/diagnostic/1/";

    /* hide constructor */
    private SRUConstants() {
    }

} // interface SRUConstants
