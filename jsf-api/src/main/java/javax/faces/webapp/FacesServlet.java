/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.faces.webapp;


import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <p><strong class="changed_modified_2_0
 * changed_modified_2_0_rev_a">FacesServlet</strong> is a servlet that
 * manages the request processing lifecycle for web applications that
 * are utilizing JavaServer Faces to construct the user interface.</p>
 */

public final class FacesServlet implements Servlet {

    /*
     * A white space separated list of case sensitive HTTP method names
     * that are allowed to be processed by this servlet. * means allow all
     */
    private static final String ALLOWED_HTTP_METHODS_ATTR =
            "com.sun.faces.allowedHttpMethods";
    
    private Set<String> allowedHttpMethods;
    private Set<String> defaultAllowedHttpMethods;
    private Set<String> allHttpMethods;

    private boolean allowAllMethods;

    /**
     * <p>Context initialization parameter name for a comma delimited list
     * of context-relative resource paths (in addition to
     * <code>/WEB-INF/faces-config.xml</code> which is loaded automatically
     * if it exists) containing JavaServer Faces configuration information.</p>
     */
    public static final String CONFIG_FILES_ATTR =
        "javax.faces.CONFIG_FILES";


    /**
     * <p>Context initialization parameter name for the lifecycle identifier
     * of the {@link Lifecycle} instance to be utilized.</p>
     */
    public static final String LIFECYCLE_ID_ATTR =
        "javax.faces.LIFECYCLE_ID";


    /**
     * The <code>Logger</code> for this class.
     */
    private static final Logger LOGGER =
          Logger.getLogger("javax.faces.webapp", "javax.faces.LogStrings");


    /**
     * <p>Factory for {@link FacesContext} instances.</p>
     */
    private FacesContextFactory facesContextFactory = null;


    /**
     * <p>The {@link Lifecycle} instance to use for request processing.</p>
     */
    private Lifecycle lifecycle = null;


    /**
     * <p>The <code>ServletConfig</code> instance for this servlet.</p>
     */
    private ServletConfig servletConfig = null;
    
    /**
     * From GLASSFISH-15632.  If true, the FacesContext instance
     * left over from startup time has been released.  
     */
    private boolean initFacesContextReleased = false;
    
    /**
     * <p>Release all resources acquired at startup time.</p>
     */
    public void destroy() {

        facesContextFactory = null;
        lifecycle = null;
        servletConfig = null;
        uninitHttpMethodValidityVerification();

    }


    /**
     * <p>Return the <code>ServletConfig</code> instance for this servlet.</p>
     */
    public ServletConfig getServletConfig() {

        return (this.servletConfig);

    }


    /**
     * <p>Return information about this Servlet.</p>
     */
    public String getServletInfo() {

        return (this.getClass().getName());

    }


    /**
     * <p>Acquire the factory instances we will require.</p>
     *
     * @throws ServletException if, for any reason, the startup of
     * this Faces application failed.  This includes errors in the
     * config file that is parsed before or during the processing of
     * this <code>init()</code> method.
     */
    public void init(ServletConfig servletConfig) throws ServletException {

        // Save our ServletConfig instance
        this.servletConfig = servletConfig;

        // Acquire our FacesContextFactory instance
        try {
            facesContextFactory = (FacesContextFactory)
                FactoryFinder.getFactory
                (FactoryFinder.FACES_CONTEXT_FACTORY);
        } catch (FacesException e) {
            ResourceBundle rb = LOGGER.getResourceBundle();
            String msg = rb.getString("severe.webapp.facesservlet.init_failed");
            Throwable rootCause = (e.getCause() != null) ? e.getCause() : e;
            LOGGER.log(Level.SEVERE, msg, rootCause);
            throw new UnavailableException(msg);
        }

        // Acquire our Lifecycle instance
        try {
            LifecycleFactory lifecycleFactory = (LifecycleFactory)
                FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            String lifecycleId ;

            // First look in the servlet init-param set
            if (null == (lifecycleId = servletConfig.getInitParameter(LIFECYCLE_ID_ATTR))) {
                // If not found, look in the context-param set 
                lifecycleId = servletConfig.getServletContext().getInitParameter
                    (LIFECYCLE_ID_ATTR);
            }

            if (lifecycleId == null) {
                lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
            }
            lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
            initHttpMethodValidityVerification();
        } catch (FacesException e) {
            Throwable rootCause = e.getCause();
            if (rootCause == null) {
                throw e;
            } else {
                throw new ServletException(e.getMessage(), rootCause);
            }
        }

    }

    private void initHttpMethodValidityVerification() {

        assert (null == allowedHttpMethods);
        assert (null == defaultAllowedHttpMethods);
        assert (null == allHttpMethods);
        // Http method names must be upper case. http://www.w3.org/Protocols/HTTP/NoteMethodCS.html
        // List of valid methods in Http 1.1 http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9
        allHttpMethods = new HashSet(Arrays.asList("OPTIONS", "GET", "HEAD", "POST",
                "PUT", "DELETE", "TRACE", "CONNECT"));
        defaultAllowedHttpMethods = new HashSet(allHttpMethods);
        allHttpMethods.add("*");

        // Configure our permitted HTTP methods
        String[] methods = {};
        String allowedHttpMethodsString = servletConfig.getServletContext().getInitParameter(ALLOWED_HTTP_METHODS_ATTR);
        if (null != allowedHttpMethodsString) {
            methods = allowedHttpMethodsString.split("\\s");
            assert (null != methods); // assuming split always returns a non-null array result
            // validate input against allHttpMethods data structure
            String allMethodsString = allHttpMethods.toString();
            for (String cur : methods) {
                if (!allHttpMethods.contains(cur)) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING,
                                "warning.webapp.facesservlet.init_invalid_http_method",
                                new Object[]{cur, allMethodsString});
                    }
                }
            }
        }
        allowedHttpMethods = (0 < methods.length) ? new HashSet(Arrays.asList(methods)) : defaultAllowedHttpMethods;
        allowAllMethods = allowedHttpMethods.contains("*");

    }

    private void uninitHttpMethodValidityVerification() {
        assert (null != allowedHttpMethods);
        assert (null != defaultAllowedHttpMethods);
        assert (null != allHttpMethods);

        allowedHttpMethods.clear();
        allowedHttpMethods = null;
        defaultAllowedHttpMethods.clear();
        defaultAllowedHttpMethods = null;
        allHttpMethods.clear();
        allHttpMethods = null;

    }


    /**
     * <p class="changed_modified_2_0">Process an incoming request, and create the
     * corresponding response according to the following
     * specification.</p>
     * 
     * <div class="changed_modified_2_0">
     *
     * <p>If the <code>request</code> and <code>response</code>
     * arguments to this method are not instances of
     * <code>HttpServletRequest</code> and
     * <code>HttpServletResponse</code>, respectively, the results of
     * invoking this method are undefined.</p>
     *
     * <p>This method must respond to requests that start with the
     * following strings by invoking the <code>sendError</code> method
     * on the response argument (cast to
     * <code>HttpServletResponse</code>), passing the code
     * <code>HttpServletResponse.SC_NOT_FOUND</code> as the
     * argument. </p>
     *
     * <ul>
     *
<pre><code>
/WEB-INF/
/WEB-INF
/META-INF/
/META-INF
</code></pre>
     *
     * </ul>
     *
     
     * <p>If none of the cases described above in the specification for
     * this method apply to the servicing of this request, the following
     * action must be taken to service the request.</p>

     * <p>Acquire a {@link FacesContext} instance for this request.</p>

     * <p>Acquire the <code>ResourceHandler</code> for this request by
     * calling {@link
     * javax.faces.application.Application#getResourceHandler}.  Call
     * {@link
     * javax.faces.application.ResourceHandler#isResourceRequest}.  If
     * this returns <code>true</code> call {@link
     * javax.faces.application.ResourceHandler#handleResourceRequest}.
     * If this returns <code>false</code>, call {@link
     * javax.faces.lifecycle.Lifecycle#execute} followed by {@link
     * javax.faces.lifecycle.Lifecycle#render}.  If a {@link
     * javax.faces.FacesException} is thrown in either case, extract the
     * cause from the <code>FacesException</code>.  If the cause is
     * <code>null</code> extract the message from the
     * <code>FacesException</code>, put it inside of a new
     * <code>ServletException</code> instance, and pass the
     * <code>FacesException</code> instance as the root cause, then
     * rethrow the <code>ServletException</code> instance.  If the cause
     * is an instance of <code>ServletException</code>, rethrow the
     * cause.  If the cause is an instance of <code>IOException</code>,
     * rethrow the cause.  Otherwise, create a new
     * <code>ServletException</code> instance, passing the message from
     * the cause, as the first argument, and the cause itself as the
     * second argument.</p>

     * In a finally block, {@link
     * javax.faces.context.FacesContext#release} must be called.

     * </div>
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs during processing
     * @throws ServletException if a servlet error occurs during processing

     */
    public void service(ServletRequest req,
                        ServletResponse resp)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        requestStart(request.getRequestURI()); // V3 Probe hook
        
        if (!isHttpMethodValid(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (Thread.currentThread().isInterrupted()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINE, "Thread {0} given to FacesServlet.service() in interrupted state", 
                        Thread.currentThread().getName());
            }
        }

        // If prefix mapped, then ensure requests for /WEB-INF are
        // not processed.
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            pathInfo = pathInfo.toUpperCase();
            if (pathInfo.startsWith("/WEB-INF/")
                || pathInfo.equals("/WEB-INF")
                || pathInfo.startsWith("/META-INF/")
                || pathInfo.equals("/META-INF")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }    

        if (!initFacesContextReleased) {
            FacesContext initFacesContext = FacesContext.getCurrentInstance();
            if (null != initFacesContext) {
                initFacesContext.release();
            }
            initFacesContextReleased = true;
        }
        
        // Acquire the FacesContext instance for this request
        FacesContext context = facesContextFactory.getFacesContext
              (servletConfig.getServletContext(), request, response, lifecycle);

        // Execute the request processing lifecycle for this request
        try {
            ResourceHandler handler =
                  context.getApplication().getResourceHandler();
            if (handler.isResourceRequest(context)) {
                handler.handleResourceRequest(context);
            } else {
                lifecycle.execute(context);
                lifecycle.render(context);
            }
        } catch (FacesException e) {
            Throwable t = e.getCause();
            if (t == null) {
                throw new ServletException(e.getMessage(), e);
            } else {
                if (t instanceof ServletException) {
                    throw ((ServletException) t);
                } else if (t instanceof IOException) {
                    throw ((IOException) t);
                } else {
                    throw new ServletException(t.getMessage(), t);
                }
            }
        }
        finally {
            // Release the FacesContext instance for this request
            context.release();
        }

        requestEnd(); // V3 Probe hook
    }

    private boolean isHttpMethodValid(HttpServletRequest request) {
        boolean result = allowAllMethods || allowedHttpMethods.contains(request.getMethod());

        return result;
    }


    // --------------------------------------------------------- Private Methods


    /**
     * DO NOT REMOVE. Necessary for V3 probe monitoring.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private void requestStart(String requestUri) { }


    /**
     * DO NOT REMOVE. Necessary for V3 probe monitoring.
     */
    private void requestEnd() { }

}
