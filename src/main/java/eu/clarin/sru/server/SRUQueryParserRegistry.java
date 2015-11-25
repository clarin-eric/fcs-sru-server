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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Class for registering query parsers. The query parser for CQL will be
 * automatically registered.
 *
 * @see SRUQueryParser
 */
public class SRUQueryParserRegistry {
    private CopyOnWriteArrayList<SRUQueryParser<?>> queryParsers =
            new CopyOnWriteArrayList<SRUQueryParser<?>>();


    /**
     * Constructor.
     */
    public SRUQueryParserRegistry() {
        queryParsers.add(new CQLQueryParser());
        queryParsers.add(new SearchTermsQueryParser());
    }


    /**
     * Register a new query parser
     *
     * @param parser
     *            the query parser instance to be registered
     * @throws SRUConfigException
     *             if a query parser for the same query type was already
     *             registered
     */
    public void registerQueryParser(SRUQueryParser<?> parser)
            throws SRUConfigException {
        if (parser == null) {
            throw new NullPointerException("parser == null");
        }
        if (parser.getQueryType() == null) {
            throw new NullPointerException("parser.getQueryType() == null");
        }
        synchronized (this) {
            final String queryType = parser.getQueryType();
            for (SRUQueryParser<?> queryParser : queryParsers) {
                if (queryType.equals(queryParser.getQueryType())) {
                    throw new SRUConfigException(
                            "query parser for queryType '" + queryType +
                                    "' is already registered");
                }
            } // for
            queryParsers.add(parser);
        } // synchronized (this)
    }


    /**
     * Find a query parser by query type.
     *
     * @param queryType
     *            the query type to search for
     * @return the matching {@link SRUQueryParser} instance or <code>null</code>
     *         if no matching parser was found.
     */
    public SRUQueryParser<?> findQueryParser(String queryType) {
        if (queryType == null) {
            throw new NullPointerException("queryType == null");
        }
        for (SRUQueryParser<?> queryParser : queryParsers) {
            if (queryType.equals(queryParser.getQueryType())) {
                return queryParser;
            }
        } // for
        return null;
    }


    public List<SRUQueryParser<?>> getQueryParsers() {
        return Collections.unmodifiableList(queryParsers);
    }

} // class SRUQueryParserRegistry
