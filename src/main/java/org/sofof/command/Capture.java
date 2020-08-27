/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.Session;
import org.sofof.SofofException;
import org.sofof.command.condition.ObjectCondition;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sofof.annotation.Capturable;

/**
 * This command capture certain variables in the object and stores them in the database. There is one allowed capture of the object class to be stored in the database so
 * every time you store a new capture the old one get deleted. you can specify the fields to be saved using {@link Capturable}.
 *<blockquote><pre>
 * class Game{
 * {@literal @}Capturable public String player;
 public Node world;
 }
 Session sess;
 Game game;
 ...
 game.player = "FastManKiller";
 game.world = new Node();
 Capture.capture(session, game);
 ...
 Game g = new Game();
 Capture.load(sess, Game.class).copyTo(g);
 System.out.printnln(g.player); 
 System.out.printnln(g.world);
 //output
 FastManKiller
 null
 </pre></blockquote>
 * @author Rami Manaf Abdullah
 * @see Capturable
 */
public class Capture implements Serializable {

    private static final long serialVersionUID = 743848439l;

    private Object obj;
    private long time;
    private Map<String, Object> fields;
    
    private Capture(Object obj) throws SofofException {
        fields = new HashMap<>();
        this.obj = obj;
        time = System.currentTimeMillis();
        loadFields(obj);
    }

    private void loadFields(Object o) throws SofofException {
        Class c = o.getClass();
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(Capturable.class)) {
                    field.setAccessible(true);
                    try {
                        fields.put(field.getName(), field.get(o));
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        throw new SofofException("", ex);
                    }
                }
            }
            c = c.getSuperclass();
        }
    }

    /**
     *
     * @return class of the captured object
     */
    public Class getCaptureClass() {
        return obj.getClass();
    }

    /**
     * @return capture time in millisecond
     */
    public long captureTime() {
        return time;
    }

    /**
     * copy fields data of the captured object to the passed object
     *
     * @param past object to inject fields data in
     * @throws SofofException
     */
    public void copyTo(Object past) throws SofofException {
        if (!past.getClass().equals(obj.getClass())) {
            throw new SofofException("copying capture from class " + obj.getClass().getName() + " to object from " + past.getClass().getName());
        }
        for (Field field : past.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Capturable.class)) {
                field.setAccessible(true);
                try {
                    field.set(past, fields.get(field.getName()));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new SofofException("", ex);
                }
            }
        }
    }

    /**
     * load a saved capture of the passed class
     * @param session session to execute on
     * @param clazz class which a capture of it will be returned
     * @return capture or null if there is no capture available for that class
     * @throws SofofException 
     */
    public static Capture load(Session session, Class clazz) throws SofofException {
        if (clazz == null) {
            throw new NullPointerException("can not query capture from null class");
        }
        List list = session.query(new Select(Capture.class).from("SofofCapture").where(new ObjectCondition("#getCaptureClass()", Operation.Equal, clazz)));
        if(list.isEmpty())return null;
        else return (Capture) list.get(0);
    }
    
    /**
     * capture {@link Capturable} annotated fields and store them in the database
     * @param session session to execute on
     * @param obj object that its fields will be captured
     * @return true if there was an old capture has been deleted
     * @throws SofofException 
     */
    public static boolean capture(Session session, Object obj) throws SofofException{
        if (obj == null) throw new NullPointerException("can not capture null object");
        int x = session.execute(new Unbind(Capture.class).from("SofofCapture").where(new ObjectCondition("#getCaptureClass()", Operation.Equal, obj.getClass())));
        session.execute(new Bind(new Capture(obj)).to("SofofCapture"));
        return x==1;
    }

}
