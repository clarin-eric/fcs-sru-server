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
 * A result set of an <em>explain</em> operation. A database implementation may
 * use it implement extensions to the SRU protocol, i.e. providing
 * extraResponseData.
 *
 * <p>
 * This class needs to be implemented for the target data source.
 * </p>
 *
 * @see <a href="http://www.loc.gov/standards/sru/specs/explain.html">SRU
 *      Explain Operation </a>
 */
public abstract class SRUExplainResult extends SRUAbstractResult {

    /**
     * Constructor.
     *
     * @param diagnostics
     *            an instance of a SRUDiagnosticList
     * @see SRUDiagnosticList
     */
    protected SRUExplainResult(SRUDiagnosticList diagnostics) {
        super(diagnostics);
    }

} // class SRUExplainResult
