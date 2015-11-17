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
    // private constants
    private static final String SRU_DIAGNOSTIC_URI_PREFIX =
            "info:srw/diagnostic/1/";

    // general diagnostics
    public static final String SRU_GENERAL_SYSTEM_ERROR =
            SRU_DIAGNOSTIC_URI_PREFIX + 1;
    public static final String SRU_SYSTEM_TEMPORARILY_UNAVAILABLE =
            SRU_DIAGNOSTIC_URI_PREFIX + 2;
    public static final String SRU_AUTHENTICATION_ERROR =
            SRU_DIAGNOSTIC_URI_PREFIX + 3;
    public static final String SRU_UNSUPPORTED_OPERATION =
            SRU_DIAGNOSTIC_URI_PREFIX + 4;
    public static final String SRU_UNSUPPORTED_VERSION =
            SRU_DIAGNOSTIC_URI_PREFIX + 5;
    public static final String SRU_UNSUPPORTED_PARAMETER_VALUE =
            SRU_DIAGNOSTIC_URI_PREFIX + 6;
    public static final String SRU_MANDATORY_PARAMETER_NOT_SUPPLIED =
            SRU_DIAGNOSTIC_URI_PREFIX + 7;
    public static final String SRU_UNSUPPORTED_PARAMETER =
            SRU_DIAGNOSTIC_URI_PREFIX + 8;
    // diagnostics relating to CQL
    public static final String SRU_QUERY_SYNTAX_ERROR =
            SRU_DIAGNOSTIC_URI_PREFIX + 10;
    public static final String SRU_TOO_MANY_CHARACTERS_IN_QUERY =
            SRU_DIAGNOSTIC_URI_PREFIX + 12;
    public static final String SRU_INVALID_OR_UNSUPPORTED_USE_OF_PARENTHESES =
            SRU_DIAGNOSTIC_URI_PREFIX + 13;
    public static final String SRU_INVALID_OR_UNSUPPORTED_USE_OF_QUOTES =
            SRU_DIAGNOSTIC_URI_PREFIX + 14;
    public static final String SRU_UNSUPPORTED_CONTEXT_SET =
            SRU_DIAGNOSTIC_URI_PREFIX + 15;
    public static final String SRU_UNSUPPORTED_INDEX =
            SRU_DIAGNOSTIC_URI_PREFIX + 16;
    public static final String SRU_UNSUPPORTED_COMBINATION_OF_INDEXES =
            SRU_DIAGNOSTIC_URI_PREFIX + 18;
    public static final String SRU_UNSUPPORTED_RELATION =
            SRU_DIAGNOSTIC_URI_PREFIX + 19;
    public static final String SRU_UNSUPPORTED_RELATION_MODIFIER =
            SRU_DIAGNOSTIC_URI_PREFIX + 20;
    public static final String SRU_UNSUPPORTED_COMBINATION_OF_RELATION_MODIFERS =
            SRU_DIAGNOSTIC_URI_PREFIX + 21;
    public static final String SRU_UNSUPPORTED_COMBINATION_OF_RELATION_AND_INDEX =
            SRU_DIAGNOSTIC_URI_PREFIX + 22;
    public static final String SRU_TOO_MANY_CHARACTERS_IN_TERM =
            SRU_DIAGNOSTIC_URI_PREFIX + 23;
    public static final String SRU_UNSUPPORTED_COMBINATION_OF_RELATION_AND_TERM =
            SRU_DIAGNOSTIC_URI_PREFIX + 24;
    public static final String SRU_NON_SPECIAL_CHARACTER_ESCAPED_IN_TERM =
            SRU_DIAGNOSTIC_URI_PREFIX + 26;
    public static final String SRU_EMPTY_TERM_UNSUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 27;
    public static final String SRU_MASKING_CHARACTER_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 28;
    public static final String SRU_MASKED_WORDS_TOO_SHORT =
            SRU_DIAGNOSTIC_URI_PREFIX + 29;
    public static final String SRU_TOO_MANY_MASKING_CHARACTERS_IN_TERM =
            SRU_DIAGNOSTIC_URI_PREFIX + 30;
    public static final String SRU_ANCHORING_CHARACTER_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 31;
    public static final String SRU_ANCHORING_CHARACTER_IN_UNSUPPORTED_POSITION =
            SRU_DIAGNOSTIC_URI_PREFIX + 32;
    public static final String SRU_COMBINATION_OF_PROXIMITY_ADJACENCY_AND_MASKING_CHARACTERS_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 33;
    public static final String SRU_COMBINATION_OF_PROXIMITY_ADJACENCY_AND_ANCHORING_CHARACTERS_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 34;
    public static final String SRU_TERM_CONTAINS_ONLY_STOPWORDS =
            SRU_DIAGNOSTIC_URI_PREFIX + 35;
    public static final String SRU_TERM_IN_INVALID_FORMAT_FOR_INDEX_OR_RELATION =
            SRU_DIAGNOSTIC_URI_PREFIX + 36;
    public static final String SRU_UNSUPPORTED_BOOLEAN_OPERATOR =
            SRU_DIAGNOSTIC_URI_PREFIX + 37;
    public static final String SRU_TOO_MANY_BOOLEAN_OPERATORS_IN_QUERY =
            SRU_DIAGNOSTIC_URI_PREFIX + 38;
    public static final String SRU_PROXIMITY_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 39;
    public static final String SRU_UNSUPPORTED_PROXIMITY_RELATION =
            SRU_DIAGNOSTIC_URI_PREFIX + 40;
    public static final String SRU_UNSUPPORTED_PROXIMITY_DISTANCE =
            SRU_DIAGNOSTIC_URI_PREFIX + 41;
    public static final String SRU_UNSUPPORTED_PROXIMITY_UNIT =
            SRU_DIAGNOSTIC_URI_PREFIX + 42;
    public static final String SRU_UNSUPPORTED_PROXIMITY_ORDERING =
            SRU_DIAGNOSTIC_URI_PREFIX + 43;
    public static final String SRU_UNSUPPORTED_COMBINATION_OF_PROXIMITY_MODIFIERS =
            SRU_DIAGNOSTIC_URI_PREFIX + 44;
    public static final String SRU_UNSUPPORTED_BOOLEAN_MODIFIER =
            SRU_DIAGNOSTIC_URI_PREFIX + 46;
    public static final String SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN =
            SRU_DIAGNOSTIC_URI_PREFIX + 47;
    public static final String SRU_QUERY_FEATURE_UNSUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 48;
    public static final String SRU_MASKING_CHARACTER_IN_UNSUPPORTED_POSITION =
            SRU_DIAGNOSTIC_URI_PREFIX + 49;
    // diagnostics relating to result sets
    public static final String SRU_RESULT_SETS_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 50;
    public static final String SRU_RESULT_SET_DOES_NOT_EXIST =
            SRU_DIAGNOSTIC_URI_PREFIX + 51;
    public static final String SRU_RESULT_SET_TEMPORARILY_UNAVAILABLE =
            SRU_DIAGNOSTIC_URI_PREFIX + 52;
    public static final String SRU_RESULT_SETS_ONLY_SUPPORTED_FOR_RETRIEVAL =
            SRU_DIAGNOSTIC_URI_PREFIX + 53;
    public static final String SRU_COMBINATION_OF_RESULT_SETS_WITH_SEARCH_TERMS_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 55;
    public static final String SRU_RESULT_SET_CREATED_WITH_UNPREDICTABLE_PARTIAL_RESULTS_AVAILABLE =
            SRU_DIAGNOSTIC_URI_PREFIX + 58;
    public static final String SRU_RESULT_SET_CREATED_WITH_VALID_PARTIAL_RESULTS_AVAILABLE =
            SRU_DIAGNOSTIC_URI_PREFIX + 59;
    public static final String SRU_RESULT_SET_NOT_CREATED_TOO_MANY_MATCHING_RECORDS =
            SRU_DIAGNOSTIC_URI_PREFIX + 60;
    // diagnostics relating to records
    public static final String SRU_FIRST_RECORD_POSITION_OUT_OF_RANGE =
            SRU_DIAGNOSTIC_URI_PREFIX + 61;
    public static final String SRU_RECORD_TEMPORARILY_UNAVAILABLE =
            SRU_DIAGNOSTIC_URI_PREFIX + 64;
    public static final String SRU_RECORD_DOES_NOT_EXIST =
            SRU_DIAGNOSTIC_URI_PREFIX + 65;
    public static final String SRU_UNKNOWN_SCHEMA_FOR_RETRIEVAL =
            SRU_DIAGNOSTIC_URI_PREFIX + 66;
    public static final String SRU_RECORD_NOT_AVAILABLE_IN_THIS_SCHEMA =
            SRU_DIAGNOSTIC_URI_PREFIX + 67;
    public static final String SRU_NOT_AUTHORISED_TO_SEND_RECORD =
            SRU_DIAGNOSTIC_URI_PREFIX + 68;
    public static final String SRU_NOT_AUTHORISED_TO_SEND_RECORD_IN_THIS_SCHEMA =
            SRU_DIAGNOSTIC_URI_PREFIX + 69;
    public static final String SRU_RECORD_TOO_LARGE_TO_SEND =
            SRU_DIAGNOSTIC_URI_PREFIX + 70;
    public static final String SRU_UNSUPPORTED_XML_ESCAPING_VALUE =
            SRU_DIAGNOSTIC_URI_PREFIX + 71;
    public static final String SRU_XPATH_RETRIEVAL_UNSUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 72;
    public static final String SRU_XPATH_EXPRESSION_CONTAINS_UNSUPPORTED_FEATURE =
            SRU_DIAGNOSTIC_URI_PREFIX + 73;
    public static final String SRU_UNABLE_TO_EVALUATE_XPATH_EXPRESSION =
            SRU_DIAGNOSTIC_URI_PREFIX + 74;
    // diagnostics relating to sorting
    public static final String SRU_SORT_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 80;
    public static final String SRU_UNSUPPORTED_SORT_SEQUENCE =
            SRU_DIAGNOSTIC_URI_PREFIX + 82;
    public static final String SRU_TOO_MANY_RECORDS_TO_SORT =
            SRU_DIAGNOSTIC_URI_PREFIX + 83;
    public static final String SRU_TOO_MANY_SORT_KEYS_TO_SORT =
            SRU_DIAGNOSTIC_URI_PREFIX + 84;
    public static final String SRU_CANNOT_SORT_INCOMPATIBLE_RECORD_FORMATS =
            SRU_DIAGNOSTIC_URI_PREFIX + 86;
    public static final String SRU_UNSUPPORTED_SCHEMA_FOR_SORT =
            SRU_DIAGNOSTIC_URI_PREFIX + 87;
    public static final String SRU_UNSUPPORTED_PATH_FOR_SORT =
            SRU_DIAGNOSTIC_URI_PREFIX + 88;
    public static final String SRU_PATH_UNSUPPORTED_FOR_SCHEMA =
            SRU_DIAGNOSTIC_URI_PREFIX + 89;
    public static final String SRU_UNSUPPORTED_DIRECTION =
            SRU_DIAGNOSTIC_URI_PREFIX + 90;
    public static final String SRU_UNSUPPORTED_CASE =
            SRU_DIAGNOSTIC_URI_PREFIX + 91;
    public static final String SRU_UNSUPPORTED_MISSING_VALUE_ACTION =
            SRU_DIAGNOSTIC_URI_PREFIX + 92;
    public static final String SRU_SORT_ENDED_DUE_TO_MISSING_VALUE =
            SRU_DIAGNOSTIC_URI_PREFIX + 93;
    public static final String SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_QUERY_PREVAILS =
            SRU_DIAGNOSTIC_URI_PREFIX + 94;
    public static final String SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_PROTOCOL_PREVAILS =
            SRU_DIAGNOSTIC_URI_PREFIX + 95;
    public static final String SRU_SORT_SPEC_INCLUDED_BOTH_IN_QUERY_AND_PROTOCOL_ERROR =
            SRU_DIAGNOSTIC_URI_PREFIX + 96;
    // diagnostics relating to stylesheets
    public static final String SRU_STYLESHEETS_NOT_SUPPORTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 110;
    public static final String SRU_UNSUPPORTED_STYLESHEET =
            SRU_DIAGNOSTIC_URI_PREFIX + 111;
    // diagnostics relating to scan
    public static final String SRU_RESPONSE_POSITION_OUT_OF_RANGE =
            SRU_DIAGNOSTIC_URI_PREFIX + 120;
    public static final String SRU_TOO_MANY_TERMS_REQUESTED =
            SRU_DIAGNOSTIC_URI_PREFIX + 121;


    /* hide constructor */
    private SRUConstants() {
    }

} // interface SRUConstants
