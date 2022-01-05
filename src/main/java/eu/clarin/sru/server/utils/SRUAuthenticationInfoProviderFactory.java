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
