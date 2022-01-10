/**
 * This software is copyright (c) 2011-2022 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;

/**
 * Default query parser to parse CQL.
 */
public final class CQLQueryParser implements SRUQueryParser<CQLNode> {
    private static final String PARAM_QUERY = "query";
    private static final List<String> QUERY_PARAMETER_NAMES =
            Collections.unmodifiableList(Arrays.asList(PARAM_QUERY));


    @Override
    public String getQueryType() {
        return SRUConstants.SRU_QUERY_TYPE_CQL;
    }


    @Override
    public boolean supportsVersion(SRUVersion version) {
        if (version == null) {
            throw new NullPointerException("version == null");
        }
        /*
         * CQL is supported by all SRU versions ...
         */
        return true;
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
    public SRUQuery<CQLNode> parseQuery(SRUVersion version,
            Map<String, String> parameters, SRUDiagnosticList diagnostics) {

        final String rawQuery = parameters.get(PARAM_QUERY);
        if (rawQuery == null) {
            diagnostics.addDiagnostic(SRUConstants.SRU_GENERAL_SYSTEM_ERROR,
                    null, "no query passed to query parser");
            return null;
        }

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
            return new CQLQuery(rawQuery, new CQLParser(compat).parse(rawQuery));
        } catch (CQLParseException e) {
            diagnostics.addDiagnostic(SRUConstants.SRU_QUERY_SYNTAX_ERROR,
                    null, "error parsing query");
        } catch (IOException e) {
            diagnostics.addDiagnostic(SRUConstants.SRU_QUERY_SYNTAX_ERROR,
                    null, "error parsing query");
        }
        return null;
    }


    public static final class CQLQuery extends SRUQueryBase<CQLNode> {

        private CQLQuery(String rawQuery, CQLNode parsedQuery) {
            super(rawQuery, parsedQuery);
        }


        @Override
        public String getQueryType() {
            return SRUConstants.SRU_QUERY_TYPE_CQL;
        }
    }

} // class CQLQueryParser
