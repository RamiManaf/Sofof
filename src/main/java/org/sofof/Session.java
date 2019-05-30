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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import javax.net.ssl.SSLSocketFactory;

/**
 * <h3>الجلسة</h3>
 * <p>
 * تسمح الجلسة بتنفيذ الأوامر على قاعدة البيانات, ويتم بدء الجلسة من خلال كائن
 * قاعدة البيانات.</p>
 * <blockquote><pre>
 * Server s = new Server(new File(db), 6969, false).startUp();
 * s.addUser(new User("rami", "password"));
 * Database db = new Database("localhost", 6969);
 * try(Session session = db.startSession(new User("rami", "password"), false)){
 * .....
 * }
 * </pre></blockquote>
 *
 * @author Rami Manaf Abdullah
 * @see Database
 */
public class Session implements AutoCloseable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * @param host
     * @param port
     * @param u
     * @throws SofofException
     */
    Session(String host, int port, User u, boolean ssl) throws SofofException {
        try {
            socket = ssl?SSLSocketFactory.getDefault().createSocket(host, port):new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(u);
            if (!in.readBoolean()) {
                throw new SofofException("user " + u.getName() + " access denied to " + host + ":" + port);
            }
        } catch (IOException ex) {
            throw new SofofException("can not connect to " + host + ":" + port, ex);
        }
    }

    /**
     * تنفذ أمرا تنفيذيا على قاعدة البيانات
     *
     * @param exe الأمر التنفيذي
     * @return عدد الكائنات التي تأثرت بالأمر
     * @throws SofofException حدوث خطأ في الاتصال بالخادم
     */
    public synchronized int execute(Executable exe) throws SofofException {
        try {
            out.reset();
            out.writeObject(true);
            out.writeObject(exe);
            Object o = in.readObject();
            if(o instanceof SofofException)throw (SofofException)o;
            else if (o instanceof SecurityException) throw (SecurityException)o;
            else return (int)o;
        } catch (IOException ex) {
            throw new SofofException("can not execute on " + socket.getInetAddress().getHostName() + ":", ex);
        } catch (ClassNotFoundException ex) {
            throw new SofofException("class not found in the classpath", ex);
        }
    }

    /**
     * تنفيذ استعلام
     *
     * @param q الاستعلام
     * @return تعيد قائمة بالكائنات المستعلم عنها أو قائمة فارغة, لا تعيد أبدا لا قيمة
     * @throws SofofException حدوث خطأ في الاتصال بالخادم
     */
    public synchronized List query(Query q) throws SofofException {
        try {
            out.reset();
            out.writeObject(false);
            out.writeObject(q);
            Object o = in.readObject();
            if(o instanceof SofofException) throw (SofofException)o;
            else if (o instanceof SecurityException) throw (SecurityException)o;
            else return (List)o;
        } catch (IOException ex) {
            throw new SofofException("can not query on " + socket.getInetAddress().getHostName(), ex);
        } catch (ClassNotFoundException ex) {
            throw new SofofException("class not found in the classpath", ex);
        }
    }

    /**
     * تقوم بإغلاق الجلسة
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
