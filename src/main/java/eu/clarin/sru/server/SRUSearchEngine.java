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
 * Interface for connecting the SRU protocol implementation to an actual search
 * engine.
 * <p>
 * Implementing the
 * {@link #explain(SRUServerConfig, SRURequest, SRUDiagnosticList)} and
 * {@link #scan(SRUServerConfig, SRURequest, SRUDiagnosticList)} is optional,
 * but implementing
 * {@link #search(SRUServerConfig, SRURequest, SRUDiagnosticList)} is mandatory.
 * </p>
 * <p>
 * The implementation of these methods <em>must</em> be thread-safe.
 * </p>
 */
public interface SRUSearchEngine {

    /**
     * Handle an <em>explain</em> operation. Implementing this method is
     * optional, but is required, if the <em>writeExtraResponseData</em> block
     * of the SRU response needs to be filled. The arguments for this operation
     * are provides by the {@link SRURequest} object.
     * <p>
     * The implementation of this method <em>must</em> be thread-safe.
     * </p>
     *
     * @param config
     *            the <code>SRUEndpointConfig</code> object that contains the
     *            endpoint configuration
     * @param request
     *            the <code>SRURequest</code> object that contains the request
     *            made to the endpoint
     * @param diagnostics
     *            the <code>SRUDiagnosticList</code> object for storing
     *            non-fatal diagnostics
     * @return a <code>SRUExplainResult</code> object or <code>null</code> if
     *         the search engine does not want to provide
     *         <em>writeExtraResponseData</em>
     * @throws SRUException
     *             if an fatal error occurred
     * @see SRUExplainResult
     */
    public SRUExplainResult explain(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException;


    /**
     * Handle a <em>searchRetrieve</em> operation. Implementing this method is
     * mandatory. The arguments for this operation are provides by the
     * {@link SRURequest} object.
     * <p>
     * The implementation of this method <em>must</em> be thread-safe.
     * </p>
     *
     * @param config
     *            the <code>SRUEndpointConfig</code> object that contains the
     *            endpoint configuration
     * @param request
     *            the <code>SRURequest</code> object that contains the request
     *            made to the endpoint
     * @param diagnostics
     *            the <code>SRUDiagnosticList</code> object for storing
     *            non-fatal diagnostics
     * @return a <code>SRUSearchResultSet</code> object
     * @throws SRUException
     *             if an fatal error occurred
     * @see SRURequest
     * @see SRUExplainResult
     */
    public SRUSearchResultSet search(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException;


    /**
     * Handle a <em>scan</em> operation. Implementing this method is optional.
     * If you don't need to handle the <em>scan</em> operation, just return
     * <code>null</code> and the SRU server will return the appropiate
     * diagnostic to the client. The arguments for this operation are provides
     * by the {@link SRURequest} object.
     * <p>
     * The implementation of this method <em>must</em> be thread-safe.
     * </p>
     *
     * @param config
     *            the <code>SRUEndpointConfig</code> object that contains the
     *            endpoint configuration
     * @param request
     *            the <code>SRURequest</code> object that contains the request
     *            made to the endpoint
     * @param diagnostics
     *            the <code>SRUDiagnosticList</code> object for storing
     *            non-fatal diagnostics
     * @return a <code>SRUScanResultSet</code> object or <code>null</code> if
     *         this operation is not supported by this search engine
     * @throws SRUException
     *             if an fatal error occurred
     * @see SRUExplainResult
     */
    public SRUScanResultSet scan(SRUServerConfig config, SRURequest request,
            SRUDiagnosticList diagnostics) throws SRUException;

} // interface SRUSearchEngine
