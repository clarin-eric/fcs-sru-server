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
 * SRU Record XML escaping.
 */
public enum SRURenderBy {
    /**
     * The client requests that the server simply return this URL in the
     * response, in the href attribute of the xml-stylesheet processing
     * instruction before the response xml
     */
    CLIENT,

    /**
     * The client requests that the server format the response according to the
     * specified stylesheet, assuming the default SRU response schema as input
     * to the stylesheet.
     */
    SERVER
} // enum SRURenderBy
