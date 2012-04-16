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

@SuppressWarnings("serial")
public class SRUException extends Exception {
    private final int code;
    private final String details;


    public SRUException(int code, String details, String message, Throwable t) {
        super(message, t);
        this.code    = code;
        this.details = details;
    }


    public SRUException(int code, String details, String message) {
        this(code, details, message, null);
    }


    public SRUException(int code, String message, Throwable t) {
        this(code, null, message, t);
    }


    public SRUException(int code, String message) {
        this(code, null, message, null);
    }


    public SRUException(int code, Throwable t) {
        this(code, null, null, t);
    }


    public SRUException(int code) {
        this(code, null, null, null);
    }


    public SRUDiagnostic getDiagnostic() {
        return new SRUDiagnostic(code, details, this.getMessage());
    }

} // class SRUException
