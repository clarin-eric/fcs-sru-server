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

public abstract class SRUQueryBase<T> implements SRUQuery<T> {
    protected final String rawQuery;
    protected final T parsedQuery;


    protected SRUQueryBase(String rawQuery, T parsedQuery) {
        if (rawQuery == null) {
            throw new NullPointerException("rawQuery == null");
        }
        this.rawQuery = rawQuery;
        if (parsedQuery == null) {
            throw new NullPointerException("parsedQuery == null");
        }
        this.parsedQuery = parsedQuery;
    }


    @Override
    public String getRawQuery() {
        return rawQuery;
    }


    @Override
    public T getParsedQuery() {
        return parsedQuery;
    }

} // abstract class SRUQueryBase
