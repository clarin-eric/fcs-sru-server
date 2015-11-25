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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class SearchTermsQueryParser implements SRUQueryParser<List<String>> {
    private static final String PARAM_QUERY = "query";
    private static final List<String> QUERY_PARAMETER_NAMES =
            Collections.unmodifiableList(Arrays.asList(PARAM_QUERY));

    @Override
    public String getQueryType() {
        return SRUConstants.SRU_QUERY_TYPE_SEARCH_TERMS;
    }


    @Override
    public boolean supportsVersion(SRUVersion version) {
        if (version == null) {
            throw new NullPointerException("version == null");
        }
        return version.compareTo(SRUVersion.VERSION_2_0) >= 0;
    }


    @Override
    public String getQueryTypeDefintion() {
        return null;
    }


    @Override
    public List<String> getQueryParameterNames() {
        return QUERY_PARAMETER_NAMES;
    }


    @Override
    public SRUQuery<List<String>> parseQuery(SRUVersion version,
            Map<String, String> parameters, SRUDiagnosticList diagnostics) {
        final String rawQuery = parameters.get(PARAM_QUERY);
        if (rawQuery == null) {
            diagnostics.addDiagnostic(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    null, "no query passed to query parser");
            return null;
        }

        String[] terms = rawQuery.split("\\s+");
        return new SearchTermsQuery(rawQuery, Arrays.asList(terms));
    }


    public static final class SearchTermsQuery extends SRUQueryBase<List<String>> {

        private SearchTermsQuery(String rawQuery, List<String> parsedQuery) {
            super(rawQuery, Collections.unmodifiableList(parsedQuery));
            System.err.println("XXXX " + parsedQuery);
        }


        @Override
        public String getQueryType() {
            return SRUConstants.SRU_QUERY_TYPE_SEARCH_TERMS;
        }
    }

} // class SearchTermsQueryParser
