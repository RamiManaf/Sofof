/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.command.Executable;
import org.sofof.command.Query;
import org.sofof.permission.SofofSecurityManager;
import org.sofof.permission.User;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * <h3>خادم قاعدة البيانات</h3>
 * <p>
 * يقوم هذا الخادم بخدمة قاعدة البيانات حتى تتمكن أي جلسة من تنفيذ الأوامر على
 * قاعدة البيانات.</p>
 *
 * <p>
 * يجب تشغيل الخادم حتى يعمل</p>
 * <blockquote><pre>
 * Server s = new Server(new File("db"), 6969, false);
 * s.startUp();
 * </pre></blockquote>
 * <p>
 * يمكن استخدام ملف توصيف لوصف قاعدة البيانات للخادم, ويتبع هذا الملف للقواعد
 * التالية:</p>
 * <ul>
 * <li>يجب أن يكون اسم الملف sofof.xml</li>
 * <li>يجب وضعه في المسار الأصلي وليس تحت أي حزمة</li>
 * <li>يجب أن يستخدم الجذر Namespace arabsefr.com/sofof/xsd</li>
 * </ul>
 *
 * @author Rami Manaf Abdullah
 * @see User
 */
public class Server extends Thread {

    private File db;
    private int port;
    private boolean ssl;
    private ServerSocket socket;
    private volatile BindTree bindTree;
    private List<User> users;
    private ClassLoader classLoader;

    /**
     * تنشئ كائن الخادم
     */
    public Server() {
        users = new ArrayList<>();
    }

    /**
     * ينشئ كائن الخادم
     *
     * @param db مجلد قاعدة البيانات
     * @param port المنفذ الذي سيستمع الخادم إليه
     * @param ssl استخدام طبقة المقابس الآمنة
     */
    public Server(File db, int port, boolean ssl) {
        this();
        this.db = db;
        this.port = port;
        this.ssl = ssl;
    }

    /**
     * ينشئ كائن الخادم
     *
     * @param db مجلد قاعدة البيانات
     * @param port المنفذ الذي سيستمع الخادم إليه
     * @param ssl استخدام طبقة المقابس الآمنة
     * @param users قائمة بالمستخدمين
     */
    public Server(File db, int port, boolean ssl, List<User> users) {
        this(db, port, ssl);
        this.users.addAll(users);
    }

    /**
     * ينشئ كائن الخادم
     *
     * @param db مجلد قاعدة البيانات
     * @param port المنفذ الذي سيستمع الخادم إليه
     * @param ssl استخدام طبقة المقابس الآمنة
     * @param users قائمة بالمستخدمين
     * @param loader محمل الصفوف الذي سيستخدم لتحميل الصفوف
     */
    public Server(File db, int port, boolean ssl, List<User> users, ClassLoader loader) {
        this(db, port, ssl);
        this.users.addAll(users);
        this.classLoader = loader;
    }

    /**
     *
     * @return محمل الصفوف أو لا قيمة
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * تحديد محمل الصفوف
     *
     * @param classLoader محمل الصفوف
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * <p>
     * تقوم بتشغيل الخادم</p>
     *
     * @throws SofofException
     * @return الخادم نفسه
     */
    public Server startUp() throws SofofException {
        try {
            readFromFile();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        shutdown();
                    } catch (SofofException ex) {
                        System.err.println(ex);
                        ex.printStackTrace();
                    }
                }
            });
            this.setDaemon(true);
            if (ssl) {
                socket = SSLServerSocketFactory.getDefault().createServerSocket(port);
            } else {
                socket = new ServerSocket(port);
            }
            this.setName("Sofof Server");
            this.start();
            return this;
        } catch (IOException ex) {
            throw new SofofException("can not open the server socket", ex);
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                Socket client = socket.accept();
                new Service(client).start();
            } catch (IOException ex) {
                if (!socket.isClosed()) {
                    System.err.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    public class Service extends Thread {

        private Socket socket;
        private User user;
        private ListInputStream in;
        private ListOutputStream out;

        public Service(Socket s) {
            socket = s;
            this.setName("Sofof Service");
        }

        @Override
        public void run() {
            try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());) {
                user = (User) ois.readObject();
                if (users.contains(user)) {
                    user = users.get(users.indexOf(user));
                    oos.writeBoolean(true);
                } else {
                    oos.writeBoolean(false);
                    return;
                }
                in = new ListInputStream(db, bindTree, classLoader);
                out = new ListOutputStream(db, bindTree);
                oos.flush();
                while (true) {
                    Object type = ois.readObject();
                    Object o = ois.readObject();
                    if (type == null) {
                        break;
                    } else if (type.equals(true)) {
                        try {
                            checkExecutingPermission(user, (Executable) o);
                            synchronized (bindTree) {
                                oos.writeObject(((Executable) o).execute(in, out));
                            }
                        } catch (SofofException | SecurityException ex) {
                            oos.writeObject(ex);
                        }
                    } else if (type.equals(false)) {
                        try {
                            checkQueryingPermission(user, (Query) o);
                            oos.writeObject(((Query) o).query(in));
                        } catch (SofofException | SecurityException ex) {
                            oos.writeObject(ex);
                        }
                    }
                    commite();
                    oos.flush();
                }
            } catch (EOFException ex) {
            } catch (ClassNotFoundException ex) {
                try {
                    throw new SofofException("the recived class not found in the classpath", ex);
                } catch (SofofException ex1) {
                    System.err.println(ex1.getMessage());
                    ex1.printStackTrace();
                }
            } catch (IOException ex) {
                if (!socket.isClosed()) {
                    System.err.println(ex.getMessage());
                    ex.printStackTrace();
                }
            } catch (SofofException ex) {
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

    }

    private void loadXML() throws SofofException {
        if (getClass().getResource("/sofof.xml") == null) {
            return;
        }
        try {
            org.jdom2.Namespace namespace = org.jdom2.Namespace.getNamespace("http://arabsefr.com/sofof/xsd");
            org.jdom2.Element root = new org.jdom2.input.SAXBuilder().build(getClass().getResource("/sofof.xml")).getRootElement();
            org.jdom2.Element server = root.getChild("server", namespace);
            this.ssl = Boolean.valueOf(server.getAttributeValue("ssl", "false"));
            this.db = new File(server.getChild("database", namespace).getAttributeValue("path"), server.getChild("database", namespace).getAttributeValue("name"));
            Database.createDatabase(this.db);
            port = Integer.parseInt(server.getAttributeValue("port"));
            if (server.getChild("users", namespace) != null) {
                org.jdom2.Element usersEl = server.getChild("users", namespace);
                for (org.jdom2.Element userEl : usersEl.getChildren("user", namespace)) {
                    User u = new User(userEl.getAttributeValue("name"), userEl.getAttributeValue("password"));
                    users.add(u);
                }
            }
        } catch (IOException ex) {
            throw new SofofException("unable to read sofof.xml", ex);
        } catch (org.jdom2.JDOMException ex) {
            throw new SofofException("can not parse to sofof.xml", ex);
        }
    }

    private void readFromFile() throws SofofException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(db, "binds"))) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    if (classLoader != null) {
                        try {
                            return Class.forName(desc.getName(), false, classLoader);
                        } catch (ClassNotFoundException ex) {
                            return super.resolveClass(desc);
                        }
                    } else {
                        return super.resolveClass(desc); //To change body of generated methods, choose Tools | Templates.
                    }
                }
            };
            bindTree = (BindTree) ois.readObject();
        } catch (IOException ex) {
            throw new SofofException("can not read meta data", ex);
        } catch (ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * <h3>حفظ</h3>
     * يتم حفظ إعدادات قاعدة البيانات
     *
     * @throws SofofException
     */
    private synchronized void commite() throws SofofException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(db, "binds"), false))) {
            oos.writeObject(bindTree);
        } catch (IOException ex) {
            throw new SofofException("can not save changes to meta ", ex);
        }
    }

    /**
     * تقوم بإعداد الخادم من ملف sofof.xml
     *
     * @return الخادم نفسه
     * @throws SofofException حدوث خطأ في تحميل الملف وتفسيره
     */
    public Server configure() throws SofofException {
        loadXML();
        return this;
    }

    private static void checkExecutingPermission(User user, Executable exe) throws SofofException {
        if (System.getSecurityManager() != null
                && SofofSecurityManager.class.isAssignableFrom(System.getSecurityManager().getClass())) {
            ((SofofSecurityManager) System.getSecurityManager()).checkExecutable(user, exe);
        }
    }

    private static void checkQueryingPermission(User user, Query query) throws SofofException {
        if (System.getSecurityManager() != null
                && SofofSecurityManager.class.isAssignableFrom(System.getSecurityManager().getClass())) {
            ((SofofSecurityManager) System.getSecurityManager()).checkQuery(user, query);
        }
    }

    public int execute(User user, Executable exe) throws SofofException {
        checkExecutingPermission(user, exe);
        int result;
        synchronized (bindTree) {
            result = exe.execute(new ListInputStream(db, bindTree, classLoader), new ListOutputStream(db, bindTree));
        }
        commite();
        return result;
    }

    public List query(User user, Query query) throws SofofException {
        checkQueryingPermission(user, query);
        return query.query(new ListInputStream(db, bindTree, classLoader));
    }

    /**
     * <p>
     * تغلق الخادم بشكل سليم مع حفظ إعدادات قاعدة البيانات, في الوضع الافتراضي
     * للخادم يكون مخفيا Deamon وستنفذ هذه الدالة عن إغلاق البرنامج بشكل طبيعي,
     * أما عندما يحدد الخادم كغير مخفي فيجب تنفيذ هذه الدالة عند الانتهاء من
     * استخدام قاعدة البيانات.</p>
     *
     * @throws org.sofof.SofofException
     */
    public void shutdown() throws SofofException {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
        }
        interrupt();
    }

}
