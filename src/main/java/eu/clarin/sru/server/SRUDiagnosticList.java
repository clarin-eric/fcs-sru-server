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

/**
 * Container for non surrogate diagnostics for the request. The will be put in
 * the <em>diagnostics</em> part of the response.
 *
 * @see SRUConstants
 * @see SRUDiagnostic
 * @see <a href="http://www.loc.gov/standards/sru/specs/diagnostics.html"> SRU
 *      Diagnostics</a>
 * @see <a href="http://www.loc.gov/standards/sru/resources/diagnostics-list.html">
 *      SRU Diagnostics List</a>
 */
public interface SRUDiagnosticList {

    /**
     * Add a non surrogate diagnostic to the response.
     *
     * @param code
     *            numerical diagnostic code
     * @param details
     *            supplementary information available, often in a format
     *            specified by the diagnostic or <code>null</code>
     * @param message
     *            human readable message to display to the end user or
     *            <code>null</code>
     */
    public void addDiagnostic(int code, String details, String message);

} // interface DiagnosticList
