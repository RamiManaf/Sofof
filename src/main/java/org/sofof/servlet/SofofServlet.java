/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet;

import org.sofof.Server;
import org.sofof.SofofException;
import java.io.File;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.sofof.SessionManager;

/**
 * This servlet starts the Server when started using web.xml file.
 * <blockquote><pre>
 * {@code <servlet>
 *         <servlet-name>Sofof Server</servlet-name>
 *         <servlet-class>org.sofof.servlet.SofofServlet</servlet-class>
 *         <load-on-startup>0</load-on-startup>
 *         <init-param>
 *             <param-name>path</param-name>
 *             <param-value>db</param-value>
 *         </init-param>
 *         <init-param>
 *             <param-name>port</param-name>
 *             <param-value>6969</param-value>
 *         </init-param>
 *         <init-param>
 *             <param-name>ssl</param-name>
 *             <param-value>false</param-value>
 *         </init-param>
 *         <init-param>
 *             <param-name>configureServer</param-name>
 *             <param-value>false</param-value>
 *         </init-param>
 *         <init-param>
 *             <param-name>configureSession</param-name>
 *             <param-value>false</param-value>
 *         </init-param>
 *</servlet>}
 * </pre></blockquote>
 * Configure server execute the configure method on the server object and configure session execute configure method on
 * {@link SessionManager}
 * 
 * @author Rami Manaf Abdullah
 */
public class SofofServlet extends HttpServlet {

    private static Server server;

    
    @Override
    public void init(ServletConfig config) throws ServletException {
        if (config.getInitParameter("path") != null) {
            server = new Server(new File(config.getInitParameter("path")), Integer.valueOf(config.getInitParameter("port")), Boolean.valueOf(config.getInitParameter("ssl")));
        } else {
            server = new Server();
        }
        try {
            if (config.getInitParameter("configureServer") != null && config.getInitParameter("configureServer").equalsIgnoreCase("true")) {
                server.configure();
            }
            server.startUp();
            if (config.getInitParameter("configureSession") != null && config.getInitParameter("configureSession").equalsIgnoreCase("true")) {
                SessionManager.configure();
            }
        } catch (SofofException ex) {
            throw new ServletException(ex);
        }
    }

    public static Server getServer() {
        return server;
    }

    @Override
    public void destroy() {
        try {
            server.shutdown();
        } catch (SofofException ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
    }

}
