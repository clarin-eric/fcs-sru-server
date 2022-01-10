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

/**
 * Holder class for a parsed query to be returned from a {@link SRUQueryParser}.
 *
 * @param <T> abstract syntax tree (object) for parsed queries.
 */
public interface SRUQuery<T> {

    /**
     * Get the short name for this parsed query, e.g. "cql".
     *
     * @return the short name for the query
     */
    public String getQueryType();


    /**
     * Get the original query as a string.
     *
     * @return the original query
     */
    public String getRawQuery();


    /**
     * Get the parsed query as an abstract syntax tree.
     *
     * @return the parsed query as an abstract syntax tree.
     */
    public T getParsedQuery();

} // interface SRUQuery
