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

/**
 * An exception raised, if something went wrong processing the request. For
 * diagnostic codes, see constants in {@link SRUConstants}.
 *
 * @see SRUConstants
 */
@SuppressWarnings("serial")
public class SRUException extends Exception {
    private final String uri;
    private final String details;


    /**
     * Constructor.
     *
     * @param uri
     *            the diagnostic's identifying URI
     * @param details
     *            diagnostic details or <code>null</code>
     * @param message
     *            diagnostic message or <code>null</code>
     * @param cause
     *            the cause of the error or <code>null</code>
     */
    public SRUException(String uri, String details, String message,
            Throwable cause) {
        super(message, cause);
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        uri = uri.trim();
        if (uri.isEmpty()) {
            throw new IllegalArgumentException("uri is empty");
        }
        this.uri     = uri;
        this.details = details;
    }


    /**
     * Constructor.
     *
     * @param uri
     *            the diagnostic's identifying URI
     * @param details
     *            diagnostic details or <code>null</code>
     * @param message
     *            diagnostic message or <code>null</code>
     */
    public SRUException(String uri, String details, String message) {
        this(uri, details, message, null);
    }


    /**
     * Constructor.
     *
     * @param uri
     *            the diagnostic's identifying URI
     * @param message
     *            diagnostic message or <code>null</code>
     * @param cause
     *            the cause of the error or <code>null</code>
     */
    public SRUException(String uri, String message, Throwable cause) {
        this(uri, null, message, cause);
    }


    /**
     * Constructor.
     *
     * @param uri
     *            the diagnostic's identifying URI
     * @param message
     *            diagnostic message or <code>null</code>
     */
    public SRUException(String uri, String message) {
        this(uri, null, message, null);
    }


    /**
     * Constructor.
     *
     * @param uri
     *            the diagnostic's identifying URI
     * @param cause
     *            the cause of the error or <code>null</code>
     */
    public SRUException(String uri, Throwable cause) {
        this(uri, null, null, cause);
    }


    /**
     * Constructor.
     *
     * @param uri
     *            the diagnostic's identifying URI
     */
    public SRUException(String uri) {
        this(uri, null, null, null);
    }


    /**
     * Create a SRU diagnostic from this exception.
     *
     * @return a {@link SRUDiagnostic} instance
     */
    public SRUDiagnostic getDiagnostic() {
        return new SRUDiagnostic(uri, details, this.getMessage());
    }

} // class SRUException
