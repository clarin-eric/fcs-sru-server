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
package eu.clarin.sru.server.utils;

import java.util.Map;

import javax.servlet.ServletContext;

import eu.clarin.sru.server.SRUAuthenticationInfoProvider;
import eu.clarin.sru.server.SRUConfigException;

public interface SRUAuthenticationInfoProviderFactory {
    /**
     * Create a authentication info provider.
     *
     * @param context
     * @param params
     * @return
     * @throws SRUConfigException
     */
    public SRUAuthenticationInfoProvider createAuthenticationInfoProvider(
            ServletContext context, Map<String, String> params)
            throws SRUConfigException;

} // interface SRUAuthenticationProviderFactory
