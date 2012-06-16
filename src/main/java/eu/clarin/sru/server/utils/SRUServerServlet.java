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
package eu.clarin.sru.server.utils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUServer;
import eu.clarin.sru.server.SRUServerConfig;


/**
 * A Servlet implementation, which provides an enviroment for running a
 * {@link SRUServer} in a servlet container. Your search engine <b>must</b> use
 * {@link SRUSearchEngineBase} as base class.
 *
 * <p>
 * Add the following to the web.xml of your web applications web.xml to define a
 * SRU server. Of couse, the value of the servlet paramter
 * "sruServerSerachEngineClass" must be adpated to match the name of your search
 * engine implementation. Furthermore, you can choose different url-pattern, to
 * match your needs.
 * </p>
 *
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;SRUServerServlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;eu.clarin.sru.server.utils.SRUServerServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;sruServerSerachEngineClass&lt;/param-name&gt;
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
            "sruServerConfigLocation";
    /**
     * Servlet initialization parameter name for the class that implements the
     * SRU search engine.
     */
    public static final String SRU_SERVER_SERACH_ENGINE_CLASS_PARAM =
            "sruServerSerachEngineClass";
    /**
     * Default value for the location of the SRU server configuration.
     */
    public static final String SRU_SERVER_CONFIG_LOCATION_DEFAULT =
            "/WEB-INF/sru-server-config.xml";
    private static final long serialVersionUID = 1L;
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

        URL sruServerConfigFile = null;
        try {
            sruServerConfigFile = ctx.getResource(sruServerConfigLocation);
        } catch (MalformedURLException e) {
            throw new ServletException("init parameter '" +
                    SRU_SERVER_CONFIG_LOCATION_PARAM + "' is not a valid URL",
                    e);
        }
        if (sruServerConfigFile == null) {
            throw new ServletException("init parameter '" +
                    SRU_SERVER_CONFIG_LOCATION_PARAM +
                    "' points to non-existing resource (" +
                    sruServerConfigLocation + ")");
        }

        String sruServerSearchEngineClass =
                cfg.getInitParameter(SRU_SERVER_SERACH_ENGINE_CLASS_PARAM);
        if (sruServerSearchEngineClass == null) {
            throw new ServletException("init parameter '" +
                    SRU_SERVER_SERACH_ENGINE_CLASS_PARAM +
                    "' not defined in servlet configuration");
        }

        /*
         * get init-parameters from ServletConfig ...
         */
        final HashMap<String, String> params = new HashMap<String, String>();
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
         * overriding parameters from servlet configuration.
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
        setDefaultConfigParam(params, SRUServerConfig.SRU_TRANSPORT,
                              "http");
        setDefaultConfigParam(params, SRUServerConfig.SRU_HOST,
                              "127.0.0.1");
        setDefaultConfigParam(params, SRUServerConfig.SRU_PORT,
                              "8080");
        setDefaultConfigParam(params, SRUServerConfig.SRU_DATABASE,
                              ctx.getContextPath());

        // parse configuration
        SRUServerConfig sruServerConfig = null;
        try {
            sruServerConfig =
                    SRUServerConfig.parse(params, sruServerConfigFile);
        } catch (SRUConfigException e) {
            throw new ServletException("sru server configuration is invalid", e);
        }

        /*
         * create an instance of the search engine ...
         */
        try {
            @SuppressWarnings("unchecked")
            Class<SRUSearchEngineBase> clazz = (Class<SRUSearchEngineBase>)
                Class.forName(sruServerSearchEngineClass);
            Constructor<SRUSearchEngineBase> constructor =
                    clazz.getConstructor();
            searchEngine = constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ServletException("error inisializing sru server", e);
        } catch (ClassCastException e) {
            throw new ServletException("error inisializing sru server", e);
        } catch (NoSuchMethodException e) {
            throw new ServletException("error inisializing sru server", e);
        } catch (SecurityException e) {
            throw new ServletException("error inisializing sru server", e);
        } catch (InstantiationException e) {
            throw new ServletException("error inisializing sru server", e);
        } catch (IllegalAccessException e) {
            throw new ServletException("error inisializing sru server", e);
        } catch (IllegalArgumentException e) {
            throw new ServletException("error inisializing sru server", e);
        } catch (InvocationTargetException e) {
            throw new ServletException("error inisializing sru server", e);
        }

        /*
         * finally initialize the SRU server ...
         */
        try {
            searchEngine.init(sruServerConfig, params);
            sruServer = new SRUServer(sruServerConfig, searchEngine);
        } catch (SRUConfigException e) {
            throw new ServletException("error inisializing sru server", e);
        } catch (SRUException e) {
            throw new ServletException("error inisializing sru server", e);
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
     * @see
     * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (sruServer == null) {
            throw new ServletException("servlet is not properly initalized");
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