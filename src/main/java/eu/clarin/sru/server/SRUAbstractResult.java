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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * Base class for SRU responses.
 */
abstract class SRUAbstractResult {
    private final SRUDiagnosticList diagnostics;


    SRUAbstractResult(SRUDiagnosticList diagnosticList) {
        if (diagnosticList == null) {
            throw new NullPointerException("Implementation error: "
                    + "diagnosticList must not be null!");
        }
        this.diagnostics = diagnosticList;
    }


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
    protected final void addDiagnostic(int code, String details, String message) {
        diagnostics.addDiagnostic(code, details, message);
    }


    /**
     * Add a non surrogate diagnostic to the response.
     * 
     * @param code
     *            numerical diagnostic code
     * @param details
     *            supplementary information available, often in a format
     *            specified by the diagnostic or <code>null</code>
     */
    protected final void addDiagnostic(int code, String details) {
        addDiagnostic(code, details, null);
    }


    /**
     * Add a non surrogate diagnostic to the response.
     * 
     * @param code
     *            numerical diagnostic code
     */
    protected final void addDiagnostic(int code) {
        addDiagnostic(code, null, null);
    }


    /**
     * Check, if extra response data should be serialized for this request.
     * Default implementation is provided for convince and always returns
     * <code>false</code>.
     * 
     * @return <code>true</code> if extra response data should be serialized.
     * @see #writeExtraResponseData(XMLStreamWriter)
     */
    public boolean hasExtraResponseData() {
        return false;
    }


    /**
     * Serialize extra response data for this request.
     * 
     * @param writer
     *            the {@link XMLStreamException} instance to be used
     * @throws XMLStreamException
     *             an error occurred while serializing the result
     * @see #hasExtraResponseData()
     */
    public void writeExtraResponseData(XMLStreamWriter writer)
            throws XMLStreamException {
    }


    /**
     * Release this result and free any associated resources.
     * <p>
     * This method <strong>must not</strong> throw any exceptions
     * </p>
     * <p>
     * Calling the method <code>close</code> on a result object that is already
     * closed is a no-op.
     * </p>
     */
    public void close() {
    }

} // abstract class SRUAbstractResult
