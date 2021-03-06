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

import javax.servlet.http.HttpServletRequest;

import org.z3950.zing.cql.CQLNode;


/**
 * Provides information about a SRU request.
 */
public interface SRURequest {
    /**
     * Get the authentication information that was extracted from the the
     * request.
     *
     * @return the request authentication or <code>null</code>
     */
    public SRUAuthenticationInfo getAuthentication();


    /**
     * Get the subject of the request.
     *
     * @return the subject of the request or <code>null</code>
     */
    public String getAuthenticationSubject();


    /**
     * Get the <em>operation</em> parameter of this request. Available for
     * <em>explain</em>, <em>searchRetrieve</em> and <em>scan</em> requests.
     *
     * @return the operation
     * @see SRUOperation
     */
    public SRUOperation getOperation();


    /**
     * Get the <em>version</em> parameter of this request. Available for
     * <em>explain</em>, <em>searchRetrieve</em> and <em>scan</em> requests.
     *
     * @return the version
     * @see SRUVersion
     */
    public SRUVersion getVersion();


    /**
     * Check if this request is of a specific version.
     *
     * @param version
     *            the version to check
     * @return <code>true</code> if this request is in the requested version,
     *         <code>false</code> otherwise
     * @throws NullPointerException
     *             if version is <code>null</code>
     */
    public boolean isVersion(SRUVersion version);


    /**
     * Check if version of this request is at least <em>min</em> and at most
     * <em>max</em>.
     *
     * @param min
     *            the minimum version
     * @param max
     *            the maximum version
     * @return <code>true</code> if this request is in the provides version,
     *         <code>false</code> otherwise
     * @throws NullPointerException
     *             if minimum or maximum <code>null</code>
     * @throws IllegalArgumentException
     *             if minimum is larger the maximum
     */
    public boolean isVersion(SRUVersion min, SRUVersion max);


    /**
     * Get the <em>recordXmlEscpaing</em> (SRU 2.0) or <em>recordPacking</em>
     * (SRU 1.1 and SRU 1.2) parameter of this request. Only available for
     * <em>explain</em> and <em>searchRetrieve</em> requests.
     *
     * @return the record XML escaping method
     * @see SRURecordXmlEscaping
     */
    public SRURecordXmlEscaping getRecordXmlEscaping();


    /**
     * Get the <em>recordPacking</em> (SRU 2.0) parameter of this request. Only
     * available for <em>searchRetrieve</em> requests.
     *
     * @return the record packing method
     * @see SRURecordPacking
     */
    public SRURecordPacking getRecordPacking();


    /**
     * Get the <em>query</em> parameter of this request. Only available for
     * <em>searchRetrieve</em> requests.
     *
     * @return an {@link SRUQuery} instance tailored for the used queryType or
     *         <code>null</code> if not a <em>searchRetrieve</em> request
     */
    public SRUQuery<?> getQuery();


    /**
     * Get the <em>query</em> parameter of this request. Only available for
     * <em>searchRetrieve</em> requests. This convenience method tried to cast
     * the query object to the supplied type.
     *
     * @param type
     *            Class representing the return type to convert the query to
     * @param <T>
     *            Class of the returned type
     * @return an {@link SRUQuery} instance tailored for the used queryType or
     *         <code>null</code> if not a <em>searchRetrieve</em> request
     * @throws ClassCastException
     *             if conversion is not supported, type is <code>null</code> or
     *             another error occurs
     */
    public <T extends SRUQuery<?>> T getQuery(Class<T> type);


    /**
     * Get the <em>queryType</em> parameter of this request. Only available for
     * <em>searchRetrieve</em> requests.
     *
     * @return the queryType of the parsed query or <code>null</code> if not a
     *         <em>searchRetrieve</em> request
     */
    public String getQueryType();


    /**
     * Check if the request was made with the given queryType. Only available
     * for <em>searchRetrieve</em> requests.
     *
     * @param queryType
     *            the queryType to compare with
     * @return <code>true</code> if the queryType matches, <code>false</code>
     *         otherwise
     */
    public boolean isQueryType(String queryType);


    /**
     * Get the <em>startRecord</em> parameter of this request. Only available
     * for <em>searchRetrieve</em> requests. If the client did not provide a
     * value for the request, it is set to <code>1</code>.
     *
     * @return the number of the start record
     */
    public int getStartRecord();


    /**
     * Get the <em>maximumRecords</em> parameter of this request. Only available
     * for <em>searchRetrieve</em> requests. If no value was supplied with the
     * request, the server will automatically set a default value.
     *
     * @return the maximum number of records
     */
    public int getMaximumRecords();


    /**
     * Get the record schema identifier derived from the <em>recordSchema</em>
     * parameter of this request. Only available for <em>searchRetrieve</em>
     * requests. If the request was send with the short record schema name,
     * it will automatically expanded to the record schema identifier.
     *
     * @return the record schema identifier or <code>null</code> if no
     *         <em>recordSchema</em> parameter was supplied for this request
     */
    public String getRecordSchemaIdentifier();


    /**
     * Get the <em>recordXPath</em> parameter of this request. Only available
     * for <em>searchRetrieve</em> requests and version 1.1 requests.
     *
     * @return the record XPath or <code>null</code> of no value was supplied
     *         for this request
     */
    public String getRecordXPath();


    /**
     * Get the <em>resultSetTTL</em> parameter of this request. Only available
     * for <em>searchRetrieve</em> requests.
     *
     * @return the result set TTL or <code>-1</code> if no value was supplied
     *         for this request
     */
    public int getResultSetTTL();


    /**
     * Get the <em>sortKeys</em> parameter of this request. Only available for
     * <em>searchRetrieve</em> requests and version 1.1 requests.
     *
     * @return the record XPath or <code>null</code> of no value was supplied
     *         for this request
     */
    public String getSortKeys();


    /**
     * Get the <em>scanClause</em> parameter of this request. Only available for
     * <em>scan</em> requests.
     *
     * @return the parsed scan clause or <code>null</code> if not a
     *         <em>scan</em> request
     */
    public CQLNode getScanClause();


    /**
     * Get the <em>responsePosition</em> parameter of this request. Only
     * available for <em>scan</em> requests. If the client did not provide
     * a value for the request, it is set to <code>1</code>.
     *
     * @return the response position
     */
    public int getResponsePosition();


    /**
     * Get the <em>maximumTerms</em> parameter of this request. Available for
     * any type of request.
     *
     * @return the maximum number of terms or <code>-1</code> if no value was
     *         supplied for this request
     */
    public int getMaximumTerms();


    /**
     * Get the <em>stylesheet</em> parameter of this request. Available for
     * <em>explain</em>, <em>searchRetrieve</em> and <em>scan</em> requests.
     *
     * @return the stylesheet or <code>null</code> if no value was supplied for
     *         this request
     */
    public String getStylesheet();


    /**
     * Get the <em>renderBy</em> parameter of this request.
     *
     * @return the renderBy parameter or <code>null</code> if no value was
     *         supplied for this request
     */
    public SRURenderBy getRenderBy();


    /**
     * (SRU 2.0) The request parameter <em>responseType</em>, paired with the
     * Internet media type specified for the response (via either the httpAccept
     * parameter or http accept header) determines the schema for the response.
     *
     * @return the value of the responeType request parameter or
     *         <code>null</code> if no value was supplied for this request
     */
    public String getResponeType();


    /**
     * (SRU 2.0) The request parameter <em>httpAccept</em> may be supplied to
     * indicate the preferred format of the response. The value is an Internet
     * media type.
     *
     * @return the value of the httpAccept request parameter or
     *         <code>null</code> if no value was supplied for
     */
    public String getHttpAccept();


    /**
     * Get the protocol schema which was used of this request. Available for
     * <em>explain</em>, <em>searchRetrieve</em> and <em>scan</em> requests.
     *
     * @return the protocol scheme
     */
    public String getProtocolScheme();


    /**
     * Get the names of extra parameters of this request. Available for
     * <em>explain</em>, <em>searchRetrieve</em> and <em>scan</em> requests.
     *
     * @return a possibly empty list of parameter names
     */
    public List<String> getExtraRequestDataNames();


    /**
     * Get the value of an extra parameter of this request. Available for
     * <em>explain</em>, <em>searchRetrieve</em> and <em>scan</em> requests.
     *
     * @param name
     *            name of the extra parameter. Must be prefixed with
     *            <code>x-</code>
     * @return the value of the parameter of <code>null</code> of extra
     *         parameter with that name exists
     * @throws NullPointerException
     *             if <code>name</code> is null
     * @throws IllegalArgumentException
     *             if <code>name</code> does not start with <code>x-</code>
     */
    public String getExtraRequestData(String name);


    /**
     * Get the raw client request information from the Servlet container.
     *
     * @return the Servlet request
     */
    public HttpServletRequest getServletRequest();

} // interface SRURequest
