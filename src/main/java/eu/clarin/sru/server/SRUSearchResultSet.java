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

import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * A result set of a <em>searchRetrieve</em> operation. It allows to iterate
 * over the result set and provides a method to serialize the record in the
 * requested format.
 * 
 * <p>This class needs to be implemented for the target data source.</p>
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
     * be 0.
     *
     * @return the total number of results or 0 if the query failed
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
     * The result set id of this result. the default implementation
     * returns <code>null</code>.
     *
     * @return the result set id or <code>null</code> if not
     *         applicable for this result
     */
    public String getResultSetId() {
        return null;
    }


    /**
     * The idle time for this result. The default implementation
     * returns <code>-1</code>.
     *
     * @return the result set idle time or <code>-1</code> if not
     *         applicable for this result
     */
    public int getResultSetIdleTime() {
        return -1;
    }


    /**
     * The record schema identifier in which the records are
     * returned (recordSchema parameter).
     *
     * @return the record schema identifier
     */
    public abstract String getRecordSchemaIdentifier();


    /**
     * Returns true if the search result set has more records. (In other words,
     * returns <code>true</code> if <code>nextRecord()</code> would move the
     * internal result pointer to a new record instead of throwing an
     * exception.)
     *
     * @return <code>true</code> if the search result set has more records,
     *         <code>false</code> otherwise
     */
    public abstract boolean hasMoreRecords();


    /**
     * Move to the next record.
     *
     * @throws NoSuchElementException
     *             result set has no more records
     */
    public abstract void nextRecord();


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
