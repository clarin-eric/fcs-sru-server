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
 * (SRU 2.0) Indicate the accuracy of the result count reported by total
 * number of records that matched the query.
 */
public enum SRUResultCountPrecision {
    /**
     * The server guarantees that the reported number of records is accurate.
     */
    EXACT,
    /**
     * The server has no idea what the result count is, and does not want to
     * venture an estimate.
     */
    UNKNOWN,
    /**
     * The server does not know the result set count, but offers an estimate.
     */
    ESTIMATE,
    /**
     * The value supplied is an estimate of the maximum possible count that the
     * result set will attain.
     */
    MAXIMUM,
    /**
     * The server does not know the result count but guarantees that it is at
     * least this large.
     */
    MINIMUM,
    /**
     * The value supplied is an estimate of the count at the time the response
     * was sent, however the result set may continue to grow.
     */
    CURRENT
} // enum SRUResultCountPrecision
