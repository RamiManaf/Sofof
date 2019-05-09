/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import org.sofof.command.condition.Condition;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.sofof.ListInputStream;
import org.sofof.ListOutputStream;

/**
 * أمر إلغاء ربط الكائنات المحددة باسم الربط
 *
 * @author Rami Manaf Abdullah
 * @see Bind
 * @see Unbind
 * @see Update
 */
public class Unbind implements Executable, Serializable {

    private static final long serialVersionUID = 7993234234l;

    private List<Object> objects;
    private String bind;
    private List<Class> classes;
    private Condition condition;

    /**
     * إنشاء أمر إلغاء الربط بالصفوف المحددة
     *
     * @param c الصفوف
     */
    public Unbind(Class... c) {
        classes = Arrays.asList(c);
    }

    /**
     * إنشاء أمر إلغاء ربط الكائنات المحددة
     *
     * @param objs الكائنات
     */
    public Unbind(Object... objs) {
        objects = new LinkedList<>(Arrays.asList(objs));
    }

    /**
     *
     * @param objs
     */
    public Unbind(List objs) {
        objects = objs;
    }

    /**
     * يحدد اسم الربط الذي سيتم إلغاء ربط الكائنات به إذا لم يتم تحديد اسم الربط
     * أو تم تمرير اللا قيمة أو تم تمرير نص يتكون من فراغات فقط سيتم إلغاء ربط
     * الكائنات من اسم الربط اللا اسم
     *
     * @param bind اسم الربط
     * @return الكائن نفسه
     */
    public Unbind from(String bind) {
        this.bind = bind;
        return this;
    }

    /**
     * تقوم بإضافة شرط ليتم إلغاء ربط الكائنات التي تحققه
     *
     * @param cond الشرط
     * @return الكائن نفسه
     */
    public Unbind where(Condition cond) {
        condition = cond;
        return this;
    }

    @Override
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException {
        int affected = 0;
        if (classes != null) {
            for (Class clazz : classes) {
                LinkedList list = in.read(bind, clazz);
                for (Object element : (List) list.clone()) {
                    if (condition == null || condition.check(element)) {
                        list.remove(element);
                        affected++;
                    }
                }
                out.write(list, bind, clazz);
            }
        } else {
            if (objects.isEmpty()) {
                throw new SofofException("no objects passed to unbind");
            }
            List list = in.read(bind, objects.get(0).getClass());
            for (Object obj : objects) {
                for (Object listObj : list) {
                    if (listObj.equals(obj)) {
                        list.remove(listObj);
                        affected++;
                    }
                }
            }
            out.write(list, bind, objects.get(0).getClass());
        }
        return affected;
    }
}
