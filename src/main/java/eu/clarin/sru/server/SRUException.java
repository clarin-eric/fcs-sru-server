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
 * An exception raised, if something went wrong processing the request. For
 * diagnostic codes, see constants in {@link SRUConstants}.
 *
 * @see SRUConstants
 */
@SuppressWarnings("serial")
public class SRUException extends Exception {
    private final int code;
    private final String details;


    /**
     * Constructor.
     *
     * @param code
     *            the diagnostic code
     * @param details
     *            diagnostic details or <code>null</code>
     * @param message
     *            diagnostic message or <code>null</code>
     * @param cause
     *            the cause of the error or <code>null</code>
     */
    public SRUException(int code, String details, String message,
            Throwable cause) {
        super(message, cause);
        this.code    = code;
        this.details = details;
    }


    /**
     * Constructor.
     *
     * @param code
     *            the diagnostic code
     * @param details
     *            diagnostic details or <code>null</code>
     * @param message
     *            diagnostic message or <code>null</code>
     */
    public SRUException(int code, String details, String message) {
        this(code, details, message, null);
    }


    /**
     * Constructor.
     *
     * @param code
     *            the diagnostic code
     * @param message
     *            diagnostic message or <code>null</code>
     * @param cause
     *            the cause of the error or <code>null</code>
     */
    public SRUException(int code, String message, Throwable cause) {
        this(code, null, message, cause);
    }


    /**
     * Constructor.
     *
     * @param code
     *            the diagnostic code
     * @param message
     *            diagnostic message or <code>null</code>
     */
    public SRUException(int code, String message) {
        this(code, null, message, null);
    }


    /**
     * Constructor.
     *
     * @param code
     *            the diagnostic code
     * @param cause
     *            the cause of the error or <code>null</code>
     */
    public SRUException(int code, Throwable cause) {
        this(code, null, null, cause);
    }


    /**
     * Constructor.
     *
     * @param code
     *            the diagnostic code
     */
    public SRUException(int code) {
        this(code, null, null, null);
    }


    /**
     * Create a SRU diagnostic from this exception.
     *
     * @return a {@link SRUDiagnostic} instance
     */
    public SRUDiagnostic getDiagnostic() {
        return new SRUDiagnostic(code, details, this.getMessage());
    }

} // class SRUException
