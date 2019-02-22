/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.ListInputStream;
import org.sofof.SofofException;
import org.sofof.command.condition.Condition;
import org.sofof.command.sorter.Sorter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * تحديد يقوم بتحديد الكائنات التي سيتم الاستعلام عنها ضمن الشروط المحددة
 * والترتيب المحدد
 *
 * @author Rami Manaf Abdullah
 */
public class Select implements Query, Serializable {

    private static final long serialVersionUID = 94683728l;

    private String bind;
    private Class clazz;
    private String expression;
    private Condition condition;
    private Sorter sorter;

    /**
     * تنشئ استعلام عن الصفوف المحددة
     *
     * @param c الصفوف
     */
    public Select(Class c) {
        clazz = c;
    }

    /**
     * تنشئ استعلام عن نواتج تنفيذ النص على كائنات الصف الممرر
     * <blockquote><pre>
     * List marks = session.query(new Select(Student.class, "#getMark()"));
     * </pre></blockquote>
     *
     * @param c الصف
     * @param expression النص التنفيذي
     */
    public Select(Class c, String expression) {
        clazz = c;
        this.expression = expression;
    }

    /**
     * يحدد اسم الربط الذي سيتم الاستعلام عن الكائنات المربوطة به إذا لم يتم
     * تحديد اسم الربط أو تم تمرير اللا قيمة أو تم تمرير نص يتكون من الفراغات
     * فسيتم الاستعلام عن الكائنات المربوطة باسم الربط اللا اسم
     *
     * @param bind اسم الربط
     * @return الكائن نفسه
     */
    public Select from(String bind) {
        this.bind = bind;
        return this;
    }

    /**
     * تقوم بتحديد شرط ليتم تنفيذه على جميع الكائنات المستعلم عنها
     *
     * @param cond الشرط
     * @return الكائن نفسه
     */
    public Select where(Condition cond) {
        condition = cond;
        return this;
    }

    /**
     * تقوم بتحديد مرتب
     *
     * @param s المرتب
     * @return الكائن نفسه
     */
    public Select sort(Sorter s) {
        sorter = s;
        return this;
    }

    @Override
    public List query(ListInputStream in) throws SofofException {
        LinkedList objs = in.read(bind, clazz);
        if (condition != null) {
            for (Object obj : (List) objs.clone()) {
                if (!condition.check(obj)) {
                    objs.remove(obj);
                }
            }
        }
        if (sorter != null) {
            sorter.sort(objs);
        }
        if (expression != null) {
            LinkedList result = new LinkedList();
            for (Object obj : objs) {
                result.add(ExpressionExecuter.execute(expression, obj));
            }
            return result;
        } else {
            return objs;
        }
    }

}
