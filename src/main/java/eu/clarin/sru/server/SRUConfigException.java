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

/**
 * An exception raised, if some error occurred with the SRUServer configuration.
 */
@SuppressWarnings("serial")
public class SRUConfigException extends Exception {

    /**
     * Constructor.
     * 
     * @param msg
     *            a message
     */
    public SRUConfigException(String msg) {
        super(msg);
    }


    /**
     * Constructor.
     * 
     * @param msg
     *            a message
     * @param cause
     *            the cause of the error
     */
    public SRUConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }

} // class SRUConfigException
