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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocketFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.sofof.serializer.JavaSerializer;
import org.sofof.serializer.Serializer;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
 * </ul>
 *
 * @author Rami Manaf Abdullah
 * @see User
 */
public class Server extends Thread {

    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    private boolean internal = false;
    private File db;
    private int port;
    private boolean ssl;
    private Serializer serializer;
    private ServerSocket socket;
    private volatile BindTree bindTree;
    private List<User> users;
    private List<String> clients;
    private ReentrantReadWriteLock readWriteLock;

    /**
     * تنشئ كائن الخادم
     */
    public Server() {
        this(null);
    }

    public Server(File db) {
        this(db, -1, false);
    }

    /**
     * ينشئ كائن الخادم
     *
     * @param db مجلد قاعدة البيانات
     * @param port المنفذ الذي سيستمع الخادم إليه
     * @param ssl استخدام طبقة المقابس الآمنة
     */
    public Server(File db, int port, boolean ssl) {
        this(db, port, ssl, new ArrayList<>());
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
        this.db = db;
        this.port = port;
        this.ssl = ssl;
        this.users = new ArrayList<>(Objects.requireNonNull(users));
        serializer = new JavaSerializer();
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<String> getClients() {
        return clients;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = Objects.requireNonNull(serializer);
    }

    public void setUsers(List<User> users) {
        this.users = Objects.requireNonNull(users);
    }

    public void setClients(List<String> clients) {
        this.clients = clients;
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
            readMetaData();
            if (db == null) {
                throw new SofofException("you havn't choose the db folder");
            }
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        shutdown();
                    } catch (SofofException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    }
                }
            });
            this.setDaemon(true);
            this.setName("Sofof Server");
            cleanUp();
            if (port == -1) {
                internal = true;
            } else {
                internal = false;
                if (ssl) {
                    socket = SSLServerSocketFactory.getDefault().createServerSocket(port);
                } else {
                    socket = new ServerSocket(port);
                }
                this.start();
            }
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
                if (clients == null || clients.contains(client.getInetAddress().getHostAddress()) || clients.contains(client.getInetAddress().getHostName())) {
                    new Service(client).start();
                } else {
                    client.close();
                }
            } catch (IOException ex) {
                if (!socket.isClosed()) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public class Service extends Thread {

        private final Socket client;
        private User user;
        private DefaultListInputStream in;
        private DefaultListOutputStream out;

        public Service(Socket s) {
            client = s;
            this.setName("Sofof Service");
        }

        @Override
        public void run() {
            try {
                user = (User) Session.readObject(client.getInputStream(), serializer);
                if (users.contains(user)) {
                    user = users.get(users.indexOf(user));
                    client.getOutputStream().write(Session.BOOLEAN_TRUE);
                    client.getOutputStream().flush();
                } else {
                    client.getOutputStream().write(Session.BOOLEAN_FALSE);
                    return;
                }
                in = new DefaultListInputStream(db, bindTree, serializer);
                out = new DefaultListOutputStream(db, bindTree, serializer);
                while (true) {
                    int type = (byte) client.getInputStream().read();
                    if (type == -1) {
                        break;
                    }
                    Object obj = Session.readObject(client.getInputStream(), serializer);
                    if (type == Session.COMMAND_EXECUTABLE) {
                        Lock lock = readWriteLock.writeLock();
                        try {
                            lock.lock();
                            checkExecutingPermission(user, (Executable) obj);
                            Session.writeObjct(client.getOutputStream(), serializer, ((Executable) obj).execute(in, out));
                        } catch (SofofException | SecurityException ex) {
                            Session.writeObjct(client.getOutputStream(), serializer, ex);
                        } finally {
                            lock.unlock();
                        }
                        commite();
                    } else if (type == Session.COMMAND_QUERY) {
                        Lock lock = readWriteLock.readLock();
                        try {
                            lock.lock();
                            checkQueryingPermission(user, (Query) obj);
                            Session.writeObjct(client.getOutputStream(), serializer, ((Query) obj).query(in));
                        } catch (SofofException | SecurityException ex) {
                            Session.writeObjct(client.getOutputStream(), serializer, ex);
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (IOException | SofofException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (!client.isClosed()) {
                        client.close();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        }

    }

    private void loadXML() throws SofofException {
        if (getClass().getResource("/sofof.xml") == null) {
            return;
        }
        try {
            Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getClass().getResourceAsStream("/sofof.xml")).getDocumentElement();
            Element serverElement = (Element) root.getElementsByTagName("server").item(0);
            this.db = new File(serverElement.getAttribute("database"));
            createDatabase();
            port = serverElement.getAttribute("port") == null ? -1 : Integer.parseInt(serverElement.getAttribute("port"));
            if (port != -1) {
                this.ssl = serverElement.getAttribute("ssl").isEmpty() ? false : Boolean.valueOf(serverElement.getAttribute("ssl"));
                if (serverElement.getElementsByTagName("users").getLength() != 0) {
                    Element usersElement = (Element) serverElement.getElementsByTagName("users").item(0);
                    for (int i = 0; i < usersElement.getElementsByTagName("user").getLength(); i++) {
                        Element userElement = (Element) usersElement.getElementsByTagName("user").item(i);
                        User u = new User(userElement.getAttribute("name"), userElement.getAttribute("password"));
                        users.add(u);
                    }
                }
                if (serverElement.getElementsByTagName("clients").getLength() != 0) {
                    Element clientsElement = (Element) serverElement.getElementsByTagName("clients").item(0);
                    clients = new ArrayList<>();
                    for (int i = 0; i < clientsElement.getElementsByTagName("client").getLength(); i++) {
                        clients.add(clientsElement.getElementsByTagName("client").item(i).getTextContent());
                    }
                }
            }
        } catch (IOException ex) {
            throw new SofofException("unable to read sofof.xml", ex);
        } catch (SAXException ex) {
            throw new SofofException("can not parse to sofof.xml", ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readMetaData() throws SofofException {
        try {
            bindTree = (BindTree) serializer.deserialize(Files.readAllBytes(new File(db, "binds").toPath()));
        } catch (IOException ex) {
            throw new SofofException("can not read meta data", ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * <h3>حفظ</h3>
     * يتم حفظ إعدادات قاعدة البيانات
     *
     * @throws SofofException
     */
    private synchronized void commite() throws SofofException {
        try ( FileOutputStream out = new FileOutputStream(new File(db, "binds"), false)) {
            out.write(serializer.serialize(bindTree));
            out.flush();
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

    /**
     * تستخدم في حالة كان الخادم داخليا فقط
     *
     * @param exe الأمر
     * @return ناتج تنفيذ الأمر, غالبا عدد الكائنات المتأثرة بالأمر
     * @throws SofofException
     */
    public int execute(Executable exe) throws SofofException {
        if (!internal) {
            throw new SofofException("unauthenticated execute for an  external server has been blocked");
        }
        Lock lock = readWriteLock.writeLock();
        int result = 0;
        try {
            lock.lock();
            result = exe.execute(new DefaultListInputStream(db, bindTree, serializer), new DefaultListOutputStream(db, bindTree, serializer));
        } finally {
            lock.unlock();
        }
        commite();
        return result;
    }

    /**
     * تستخدم في حالة كان الخادم داخليا فقط
     *
     * @param query الاستعلام
     * @return قائمة بالكائنات المستعلم عنها
     * @throws SofofException
     */
    public List query(Query query) throws SofofException {
        if (!internal) {
            throw new SofofException("unauthenticated query for an  external server has been blocked");
        }
        Lock lock = readWriteLock.readLock();
        List result;
        try{
            lock.lock();
            result = query.query(new DefaultListInputStream(db, bindTree, serializer));
        }finally{
            lock.unlock();
        }
        return result;
    }

    /**
     * <p>
     * تنشئ قاعدة بيانات, إذا كان هناك قاعدة بيانات بنفس الاسم لن يتم عمل أي
     * شيء.</p>
     *
     * <p>
     * يجب الانتباه إلى أن ملف قاعدة البيانات لا يجب أن يكون موجودا</p>
     *
     * @return تعيد صحيح إذا كانت قاعدة البيانات غير موجودة وخاطئ إذا كانت هناك
     * قاعدة موجودة بنفس الاسم
     * @throws SofofException إذا حدث أي خطأ دخل وخرج
     */
    public boolean createDatabase() throws SofofException {
        if (!db.exists()) {
            try {
                db.mkdir();
                File binds = new File(db, "binds");
                binds.createNewFile();
                try ( FileOutputStream out = new FileOutputStream(binds, false)) {
                    out.write(serializer.serialize(new BindTree()));
                }
            } catch (IOException ex) {
                throw new SofofException("couldn't create database ", ex);
            }
            return true;
        } else {
            return false;
        }
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
        if (!internal) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ex) {
            }
            interrupt();
        }
    }

    private void cleanUp() {
        for (BindTree.Bind bind : bindTree.getBinds()) {
            for (BindTree.BindClass bindClass : bind.getClasses()) {
                if (bindClass.getStorageFile() != null) {
                    File temp = new File(bindClass.getStorageFile().getParentFile(), "temp-" + bindClass.getStorageFile().getName());
                    if (temp.exists()) {
                        bindClass.getStorageFile().delete();
                        temp.renameTo(bindClass.getStorageFile());
                    }
                }
            }
        }
    }

}
