/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import org.sofof.command.condition.Condition;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.sofof.ListInputStream;
import org.sofof.ListOutputStream;

/**
 * تحديث الكائنات المحددة
 *
 * @author Rami Manaf Abdullah
 */
public class Update implements Executable, Serializable {

    private static final long serialVersionUID = 3727839l;

    private Object object;
    private Object update;
    private String bind;
    private Class clazz;
    private Condition condition;

    /**
     * إنشاء محدث لكائنات الصف المحدد
     *
     * @param c الصف
     */
    public Update(Class c) {
        clazz = c;
    }

    /**
     * إنشاء محدث للكائن الممرر
     *
     * @param obj الكائن
     */
    public Update(Object obj) {
        object = obj;
    }

    /**
     * يحدد اسم الربط الذي سيتم تحديث الكائنات المرتبطة به إذا لم يتم تحديد اسم
     * الربط أو تم تمرير اللا قيمة أو تم تمرير نص يتكون من الفراغات سيتم تحديق
     * الكائنات المرتبطة باسم الربط اللا اسم
     *
     * @param bind اسم الربط
     * @return الكائن نفسه
     */
    public Update from(String bind) {
        this.bind = bind;
        return this;
    }

    /**
     * تحدد الكائن الذي سيتم وضعه بدل الكائنات التي تنطبق عليها الشروط
     *
     * @param update الكائن الجديد
     * @return الكائن نفسه
     */
    public Update set(Object update) {
        this.update = update;
        return this;
    }

    /**
     * تضيف شرطا يوجب على الكائنات تحقيقه لتحديثها
     *
     * @param cond الشرط
     * @return الكائن نفسه
     */
    public Update where(Condition cond) {
        condition = cond;
        return this;
    }

    @Override
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException {
        int affected = 0;
        if (clazz != null) {
            List list = in.read(bind, clazz);
            for (int x = 0; x < list.size(); x++) {
                if (condition == null || condition.check(list.get(x))) {
                    list.set(x, update);
                    affected++;
                }
            }
            out.write(list, bind, clazz);
        } else {
            List list = in.read(bind, object.getClass());
            for (int x = 0; x < list.size(); x++) {
                if (Objects.equals(list.get(x), object)) {
                    list.set(x, update);
                    affected++;
                }
            }
            out.write(list, bind, object.getClass());
        }
        return affected;
    }

}
