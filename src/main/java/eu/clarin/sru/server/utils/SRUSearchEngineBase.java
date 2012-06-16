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
package eu.clarin.sru.server.utils;

import java.util.Map;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUExplainResult;
import eu.clarin.sru.server.SRURequest;
import eu.clarin.sru.server.SRUScanResultSet;
import eu.clarin.sru.server.SRUSearchEngine;
import eu.clarin.sru.server.SRUSearchResultSet;
import eu.clarin.sru.server.SRUServerConfig;


/**
 * Base class required for an {@link SRUSearchEngine} implementation to be used
 * with the {@link SRUServerServlet} Servlet.
 */
public abstract class SRUSearchEngineBase implements SRUSearchEngine {

    public SRUSearchEngineBase() {
    }


    @Override
    public SRUExplainResult explain(SRUServerConfig config, SRURequest request,
            SRUDiagnosticList diagnostics) throws SRUException {
        return null;
    }


    @Override
    public SRUSearchResultSet search(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException {
        return null;
    }


    @Override
    public abstract SRUScanResultSet scan(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException;


    /**
     * Initialize the search engine.
     *
     * @param config
     *            the {@link SRUServerConfig} object for this search engine
     * @param params
     *            additional parameters gathered from the Servlet configuration
     *            and Servlet context.
     * @throws SRUConfigException an error occurred during initialization of the search engine
     */
    public void init(SRUServerConfig config, Map<String, String> params)
            throws SRUConfigException {
    }


    /**
     * Destroy the search engine. Use this method for any cleanup the search
     * engine needs to perform upon termination.
     */
    public void destroy() {
    }

} // abstract class SRUSearchEngineBase
