/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.Session;
import org.sofof.SofofException;
import org.sofof.annotation.Particle;
import org.sofof.command.condition.ObjectCondition;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h3>اللقطة</h3>
 * <p>
 * يقوم هذا الصف بالتقاط قيم حقول محددة لكائن معين, ويتم تخزينهم في قاعدة
 * البيانات, ويسمح بلقطة واحدة للصف الواحد.</p>
 * <p>
 * يستطيع المستخدم تحميل قيم تلك المتغيرات, ويتم تحديد المتغيرات باستخدام
 * {@link Particle}.</p>
 *<blockquote><pre>
 * class Game{
 * {@literal @}Particle public String player;
 * public Node world;
 * }
 * Session sess;
 * Game game;
 * ...
 * game.player = "FastManKiller";
 * game.world = new Node();
 * Capture.capture(sess, game);
 * ...
 * Game g = new Game();
 * Capture.load(sess, Game.class).copyTo(g);
 * System.out.printnln(g.player); 
 * System.out.printnln(g.world);
 * //output
 * FastManKiller
 * null
 * </pre></blockquote>
 * @author Rami Manaf Abdullah
 * @see Particle
 */
public class Capture implements Serializable {

    private static final long serialVersionUID = 743848439l;

    private Object obj;
    private long time;
    private Map<String, Object> fields;
    private Class clazz;

    /**
     * <p>
     * تقوم بأخذ لقطة لحالة الكائن, وتحفظها في قاعدة البيانات, ويتم حفظ لقطة
     * واحدة فقط للصف الواحد في قاعدة البيانات.</p>
     * <p>
     * اللقطة هي تسجيل لقيمة بعض المتغيرات في الكائن, والتي يتم تحديدها باستخدام
     * {@link org.sofof.capture.Particle}.</p>
     *
     * @param obj الكائن الذي سيتم التقاطه
     * @throws SofofException
     * @see Session#capture(java.lang.Object)
     * @see Capture
     * @see Particle
     */
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
                if (field.isAnnotationPresent(Particle.class)) {
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
     * <p>
     * تعيد الصف الخاص بالكائن الذي تم التقاطه.</p>
     *
     * @return الصف
     */
    public Class getCaptureClass() {
        return obj.getClass();
    }

    /**
     * <p>
     * تعيد زمن الالتقاط</p>
     *
     * @return الزمن بالميلي ثانية
     */
    public long captureTime() {
        return time;
    }

    /**
     * <p>
     * تقوم بنسخ قيم الحقول الملتقطة إلى الكائن الممرر.</p>
     *
     * @param past الكائن
     * @throws SofofException
     */
    public void copyTo(Object past) throws SofofException {
        if (!past.getClass().equals(obj.getClass())) {
            throw new SofofException("copying capture from class " + obj.getClass().getName() + " to object from " + past.getClass().getName());
        }
        for (Field field : past.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Particle.class)) {
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
     * تقوم بتحميل لقطة للصف من الجلسة
     * @param session الجلسة
     * @param clazz الصف الذي سيتم تحميل لقطته
     * @return لقطة الصف أو لا قيمة إذا لم تتواجد لقطة
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
     * تقوم بالتقاط لقطة للكائن وحفظها في قاعدة البيانات المتصلة بالجلسة
     * @param session الجلسة
     * @param obj الكائن الذي سيتك التقاط لقطة له
     * @return تعيد واحد إذا كان هناك لقطة قديمة, وصفر في غير ذلك
     * @throws SofofException 
     */
    public static int capture(Session session, Object obj) throws SofofException{
        if (obj == null) throw new NullPointerException("can not capture null object");
        int x = session.execute(new Unbind(Capture.class).from("SofofCapture").where(new ObjectCondition("#getCaptureClass()", Operation.Equal, obj.getClass())));
        session.execute(new Bind(new Capture(obj)).to("SofofCapture"));
        return x;
    }

}
