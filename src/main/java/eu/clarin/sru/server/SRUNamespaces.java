/**
 * This software is copyright (c) 2011-2016 by
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
 * Interface for decoupling SRU namespaces from implementation to allow to
 * support SRU 1.1/1.2 and SRU 2.0.
 */
public interface SRUNamespaces {

    /**
     * The namespace URI for encoding <em>explain</em> and
     * <em>searchRetrieve</em> operation responses.
     * 
     * @return the namespace URI for encoding <em>explain</em> and
     *         <em>searchRetrieve</em>
     */
    public String getResponseNS();


    /**
     * The namespace prefix for encoding <em>explain</em> and
     * <em>searchRetrieve</em> operation responses.
     * 
     * @return the namespace prefix for encoding <em>explain</em> and
     *         <em>searchRetrieve</em>
     */
    public String getResponsePrefix();


    /**
     * The namespace URI for encoding <em>scan</em> operation responses.
     * 
     * @return the namespace URI for encoding <em>scan</em>
     */
    public String getScanNS();


    /**
     * The namespace prefix for encoding <em>scan</em> operation responses.
     * 
     * @return the namespace prefix for encoding <em>scan</em>
     */
    public String getScanPrefix();


    /**
     * The namespace URI for encoding SRU diagnostics.
     * 
     * @return the namespace URI for encoding SRU diagnostics
     */
    public String getDiagnosticNS();


    /**
     * The namespace prefix for encoding SRU diagnostics.
     * 
     * @return the namespace prefix for encoding SRU diagnostics
     */
    public String getDiagnosticPrefix();


    /**
     * The namespace URI for encoding explain record data fragments.
     * 
     * @return the namespace URI for encoding explain record data fragments
     */
    public String getExplainNS();


    /**
     * The namespace prefix for encoding explain record data fragments.
     * 
     * @return the namespace prefix for encoding explain record data fragments
     */
    public String getExplainPrefix();


    /**
     * The namespace URI for encoding XCQL fragments
     * 
     * @return the namespace URI for encoding XCQL fragments
     */
    public String getXcqlNS();

} // interface SRUNamespaces
