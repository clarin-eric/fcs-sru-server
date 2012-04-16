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
 * SRU version
 */
public enum SRUVersion {
    /**
     * SRU/CQL version 1.1
     */
    VERSION_1_1 {
        @Override
        int getVersionNumber() {
            return ((1 << 16) | 1);
        }

        @Override
        String getVersionString() {
            return "1.1";
        }
    },

    /**
     * SRU/CQL version 1.2
     */
    VERSION_1_2 {
        @Override
        int getVersionNumber() {
            return ((1 << 16) | 2);
        }

        @Override
        String getVersionString() {
            return "1.2";
        }
    };

    abstract int getVersionNumber();

    abstract String getVersionString();

} // enum SRUVersion
