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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A registry to keep track of registered {@link SRUQueryParser} to be used by
 * the {@link SRUServer}.
 *
 * @see SRUQueryParser
 */
public class SRUQueryParserRegistry {
    private final List<SRUQueryParser<?>> parsers;


    /**
     * Constructor.
     */
    private SRUQueryParserRegistry(List<SRUQueryParser<?>> parsers) {
        if (parsers == null) {
            throw new NullPointerException("parsers == null");
        }
        if (parsers.isEmpty()) {
            throw new IllegalArgumentException("parsers is empty!");
        }
        this.parsers = Collections.unmodifiableList(parsers);
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
        return findParser(parsers, queryType);
    }


    /**
     * Get a list of all registered query parsers.
     *
     * @return a list of registered query parsers
     */
    public List<SRUQueryParser<?>> getQueryParsers() {
        return parsers;
    }


    /**
     * Builder for creating {@link SRUQueryParserRegistry} instances.
     */
    public static class Builder {
        private final List<SRUQueryParser<?>> parsers =
                new ArrayList<SRUQueryParser<?>>();


        /**
         * Constructor.
         *
         * @param registerDefaults
         *            if <code>true</code>, register SRU/CQL standard query
         *            parsers (queryType <em>cql</em> and <em>searchTerms</em>),
         *            otherwise do nothing
         */
        public Builder(boolean registerDefaults) {
            if (registerDefaults) {
                registerDefaults();
            }
        }


        /**
         * Constructor. Automaticaly registers registers SRU/CQL standard query
         * parsers (queryType <em>cql</em> and <em>searchTerms</em>).
         */
        public Builder() {
            this(true);
        }


        public Builder registerDefaults() {
            if (findParser(parsers, SRUConstants.SRU_QUERY_TYPE_CQL) == null) {
                try {
                    register(new CQLQueryParser());
                } catch (SRUConfigException e) {
                    /* IGNORE */
                }
            }
            if (findParser(parsers, SRUConstants.SRU_QUERY_TYPE_SEARCH_TERMS) == null) {
                try {
                    register(new SearchTermsQueryParser());
                } catch (SRUConfigException e) {
                    /* IGNORE */
                }
            }
            return this;
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
        public Builder register(SRUQueryParser<?> parser) throws SRUConfigException {
            if (parser == null) {
                throw new NullPointerException("parser == null");
            }
            if (parser.getQueryType() == null) {
                throw new NullPointerException("parser.getQueryType() == null");
            }

            // duplicate-save add ...
            if (findParser(parsers, parser.getQueryType()) == null) {
                parsers.add(parser);
            } else {
                throw new SRUConfigException("query parser for queryType '" +
                        parser.getQueryType() + "' is already registered");
            }

            return this;
        }


        /**
         * Create a configured {@link SRUQueryParserRegistry} instance from this
         * builder.
         *
         * @return a {@link SRUQueryParserRegistry} instance
         */
        public SRUQueryParserRegistry build() {
            return new SRUQueryParserRegistry(parsers);
        }
    }


    private static final SRUQueryParser<?> findParser(
            List<SRUQueryParser<?>> parsers, String queryType) {
        for (SRUQueryParser<?> parser : parsers) {
            if (queryType.equals(parser.getQueryType())) {
                return parser;
            }
        }
        return null;
    }

} // class SRUQueryParserRegistry
