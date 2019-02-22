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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 *<h3>مرتب الكائنات</h3>
 * 
 * يقوم بترتيب الكائنات حسب القيمة الناتجة من{@link ObjectCondition النص التنفيذي}
 * ويمكن أن تكون القيمة الناتجة نصا أو رقما أو تاريخا {@link java.util.Date} {@link java.time.LocalDate} فقط
 * @author Rami Manaf Abdullah
 */
public class ObjectSorter implements Sorter, Serializable {

    private static final long serialVersionUID = 97097243l;
    
    private String parameter;
    private Order order;

    /**
     *ترتب العناصر حسب الناتج من النص التنفيذي بالترتيب الممرر
     * @param parameter النص التنفيذي
     * @param order الترتيب
     */
    public ObjectSorter(String parameter, Order order) {
        this.parameter = parameter;
        this.order = order;
    }

    @Override
    public void sort(List list) throws SofofException{
        for (int x = 1; x < list.size(); x++) {
            Object o =  execute(parameter, list.get(x));
            Object o2 = execute(parameter, list.get(x-1));
             if(o instanceof Comparable){
                if(((Comparable)o).compareTo((Comparable)o2)==-1){
                    replace(x, x-1, list);
                }
            }else if(o instanceof LocalDate){
                if(((LocalDate)o).isBefore((LocalDate)o2)){
                    replace(x, x-1, list);
                }
            }else{
                throw new SofofException("unsortable class "+o.getClass().getName());
            }
        }
        if(order.equals(Order.Descending)){
            Collections.reverse(list);
        }
    }
    
    private void replace(int x, int y, List list){
        Object temp = list.get(x);
        list.set(x, list.get(y));
        list.set(y, temp);
    }

}
