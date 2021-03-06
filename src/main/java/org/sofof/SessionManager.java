/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.sofof.permission.User;
import org.sofof.serializer.JavaSerializer;
import org.sofof.serializer.JsonSerializer;
import org.sofof.serializer.Serializer;
import org.sofof.serializer.SofofSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class SessionManager {

    private static ArrayList<Serializer> serializers = new ArrayList<Serializer>() {
        {
            add(new JavaSerializer());
            add(new SofofSerializer());
            add(new JsonSerializer());
        }
    };
    private static HashMap<String, Session> sessions = new HashMap<>();

    private static List<Session> pool;

    /**
     * register a serializer so can be used in sessions referencing its name
     *
     * @param serializer
     */
    public static void registerSerializer(Serializer serializer) {
        serializers.add(Objects.requireNonNull(serializer));
    }

    /**
     * removes the serializer by its name
     *
     * @param name
     * @return true if removed or false if it was not found
     */
    public static boolean removeSerializer(String name) {
        for (int i = 0; i < serializers.size(); i++) {
            if (serializers.get(i).getName().equals(name)) {
                serializers.remove(i);
                return true;
            }
        }
        return false;
    }

    public static List<Serializer> getSerializers() {
        return Collections.unmodifiableList(serializers);
    }

    /**
     * starts a new session
     *
     * @param url serializer:host:port
     * @param user user name which will be used to login and execute commands
     * @param ssl use ssl
     * @return the new connected session
     * @throws SofofException
     */
    public static Session startSession(String url, User user, boolean ssl) throws SofofException {
        String[] components = url.split(":");
        if (components.length != 3) {
            throw new IllegalArgumentException("the url " + url + " has inappropriate schema. expected serializer:host:port");
        }
        for (Serializer serializer : serializers) {
            if (serializer.getName().equals(components[0])) {
                return new Session(components[1], Integer.parseInt(components[2]), serializer, user, ssl);
            }
        }
        throw new SofofException("there is no serializer with the name " + components[0]);
    }

    /**
     * starts a new session
     *
     * @param url serializer:host:port
     * @param user user name which will be used to login and execute commands
     * @return the new connected session
     * @throws SofofException
     */
    public static Session startSession(String url, User user) throws SofofException {
        return startSession(url, user, false);
    }

    /**
     * prepare sessions from xml file and starts them
     *
     * @throws SofofException
     */
    public static void configure() throws SofofException {
        if (SessionManager.class.getResource("/sofof.xml") != null) {
            try {
                Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(SessionManager.class.getResourceAsStream("/sofof.xml")).getDocumentElement();
                NodeList nodes = root.getElementsByTagName("sessions");
                if (nodes.getLength() != 0) {
                    Element sessionsEl = (Element) nodes.item(0);
                    for (int i = 0; i < sessionsEl.getElementsByTagName("session").getLength(); i++) {
                        Element session = (Element) sessionsEl.getElementsByTagName("session").item(i);
                        Element user = (Element) session.getElementsByTagName("user").item(0);
                        sessions.put(session.getAttribute("name"), startSession(session.getAttribute("url"), new User(user.getAttribute("name"), user.getAttribute("password")), session.getAttribute("ssl").isEmpty() ? false : Boolean.valueOf(session.getAttribute("ssl"))));
                    }
                }
                NodeList pools = root.getElementsByTagName("pool");
                if (pools.getLength() > 0) {
                    //currently only support one pool
                    Element poolElement = (Element) pools.item(0);
                    int size = Integer.parseInt(poolElement.getAttribute("size"));
                    String url = poolElement.getAttribute("url");
                    Element userElement = (Element) poolElement.getElementsByTagName("user").item(0);
                    User user = new User(userElement.getAttribute("name"), userElement.getAttribute("password"));
                    createPool(size, url, user, poolElement.getAttribute("ssl").isEmpty() ? false : Boolean.valueOf(poolElement.getAttribute("ssl")));
                }
            } catch (IOException ex) {
                throw new SofofException("unable to read sofof.xml", ex);
            } catch (SAXException ex) {
                throw new SofofException("couldn't parse to sofof.xml", ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * get a session prepared in sofof.xml by its name
     *
     * @param name session name
     * @return session object or null if there is no session with that name
     */
    public static Session getSession(String name) {
        return sessions.get(name);
    }

    /**
     * creates a pool of specific size
     *
     * @param poolSize number of opened sessions
     * @param url serializer:host:port
     * @param user user name which will be used to login and execute commands
     * @param ssl use ssl
     * @throws SofofException
     */
    public static void createPool(int poolSize, String url, User user, boolean ssl) throws SofofException {
        pool = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < poolSize; i++) {
            pool.add(startSession(url, user, ssl));
        }
    }

    /**
     * creates a pool of local sessions
     *
     * @param poolSize number of opened sessions
     * @param server server instance
     * @param user user name which will be used to login and execute commands
     * @throws SofofException
     */
    public static void createPool(int poolSize, Server server, User user) throws SofofException {
        pool = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < poolSize; i++) {
            pool.add(server.createLocalSession(user));
        }
    }

    /**
     * try to get a pooled session in a specific time out period. if the period
     * has ended and there is no available session the method will return null
     *
     * @param timeout time in millisecond
     * @return
     */
    public static Session getPooledSession(int timeout) {
        if (pool == null) {
            throw new RuntimeException("pool has not been created");
        }
        synchronized (pool) {
            if (pool.isEmpty()) {
                try {
                    pool.wait(timeout);
                    return null;
                } catch (InterruptedException ex) {
                    return null;
                }
            }
            return pool.remove(0);
        }
    }

    /**
     * get a pooled session and if there is no one available now it will wait
     * until there is one release
     *
     * @return
     */
    public static Session getPooledSession() {
        if (pool == null) {
            throw new RuntimeException("pool has not been created");
        }
        synchronized (pool) {
            while (pool.isEmpty()) {
                try {
                    pool.wait();
                } catch (InterruptedException ex) {
                    return null;
                }
            }
            return pool.remove(0);
        }
    }

    /**
     * release a session so it can be used in other places
     * @param session 
     */
    public static void release(Session session) {
        synchronized (pool) {
            pool.add(session);
            pool.notify();
        }
    }

}
