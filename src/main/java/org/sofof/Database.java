/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.permission.User;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

/**
 * قاعدة بيانات
 *
 * @author Rami Manaf Abdullah
 * @see Session
 */
public class Database {

    private String host;
    private int port;
    private static HashMap<String, Session> sessions = new HashMap<>();

    /**
     * <p>
     * تنشئ كائن قاعدة بيانات من عنوان قاعدة البيانات ورقم المنفذ. يمكن لعنوان
     * قاعدة البيانات أن يكون رابطا أو عنوان إنترنت, مثلا:</p>
     * <ul>
     * <li>google.com</li>
     * <li>179.0.23.8</li>
     * </ul>
     * في حالة كون قاعدة البيانات موجودة على الجهاز نفسه يجب استخدام عنوان
     * الجهاز نفسه, مثلا:
     * <ul>
     * <li>localhost</li>
     * <li>127.0.0.1</li>
     * <li></li>
     * </ul>
     *
     * @param host عنوان قاعدة البيانات
     * @param port رقم المنفذ
     */
    public Database(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * <p>
     * بدء جلسة من قاعدة البيانات باسم المستخدم الممرر</p>
     *
     * @param u المستخدم
     * @return جلسة
     * @throws SofofException إذا كانت قاعدة البيانات غير مرتبطة بخادم يعمل أو
     * إذا كانت المستخد غير مسجلا في الخادم
     */
    public Session startSession(User u) throws SofofException {
        return new Session(host, port, u, false);
    }

    /**
     * <p>
     * بدء جلسة من قاعدة البيانات باسم المستخدم الممرر وبتحديد إذا كان الاتصال
     * من الطبقة الآمنة أم لا</p>
     *
     * @param u المستخدم
     * @param ssl طبقة المقابس الآمنة
     * @return جلسة
     * @throws SofofException إذا كانت قاعدة البيانات غير مرتبطة بخادم يعمل أو
     * إذا كانت المستخد غير مسجلا في الخادم
     */
    public Session startSession(User u, boolean ssl) throws SofofException {
        return new Session(host, port, u, ssl);
    }

    /**
     * <p>
     * تنشئ قاعدة بيانات, إذا كان هناك قاعدة بيانات بنفس الاسم لن يتم عمل أي
     * شيء.</p>
     *
     * <p>
     * يجب الانتباه إلى أن ملف قاعدة البيانات لا يجب أن يكون موجودا</p>
     *
     * @param db ملف قاعدة البيانات
     * @return تعيد صحيح إذا كانت قاعدة البيانات غير موجودة وخاطئ إذا كانت هناك
     * قاعدة موجودة بنفس الاسم
     * @throws SofofException إذا حدث أي خطأ دخل وخرج
     */
    public static boolean createDatabase(File db) throws SofofException {
        if (db == null) {
            throw new NullPointerException("can't start a local session with no path specified");
        }
        if (!db.exists()) {
            try {
                db.mkdir();
                File binds = new File(db, "binds");
                binds.createNewFile();
                BindTree bindTree = new BindTree();
                bindTree.addBind(new BindTree.Bind("SofofNoName"));
                bindTree.addBind(new BindTree.Bind("SofofCaptures"));
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(binds, false));
                oos.writeObject(bindTree);
                oos.close();
            } catch (IOException ex) {
                throw new SofofException("can't creat database ", ex);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>
     * ستقوم بإعداد الجلسات من ملف</p>
     * sofof.xml
     *
     * @throws SofofException في حال حدوث خطأ دخل وخرج
     */
    public static void configure() throws SofofException {
        if (Database.class.getResource("/sofof.xml") != null) {
            Namespace namespace = Namespace.getNamespace("http://sofof.org/xsd");
            try {
                Element sessionsEl = new SAXBuilder().build(Database.class.getResource("/sofof.xml")).getRootElement().getChild("sessions", namespace);
                if (sessionsEl != null) {
                    for (Element session : sessionsEl.getChildren("session", namespace)) {
                        Element user = session.getChild("user", namespace);
                        sessions.put(session.getAttributeValue("name"), new Database(session.getAttributeValue("host", "localhost"), Integer.valueOf(session.getAttributeValue("port"))).startSession(new User(user.getAttributeValue("name"), user.getAttributeValue("password")), Boolean.valueOf(session.getAttributeValue("ssl", "false"))));
                    }
                }
            } catch (IOException ex) {
                throw new SofofException("unable to read sofof.xml", ex);
            } catch (JDOMException ex) {
                throw new SofofException("can not parse to sofof.xml", ex);
            }
        }
    }

    /**
     * تقوم بجلب الجلسة المقترنة باسمها من ملف sofof.xml
     *
     * @param name اسم الجلسة في ملف sofof.xml
     * @return الجلسة المقترنة بالاسم الممرر أو لا قيمة إذا لم يكن هناك جلسة
     * بذلك الاسم
     */
    public static Session getSession(String name) {
        return sessions.get(name);
    }

    /**
     * <p>
     * تتأكد من أن اسم الربط هو بلا اسم. إذا كان الاسم هو اللا قيمة أو كان فارغا
     * أو إذا كان عبارة عن مسافات فهو بلا اسم.</p>
     *
     * @param bind اسم الربط
     * @return صحيح إذا كان اسم الربط هو اللا اسم وخطأ في غير ذلك
     */
    public static boolean isNoName(String bind) {
        return bind == null || bind.trim().isEmpty();
    }

}
