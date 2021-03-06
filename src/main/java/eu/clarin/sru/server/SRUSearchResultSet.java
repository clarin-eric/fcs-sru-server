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

import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * A result set of a <em>searchRetrieve</em> operation. It it used to iterate
 * over the result set and provides a method to serialize the record in the
 * requested format.
 * <p>
 * A <code>SRUSearchResultSet</code> object maintains a cursor pointing to its
 * current record. Initially the cursor is positioned before the first record.
 * The <code>next</code> method moves the cursor to the next record, and because
 * it returns <code>false</code> when there are no more records in the
 * <code>SRUSearchResultSet</code> object, it can be used in a
 * <code>while</code> loop to iterate through the result set.
 * </p>
 * <p>
 * This class needs to be implemented for the target search engine.
 * </p>
 *
 * @see <a href="http://www.loc.gov/standards/sru/specs/search-retrieve.html">
 *      SRU Search Retrieve Operation</a>
 */
public abstract class SRUSearchResultSet extends SRUAbstractResult {

    /**
     * Constructor.
     *
     * @param diagnostics
     *            an instance of a SRUDiagnosticList
     * @see SRUDiagnosticList
     */
    protected SRUSearchResultSet(SRUDiagnosticList diagnostics) {
        super(diagnostics);
    }


    /**
     * The number of records matched by the query. If the query fails this must
     * be 0. If the search engine cannot determine the total number of matched
     * by a query, it must return -1.
     *
     * @return the total number of results or 0 if the query failed or -1 if the
     *         search engine cannot determine the total number of results
     */
    public abstract int getTotalRecordCount();


    /**
     * The number of records matched by the query but at most as the number of
     * records requested to be returned (maximumRecords parameter). If the query
     * fails this must be 0.
     *
     * @return the number of results or 0 if the query failed
     */
    public abstract int getRecordCount();


    /**
     * The result set id of this result. the default implementation returns
     * <code>null</code>.
     *
     * @return the result set id or <code>null</code> if not applicable for this
     *         result
     */
    public String getResultSetId() {
        return null;
    }


    /**
     * The result set time to live. In SRU 2.0 it will be serialized as
     * <code>&lt;resultSetTTL&gt;</code> element; in SRU 1.2 as
     * <code>&lt;resultSetIdleTime&gt;</code> element.The default implementation
     * returns <code>-1</code>.
     *
     * @return the result set time to live or <code>-1</code> if not applicable for
     *         this result
     */
    public int getResultSetTTL() {
        return -1;
    }


    /**
     * (SRU 2.0) Indicate the accuracy of the result count reported by total
     * number of records that matched the query. Default implementation returns
     * <code>null</code>.
     *
     * @see SRUResultCountPrecision
     * @return the result count precision or <code>null</code> if not applicable
     *         for this result
     */
    public SRUResultCountPrecision getResultCountPrecision() {
        return null;
    }


    /**
     * The record schema identifier in which the records are returned
     * (recordSchema parameter).
     *
     * @return the record schema identifier
     */
    public abstract String getRecordSchemaIdentifier();


    /**
     * Moves the cursor forward one record from its current position. A
     * <code>SRUSearchResultSet</code> cursor is initially positioned before the
     * first record; the first call to the method <code>next</code> makes the
     * first record the current record; the second call makes the second record
     * the current record, and so on.
     * <p>
     * When a call to the <code>next</code> method returns <code>false</code>,
     * the cursor is positioned after the last record.
     * </p>
     *
     * @return <code>true</code> if the new current record is valid;
     *         <code>false</code> if there are no more records
     * @throws SRUException
     *             if an error occurred while fetching the next record
     */
    public abstract boolean nextRecord() throws SRUException;


    /**
     * An identifier for the current record by which it can unambiguously be
     * retrieved in a subsequent operation.
     *
     * @return identifier for the record or <code>null</code> of none is
     *         available
     * @throws NoSuchElementException
     *             result set is past all records
     */
    public abstract String getRecordIdentifier();


    /**
     * Get surrogate diagnostic for current record. If this method returns a
     * diagnostic, the writeRecord method will not be called. The default
     * implementation returns <code>null</code>.
     *
     * @return a surrogate diagnostic or <code>null</code>
     */
    public SRUDiagnostic getSurrogateDiagnostic() {
        return null;
    }


    /**
     * Serialize the current record in the requested format.
     *
     * @param writer
     *            the {@link XMLStreamException} instance to be used
     * @throws XMLStreamException
     *             an error occurred while serializing the result
     * @throws NoSuchElementException
     *             result set past all records
     * @see #getRecordSchemaIdentifier()
     */
    public abstract void writeRecord(XMLStreamWriter writer)
            throws XMLStreamException;


    /**
     * Check, if extra record data should be serialized for the current record.
     * The default implementation returns <code>false</code>.
     *
     * @return <code>true</code> if the record has extra record data
     * @throws NoSuchElementException
     *             result set is already advanced past all records
     * @see #writeExtraResponseData(XMLStreamWriter)
     */
    public boolean hasExtraRecordData() {
        return false;
    }


    /**
     * Serialize extra record data for the current record. A no-op default
     * implementation is provided for convince.
     *
     * @param writer
     *            the {@link XMLStreamException} instance to be used
     * @throws XMLStreamException
     *             an error occurred while serializing the result extra data
     * @throws NoSuchElementException
     *             result set past already advanced past all records
     * @see #hasExtraRecordData()
     */
    public void writeExtraRecordData(XMLStreamWriter writer)
            throws XMLStreamException {
    }

} // class SRUSearchResultSet
