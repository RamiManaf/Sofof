/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.sorter;

import org.sofof.SofofException;
import static org.sofof.command.ExpressionExecuter.execute;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Sort objects on the server based on the results of executing an expression {@link org.sofof.command.ExpressionExecuter} on objects.
 * The result could be any object that extends {@link Comparable}.
 *
 * @author Rami Manaf Abdullah
 */
public class ObjectSorter implements Sorter, Serializable {

    private static final long serialVersionUID = 97097243l;

    private String parameter;
    private Order order;

    private ObjectSorter(){}
    
    /**
     * Sort objects based on executing the expression 
     *
     * @param expression
     * @param order
     */
    public ObjectSorter(String expression, Order order) {
        this.parameter = expression;
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
