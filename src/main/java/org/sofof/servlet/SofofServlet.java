/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet;

import org.sofof.Database;
import org.sofof.Server;
import org.sofof.SofofException;
import java.io.File;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * <h3>مشغل الخادم</h3>
 * <p>
 * يقوم هذا المشغل بتشغيل الخادم يعد أن يتم ضبط ملف الweb.xml لتشغيل هذا خويدم,
 * ويتم تمرير الإعدادات الخاصة بالخادم من خلال الملف أيضا.</p>
 * <blockquote><pre>
 * {@code <servlet>
 *         <servlet-name>Sofof Server</servlet-name>
 *         <servlet-class>com.sefr.sofof.servlet.SofofServlet</servlet-class>
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
 * وظيفة إعداد الخادم هي تنفيذ دالة configure على الخادم
 * ووظيفة إعداد الجلسات هي تنفيذ الدالة الإستاتيكية configure على قاعدة البيانات لإعداد الجلسات
 * </pre></blockquote>
 *
 * @author Rami Manaf Abdullah
 */
public class SofofServlet extends HttpServlet {

    private Server server;

    /**
     * <p>
     * تقوم بتششغيل الخادم</p>
     *
     * @param config
     * @throws ServletException
     */
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
                Database.configure();
            }
        } catch (SofofException ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * <p>
     * تقوم بإغلاق الخادم</p>
     */
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
