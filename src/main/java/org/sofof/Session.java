/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.command.Executable;
import org.sofof.command.Query;
import org.sofof.permission.User;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import javax.net.ssl.SSLSocketFactory;
import org.sofof.serializer.Serializer;

/**
 * allow remote users to execute commands on the database. you can start a new session from SessionManager
 * <blockquote><pre>
 * Server s = new Server(new File(db), 6969, false);
 * s.createDatabase();
 * s.startUp();
 * s.getUsers().add(new User("rami", "password"));
 * try(Session session = SessionManager.startSession("sofof:localhost:6969", new User("rami", "password"))){
 * .....
 * }
 * </pre></blockquote>
 *
 * @author Rami Manaf Abdullah
 */
public class Session implements AutoCloseable {

    public static final byte BOOLEAN_TRUE = 1;
    public static final byte BOOLEAN_FALSE = 0;
    public static final byte COMMAND_EXECUTABLE = 2;
    public static final byte COMMAND_QUERY = 3;

    private Socket socket;
    private Serializer serializer;

    Session() {}
    
    /**
     * @param host
     * @param port
     * @param user
     * @throws SofofException
     */
    Session(String host, int port, Serializer serializer, User user, boolean ssl) throws SofofException {
        try {
            this.serializer = serializer;
            socket = ssl ? SSLSocketFactory.getDefault().createSocket(host, port) : new Socket(host, port);
            serializer.serialize(user, socket.getOutputStream());
            if (socket.getInputStream().read() != BOOLEAN_TRUE) {
                close();
                throw new SofofException(" access denied from " + host + ":" + port + " to " + user.getName());
            }
        } catch (IOException ex) {
            throw new SofofException("can not connect to " + host + ":" + port, ex);
        }
    }

    /**
     * execute an Executable on the database
     *
     * @param exe
     * @return usually number of affected objects
     * @throws SofofException connection or executing error
     */
    public synchronized int execute(Executable exe) throws SofofException {
        try {
            socket.getOutputStream().write(COMMAND_EXECUTABLE);
            serializer.serialize(exe, socket.getOutputStream());
            Object result = serializer.deserialize(socket.getInputStream());
            if (result instanceof SofofException) {
                throw (SofofException) result;
            } else if (result instanceof SecurityException) {
                throw (SecurityException) result;
            } else {
                return (int) result;
            }
        } catch (IOException ex) {
            throw new SofofException("can not execute on " + socket.getInetAddress().getHostName() + ":" + socket.getPort(), ex);
        } catch (ClassNotFoundException ex) {
            throw new SofofException(ex);
        }
    }

    /**
     * execute a query
     *
     * @param query
     * @return list of queried objects. never null
     * @throws SofofException
     */
    public synchronized List query(Query query) throws SofofException {
        try {
            socket.getOutputStream().write(COMMAND_QUERY);
            serializer.serialize(query, socket.getOutputStream());
            Object result = serializer.deserialize(socket.getInputStream());
            if (result instanceof SofofException) {
                throw (SofofException) result;
            } else if (result instanceof SecurityException) {
                throw (SecurityException) result;
            } else {
                return (List) result;
            }
        } catch (IOException ex) {
            throw new SofofException("can not query on " + socket.getInetAddress().getHostName(), ex);
        } catch (ClassNotFoundException ex) {
            throw new SofofException(ex);
        }
    }

    /**
     * close the session
     *
     * @throws org.sofof.SofofException
     */
    @Override
    public void close() throws SofofException {
        try {
            socket.close();
        } catch (IOException ex) {
            throw new SofofException("error when closing session socket", ex);
        }
    }

}
