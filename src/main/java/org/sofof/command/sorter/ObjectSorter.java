/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.sorter;

import org.sofof.SofofException;
import static org.sofof.command.ExpressionExecuter.execute;
import org.sofof.command.condition.ObjectCondition;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * <h3>مرتب الكائنات</h3>
 *
 * يقوم بترتيب الكائنات حسب القيمة الناتجة
 * من{@link ObjectCondition النص التنفيذي} ويمكن أن تكون القيمة الناتجة نصا أو
 * رقما أو تاريخا {@link java.util.Date} {@link java.time.LocalDate} فقط
 *
 * @author Rami Manaf Abdullah
 */
public class ObjectSorter implements Sorter, Serializable {

    private static final long serialVersionUID = 97097243l;

    private String parameter;
    private Order order;

    /**
     * ترتب العناصر حسب الناتج من النص التنفيذي بالترتيب الممرر
     *
     * @param parameter النص التنفيذي
     * @param order الترتيب
     */
    public ObjectSorter(String parameter, Order order) {
        this.parameter = parameter;
        this.order = order;
    }

    @Override
    public void sort(List list) throws SofofException {
        list.sort((o1, o2) -> {
            try {
                Object result1 = execute(parameter, o1);
                Object result2 = execute(parameter, o2);
                if (result1 == null) {
                    return result2 == null ? 0 : -1;
                } else if (result2 == null) {
                    return 1;
                } else {
                    return ((Comparable) result1).compareTo(result2);
                }
            } catch (SofofException ex) {
                throw new RuntimeException(ex);
            }
        });
        if (order.equals(Order.Descending)) {
            Collections.reverse(list);
        }
    }

}
