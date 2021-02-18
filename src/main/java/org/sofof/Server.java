/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import org.sofof.command.Executable;
import org.sofof.command.Query;
import org.sofof.permission.SofofSecurityManager;
import org.sofof.permission.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
import org.sofof.serializer.Serializer;
import org.sofof.serializer.SofofSerializer;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <h3>Sofof default server</h3>
 * this server offer the database services for sessions
 *
 * <blockquote><pre>
 * Server s = new Server(new File("db"), 6969, false);
 * s.startUp();
 * </pre></blockquote>
 * you can configure the server using xml files. The file should have the
 * sofof.xml name and should be placed in the default package
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
    private volatile BindingNamesTree bindTree;
    private List<User> users;
    private List<String> clients;
    private ReentrantReadWriteLock readWriteLock;
    private SofofSecurityManager securityManager;

    public Server() {
        this(null);
    }

    /**
     * creates a new local server with no ssl
     *
     * @param db database folder
     */
    public Server(File db) {
        this(db, -1, false);
    }

    /**
     *
     * @param db database folder
     * @param port port which the server will listen to sessions requests. pass
     * -1 for using Server locally
     * @param ssl use ssl
     */
    public Server(File db, int port, boolean ssl) {
        this(db, port, ssl, new ArrayList<>());
    }

    /**
     *
     * @param db database folder
     * @param port port which the server will listen to sessions requests. pass
     * -1 for using Server locally
     * @param ssl use ssl
     * @param users users list
     */
    public Server(File db, int port, boolean ssl, List<User> users) {
        this(db, port, ssl, users, new SofofSerializer());
    }

    /**
     *
     * @param db database folder
     * @param port port which the server will listen to sessions requests. pass
     * -1 for using Server locally
     * @param ssl use ssl
     * @param users users list
     * @param serializer serializer that will be used for communication
     */
    public Server(File db, int port, boolean ssl, List<User> users, Serializer serializer) {
        this.db = db;
        this.port = port;
        this.ssl = ssl;
        this.users = new ArrayList<>(Objects.requireNonNull(users));
        this.serializer = serializer;
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    /**
     * set the security manager instance that will be used by this server to
     * check the accessibility for users on the database
     *
     * @param securityManager
     */
    public void setSecurityManager(SofofSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * return the serializer used in this server
     *
     * @return
     */
    public Serializer getSerializer() {
        return serializer;
    }

    /**
     *
     * @return allowed users list
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     *
     * @return allowed hosts list or null if there is no restriction on hosts
     * that will use the database
     */
    public List<String> getClients() {
        return clients;
    }

    /**
     * changes the serializer
     *
     * @param serializer
     */
    public void setSerializer(Serializer serializer) {
        this.serializer = Objects.requireNonNull(serializer);
    }

    /**
     * sets a new allowed users list
     *
     * @param users
     */
    public void setUsers(List<User> users) {
        this.users = Objects.requireNonNull(users);
    }

    /**
     * sets hosts or ip addresses of the allowed hosts to connect to the
     * database
     *
     * @param clients
     */
    public void setClients(List<String> clients) {
        this.clients = clients;
    }

    /**
     * startup the server
     *
     * @throws SofofException
     * @return this object
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
                user = (User) serializer.deserialize(client.getInputStream());
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
                    Object obj = serializer.deserialize(client.getInputStream());
                    if (type == Session.COMMAND_EXECUTABLE) {
                        Lock lock = readWriteLock.writeLock();
                        try {
                            lock.lock();
                            checkExecutingPermission(user, (Executable) obj);
                            serializer.serialize(((Executable) obj).execute(in, out), client.getOutputStream());
                            commit();
                        } catch (SofofException | SecurityException ex) {
                            serializer.serialize(ex, client.getOutputStream());
                            throw ex;
                        } finally {
                            lock.unlock();
                        }
                    } else if (type == Session.COMMAND_QUERY) {
                        Lock lock = readWriteLock.readLock();
                        try {
                            lock.lock();
                            checkQueryingPermission(user, (Query) obj);
                            serializer.serialize(((Query) obj).query(in), client.getOutputStream());
                        } catch (SofofException | SecurityException ex) {
                            serializer.serialize(ex, client.getOutputStream());
                            throw ex;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (IOException | SofofException | ClassNotFoundException ex) {
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
            bindTree = (BindingNamesTree) serializer.deserialize(new ByteArrayInputStream(readFile(new File(db, "binds"))));
        } catch (IOException ex) {
            throw new SofofException("can not read meta data", ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private byte[] readFile(File file) throws FileNotFoundException, IOException {
        try ( FileInputStream in = new FileInputStream(file)) {
            if (file.length() > Integer.MAX_VALUE) {
                throw new RuntimeException(file.getName() + " is too big to read");
            }
            byte[] data = new byte[(int) file.length()];
            int position = 0;
            while (position < data.length) {
                int bytesRead = in.read(data, position, data.length - position);
                if (bytesRead == -1) {
                    throw new EOFException("had read only " + position + " of " + data.length + " and end of stream is reached");
                }
                position += bytesRead;
            }
            return data;
        }
    }

    /**
     * save BindingNamesTree to binds file
     *
     * @throws SofofException
     */
    private void commit() throws SofofException {
        try ( FileOutputStream out = new FileOutputStream(new File(db, "binds"), false)) {
            serializer.serialize(bindTree, out);
            out.flush();
        } catch (IOException ex) {
            throw new SofofException("can not save changes to meta ", ex);
        }
    }

    /**
     * configure the server from an xml file
     *
     * @return this object
     * @throws SofofException when there is an error in reading the xml file
     */
    public Server configure() throws SofofException {
        loadXML();
        return this;
    }

    private void checkExecutingPermission(User user, Executable exe) throws SofofException {
        if (securityManager != null) {
            securityManager.checkExecutable(user, exe);
        }
    }

    private void checkQueryingPermission(User user, Query query) throws SofofException {
        if (securityManager != null) {
            securityManager.checkQuery(user, query);
        }
    }

    /**
     * creates a new database folder and binds file. if there is an existing
     * folder this method will not do anything
     *
     * @return true if a new database is created successfully and false if there
     * is a folder with the same name
     * @throws SofofException
     */
    public boolean createDatabase() throws SofofException {
        if (!db.exists()) {
            try {
                db.mkdir();
                File binds = new File(db, "binds");
                binds.createNewFile();
                try ( FileOutputStream out = new FileOutputStream(binds, false)) {
                    serializer.serialize(new BindingNamesTree(), out);
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
     * stops the server listening and stop other connections
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

    /**
     * recover the server from forced closing
     */
    private void cleanUp() {
        for (BindingNamesTree.BindingName bind : bindTree.getBindingNames()) {
            for (BindingNamesTree.BindingClass bindClass : bind.getClasses()) {
                if (bindClass.getStorageFile() != null) {
                    if (bindClass.getStorageFile() != null && bindClass.getStorageFile().getName().startsWith("temp-")) {
                        bindClass.setStorageFile(new File(bindClass.getStorageFile().getParentFile(), bindClass.getStorageFile().getName().substring(5)));
                    }
                    File temp = new File(bindClass.getStorageFile().getParentFile(), "temp-" + bindClass.getStorageFile().getName());
                    if (temp.exists()) {
                        bindClass.getStorageFile().delete();
                        temp.renameTo(bindClass.getStorageFile());
                    }
                }
            }
        }
    }

    /**
     * creates a local session.
     * @param user
     * @return
     * @throws SofofException 
     */
    public Session createLocalSession(User user) throws SofofException {
        return new LocalSession(user);
    }

    private class LocalSession extends Session {

        private User user;
        private ListInputStream in;
        private ListOutputStream out;

        public LocalSession(User user) throws SofofException {
            if (!users.contains(user)) {
                throw new SofofException(user.getName() + " access denied");
            }
            this.user = user;
            this.in = new DefaultListInputStream(db, bindTree, serializer);
            this.out = new DefaultListOutputStream(db, bindTree, serializer);
        }

        @Override
        public synchronized int execute(Executable exe) throws SofofException {
            Lock lock = readWriteLock.writeLock();
            try {
                lock.lock();
                checkExecutingPermission(user, exe);
                int result = (exe).execute(in, out);
                commit();
                return result;
            } catch (SofofException | SecurityException ex) {
                throw ex;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public synchronized List query(Query query) throws SofofException {
            Lock lock = readWriteLock.readLock();
            try {
                lock.lock();
                checkQueryingPermission(user, query);
                return query.query(in);
            } catch (SofofException | SecurityException ex) {
                throw ex;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void close() throws SofofException {}

    }

}
