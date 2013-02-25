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

import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * A result set of a <em>scan</em> operation. It is used to iterate over the
 * term set and provides a method to serialize the terms.
 *
 * <p>
 * A <code>SRUScanResultSet</code> object maintains a cursor pointing to its
 * current term. Initially the cursor is positioned before the first term. The
 * <code>next</code> method moves the cursor to the next term, and because it
 * returns <code>false</code> when there are no more terms in the
 * <code>SRUScanResultSet</code> object, it can be used in a <code>while</code>
 * loop to iterate through the term set.
 * </p>
 * <p>
 * This class needs to be implemented for the target search engine.
 * </p>
 *
 * @see <a href="http://www.loc.gov/standards/sru/specs/scan.html"> SRU Scan
 *      Operation</a>
 */
public abstract class SRUScanResultSet extends SRUAbstractResult {
    /**
     * A flag to indicate the position of the term within the complete term
     * list.
     */
    public enum WhereInList {
        /**
         * The first term (<em>first</em>)
         */
        FIRST,

        /**
         * The last term (<em>last</em>)
         */
        LAST,

        /**
         * The only term (<em>only</em>)
         */
        ONLY,

        /**
         * Any other term (<em>inner</em>)
         */
        INNER
    }


    /**
     * Constructor.
     *
     * @param diagnostics
     *            an instance of a SRUDiagnosticList.
     * @see SRUDiagnosticList
     */
    protected SRUScanResultSet(SRUDiagnosticList diagnostics) {
        super(diagnostics);
    }


    /**
     * Moves the cursor forward one term from its current position. A result set
     * cursor is initially positioned before the first record; the first call to
     * the method <code>next</code> makes the first term the current term; the
     * second call makes the second term the current term, and so on.
     * <p>
     * When a call to the <code>next</code> method returns <code>false</code>,
     * the cursor is positioned after the last term.
     * </p>
     *
     * @return <code>true</code> if the new current term is valid;
     *         <code>false</code> if there are no more terms
     */
    public abstract boolean nextTerm();


    /**
     * Get the current term exactly as it appears in the index.
     *
     * @return current term
     */
    public abstract String getValue();


    /**
     * Get the number of records for the current term which would be matched if
     * the index in the request's <em>scanClause</em> was searched with the term
     * in the <em>value</em> field.
     *
     * @return a non-negative number of records
     */
    public abstract int getNumberOfRecords();


    /**
     * Get the string for the current term to display to the end user in place
     * of the term itself.
     *
     * @return display string or <code>null</code>
     */
    public abstract String getDisplayTerm();


    /**
     * Get the flag to indicate the position of the term within the complete
     * term list.
     *
     * @return position within term list or <code>null</code>
     */
    public abstract WhereInList getWhereInList();


    /**
     * Check, if extra term data should be serialized for the current term. A
     * default implementation is provided for convince and always returns
     * <code>false</code>.
     *
     * @return <code>true</code> if the term has extra term data
     * @throws NoSuchElementException
     *             term set is already advanced past all past terms
     * @see #writeExtraTermData(XMLStreamWriter)
     */
    public boolean hasExtraTermData() {
        return false;
    }


    /**
     * Serialize extra term data for the current term. A no-op default
     * implementation is provided for convince.
     *
     * @param writer
     *            the {@link XMLStreamException} instance to be used
     * @throws XMLStreamException
     *             an error occurred while serializing the term extra data
     * @throws NoSuchElementException
     *             result set already advanced past all terms
     * @see #hasExtraTermData()
     */
    public void writeExtraTermData(XMLStreamWriter writer)
            throws XMLStreamException {
    }

} // abstract class SRUScanResult
