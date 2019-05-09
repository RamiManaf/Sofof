/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.Session;
import org.sofof.SofofException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.sofof.ListInputStream;
import org.sofof.ListOutputStream;

/**
 * <h3>أمر ربط</h3>
 * يقوم بربط كائن باسم ربط وتخزينه في قاعدة البيانات, ويمكن لاسم الربط أن يكون
 * باللغة العربية أو أي لغة أخرى شرط أن لا يحتوي على أي من /|\*:?" &lt; &gt;
 *
 * @author Rami Manaf Abdullah
 * @see Unbind
 * @see Update
 */
public class Bind implements Executable, Query, Serializable {

    private static final long serialVersionUID = 74849927l;

    private String bind;
    private List<Object> objects;

    /**
     * يقوم بربط الكائنات باسم ربط وتخزينهم
     *
     * @param obj الكائنات
     */
    public Bind(Object... obj) {
        this.objects = new LinkedList<>(Arrays.asList(obj));
    }

    /**
     * يقوم بربط الطائنات باسم ربط وتخزينهم
     *
     * @param objs قائمة بالكائنات التي سيتم ربطها
     */
    public Bind(List objs) {
        this.objects = new LinkedList<>(objs);
    }

    /**
     * يحدد اسم الربط الذي سيتم ربط الكائنات به إذا لم يتم تحديد اسم الربط أو تم
     * تمرير اللا قيمة أو تم تمرير نص يتكون من المسافات فقط سيتم ربط الكائنات
     * بالا اسم
     *
     * @param bind اسم الربط
     * @return الكائن نفسه
     */
    public Bind to(String bind) {
        this.bind = bind;
        return this;
    }

    @Override
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException {
        if (objects.isEmpty()) {
            throw new SofofException("no objects passed to bind");
        }
        List list = in.read(bind, objects.get(0).getClass());
        list.addAll(objects);
        out.write(list, bind, objects.get(0).getClass());
        return objects.size();
    }

    @Override
    public List query(ListInputStream in) throws SofofException {
        if(objects.isEmpty())throw new SofofException("no objects to be recovered");
        LinkedList recovered = in.read(bind, objects.get(0).getClass());
        LinkedList matches = new LinkedList();
        objects.forEach((object) -> {
            if(recovered.contains(object))matches.add(recovered.get(recovered.indexOf(object)));
        });
        return matches;
    }
    
    /**
     * تستخدم لربط الكائن ثم إعادة قراءته بعد توليد المعرفات إن وجدت
     * @param session الجلسة التي سيتم تنفيذ الأمر عليها
     * @param bind الأمر
     * @return قائمة بالكائنات التي تمت قراءتها
     * @throws SofofException 
     */
    public static List bindAndReload(Session session, Bind bind) throws SofofException{
        session.execute(bind);
        return session.query(bind);
    }

}
