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

import java.util.List;
import java.util.Map;

/**
 * Interface for implementing pluggable query parsers.
 *
 * @param <T> abstract syntax tree (object) for parsed queries.
 */
public interface SRUQueryParser<T> {

    /**
     * Get the short name for supported query, e.g. "cql".
     *
     * @return the query short name
     */
    public String getQueryType();


    /**
     * Check if query is supported by a specific version of SRU/CQL
     *
     * @param version
     *            the version
     * @return <code>true</code> if version is supported, <code>false</code>
     *         otherwise
     */
    public boolean supportsVersion(SRUVersion version);


    /**
     * The URI for the for the query type’s definition.
     *
     * @return the  URI for the for the query type’s definition.
     */
    public String getQueryTypeDefintion();


    /**
     * Get the list of query parameters.
     *
     * @return the list of query parameters names.
     */
    public List<String> getQueryParameterNames();


    /**
     * Parse a query into an abstract syntax tree
     *
     * @param version
     *            the SRU version the request was made
     * @param parameters
     *            the request parameters containing the query (@see
     *            {@link SRUQueryParser#getQueryParameterNames()}
     * @param diagnostics
     *            a {@link SRUDiagnosticList} for storing fatal and non-fatal
     *            diagnostics
     * @return the parsed query or <code>null</code> if the query could not be
     *         parsed
     */
    public SRUQuery<T> parseQuery(SRUVersion version,
            Map<String, String> parameters, SRUDiagnosticList diagnostics);

} // interface SRUQueryParser
