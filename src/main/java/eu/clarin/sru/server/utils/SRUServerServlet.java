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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.server.SRUAuthenticationInfoProvider;
import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUQueryParserRegistry;
import eu.clarin.sru.server.SRUServer;
import eu.clarin.sru.server.SRUServerConfig;


/**
 * A Servlet implementation, which provides an environment for running a
 * {@link SRUServer} in a Servlet container. Your search engine <b>must</b> use
 * {@link SRUSearchEngineBase} as base class.
 *
 * <p>
 * Add the following to the web.xml of your web applications web.xml to define a
 * SRU server. Of course, the value of the Servlet initialization parameter
 * "eu.clarin.sru.server.utils.sruServerSearchEngineClass" must be adapted to
 * match the name of your search engine implementation. Furthermore, you can
 * choose different url-pattern, to match your needs.
 * </p>
 *
 * <p>
 * For example, if your implementation of {@link SRUSearchEngineBase} is
 * "com.acme.MySearchEngine" and you want to map the Servlet to the URI "/sru"
 * the following snippet in your "web.xml" should accomplish the task:
 * </p>
 *
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;SRUServerServlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;eu.clarin.sru.server.utils.SRUServerServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;eu.clarin.sru.server.utils.sruServerSearchEngineClass&lt;/param-name&gt;
 *     &lt;param-value&gt;com.acme.MySearchEngine&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;SRUServerServlet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/sru&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 */
public final class SRUServerServlet extends HttpServlet {
    /**
     * Servlet initialization parameter name for the location of the SRU server
     * configuration.
     */
    public static final String SRU_SERVER_CONFIG_LOCATION_PARAM =
            "eu.clarin.sru.server.utils.sruServerConfigLocation";
    /**
     * Servlet initialization parameter name for the class that implements the
     * SRU search engine.
     */
    public static final String SRU_SERVER_SEARCH_ENGINE_CLASS_PARAM =
            "eu.clarin.sru.server.utils.sruServerSearchEngineClass";
    /**
     * Default value for the location of the SRU server configuration.
     */
    public static final String SRU_SERVER_CONFIG_LOCATION_DEFAULT =
            "/WEB-INF/sru-server-config.xml";
    private static final long serialVersionUID = 1L;
    private static final Logger logger =
            LoggerFactory.getLogger(SRUServerServlet.class);
    private SRUServer sruServer;
    private SRUSearchEngineBase searchEngine;


    /**
     * Initialize the SRU server Servlet.
     *
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        final ServletConfig cfg  = getServletConfig();
        final ServletContext ctx = getServletContext();

        String sruServerConfigLocation =
                cfg.getInitParameter(SRU_SERVER_CONFIG_LOCATION_PARAM);
        if (sruServerConfigLocation == null) {
            sruServerConfigLocation = SRU_SERVER_CONFIG_LOCATION_DEFAULT;
        }

        URL sruServerConfigFile;
        try {
            sruServerConfigFile = ctx.getResource(sruServerConfigLocation);
        } catch (MalformedURLException e) {
            throw new ServletException("init-parameter '" +
                    SRU_SERVER_CONFIG_LOCATION_PARAM + "' is not a valid URL",
                    e);
        }
        if (sruServerConfigFile == null) {
            throw new ServletException("init-parameter '" +
                    SRU_SERVER_CONFIG_LOCATION_PARAM +
                    "' points to non-existing resource (" +
                    sruServerConfigLocation + ")");
        }

        /* get search engine class name from Servlet init-parameters */
        String sruServerSearchEngineClass =
                cfg.getInitParameter(SRU_SERVER_SEARCH_ENGINE_CLASS_PARAM);
        if (sruServerSearchEngineClass == null) {
            throw new ServletException("init-parameter '" +
                    SRU_SERVER_SEARCH_ENGINE_CLASS_PARAM +
                    "' not defined in servlet configuration");
        }

        /*
         * get init-parameters from ServletConfig ...
         */
        Map<String, String> params = new HashMap<>();
        for (Enumeration<?> i = cfg.getInitParameterNames();
                i.hasMoreElements();) {
            String key = (String) i.nextElement();
            String value = cfg.getInitParameter(key);
            if ((value != null) && !value.isEmpty()) {
                params.put(key, value);
            }
        }

        /*
         * ... and get more init-parameters from ServletContext and potentially
         * overriding parameters from Servlet configuration.
         */
        for (Enumeration<?> i = ctx.getInitParameterNames();
                i.hasMoreElements();) {
            String key = (String) i.nextElement();
            String value = ctx.getInitParameter(key);
            if ((value != null) && !value.isEmpty()) {
                params.put(key, value);
            }
        }

        /*
         * Set some defaults (aka "plug and play" for development deployment)
         * Override those for a production deployment through your Servlet
         * container's context configuration!
         */
        if (!params.containsKey(SRUServerConfig.SRU_TRANSPORT)) {
            setDefaultConfigParam(params, SRUServerConfig.SRU_TRANSPORT,
                    "http");
        }
        if (!params.containsKey(SRUServerConfig.SRU_HOST)) {
            setDefaultConfigParam(params, SRUServerConfig.SRU_HOST,
                    "127.0.0.1");
        }
        if (!params.containsKey(SRUServerConfig.SRU_PORT)) {
            setDefaultConfigParam(params, SRUServerConfig.SRU_PORT,
                    "8080");
        }
        if (!params.containsKey(SRUServerConfig.SRU_DATABASE)) {
            String contextPath;
            try {
                /*
                 * this only works with Servlet 2.5 API ...
                 */
                contextPath = ctx.getContextPath();
            } catch (NoSuchMethodError e) {
                /*
                 * if we fail, put at least something here ...
                 */
                contextPath = "/";
                log("NOTE: auto-configuration for parameter '" +
                        SRUServerConfig.SRU_DATABASE +
                        "' failed and will contain only a dummy value!");
            }
            setDefaultConfigParam(params, SRUServerConfig.SRU_DATABASE,
                    contextPath);
        }
        // seal parameters against tampering ...
        params = Collections.unmodifiableMap(params);

        /*
         * now go ahead and setup everything ...
         */
        try {
            /*
             * parse SRU server configuration
             */
            SRUServerConfig sruServerConfig =
                    SRUServerConfig.parse(params, sruServerConfigFile);

            /*
             * create an instance of the search engine ...
             */
            try {
                logger.debug("creating new search engine from class {}",
                        sruServerSearchEngineClass);
                @SuppressWarnings("unchecked")
                Class<SRUSearchEngineBase> clazz = (Class<SRUSearchEngineBase>)
                    Class.forName(sruServerSearchEngineClass);
                Constructor<SRUSearchEngineBase> constructor =
                        clazz.getConstructor();
                searchEngine = constructor.newInstance();
            } catch (ClassNotFoundException |
                    NoSuchMethodException |
                    SecurityException |
                    InstantiationException |
                    IllegalAccessException |
                    IllegalArgumentException |
                    InvocationTargetException e) {
                throw new SRUConfigException("error creating search engine", e);
            }

            /*
             * initialize search engine ...
             */
            final SRUQueryParserRegistry.Builder builder =
                    new SRUQueryParserRegistry.Builder();
            searchEngine.init(ctx, sruServerConfig, builder, params);

            /*
             * create authentication provider
             */
            SRUAuthenticationInfoProvider authenticationProvider = null;
            if (SRUAuthenticationInfoProviderFactory.class.isInstance(searchEngine)) {
                logger.debug("creating new authentication info provider");

                authenticationProvider = ((SRUAuthenticationInfoProviderFactory) searchEngine)
                        .createAuthenticationInfoProvider(ctx, params);
            }

            /*
             * finally create the sru server ...
             */
            sruServer = new SRUServer(sruServerConfig,
                    builder.build(),
                    authenticationProvider,
                    searchEngine);
        } catch (SRUConfigException e) {
            String msg = (e.getMessage() != null)
                        ? "error configuring or inializing the server: " + e.getMessage()
                        : "error configuring or inializing the server";
            throw new ServletException(msg, e);
        }
    }


    /**
     * Destroy the SRU server Servlet.
     *
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        if (searchEngine != null) {
            searchEngine.destroy();
        }
        super.destroy();
    }


    /**
     * Handle a HTTP get request.
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (sruServer == null) {
            throw new ServletException("servlet is not properly initialized");
        }
        sruServer.handleRequest(request, response);
    }


    /**
     * Handle a HTTP post request.
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (sruServer == null) {
            throw new ServletException("servlet is not properly initalized");
        }
        sruServer.handleRequest(request, response);
    }


    private void setDefaultConfigParam(Map<String, String> params, String key,
            String value) {
        if (!params.containsKey(key)) {
            params.put(key, value);
            log("NOTE: using default '" + value + "' for parameter '" + key +
                    "', because it was not defined in context configuration.");
            log("THIS IS NOT RECOMMENDED FOR PRODUCTION DEPLOYMENT!");
        }
    }

} // class SRUServerServlet
