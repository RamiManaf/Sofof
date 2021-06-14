/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sofof.BindingNamesTree;
import org.sofof.ListInputStream;
import org.sofof.SequentialReader;
import org.sofof.SofofException;
import org.sofof.command.condition.Condition;

/**
 * used to get minimum values of a comparable extracted from an object using
 * executable expression.
 * @author Rami Manaf Abdullah
 */
public class Min implements Query, Serializable {

    private static final long serialVersionUID = 94683728l;

    private String bind;
    private Class clazz;
    private int count;
    private String expression;
    private Condition condition;

    public Min() {
    }

    /**
     * this will return objects with minimum values of the result of expression
     * execution
     *
     * @param clazz
     * @param count number of values to return
     * @param expression the expression that will be executed to get values that their minimum will be returned
     */
    public Min(Class clazz, int count, String expression) {
        if (count < 0) {
            throw new IllegalArgumentException("count: " + count + " cann't be negative");
        }
        this.clazz = clazz;
        this.count = count;
        this.expression = expression;
    }

    /**
     * Specify the binding name that objects are bound to. If the name is empty
     * space filled strings or null then the name will be converted to
     * SofofNoName
     *
     * @param bind binding name
     * @return this object
     */
    public Min from(String bind) {
        this.bind = bind;
        return this;
    }

    /**
     * Specify a condition that will be applied on all queried objects
     *
     * @param condition
     * @return this object
     */
    public Min where(Condition condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public List query(ListInputStream in) throws SofofException {
        bind = BindingNamesTree.parseNoName(bind);
        ArrayList objs = new ArrayList(count);
        if (expression.equals(in.getBindingNamesTree().getBindingName(bind).getBindingClass(clazz).getIndexExpression())) {
            Index.Indexes indexes = Index.getIndexes(bind, clazz, in);
            for (int i = 0; i < Math.min(indexes.size(), count); i++) {
                objs.add(indexes.get(i).getKey());
            }
            return objs;
        }
        ArrayList keys = new ArrayList(count);
        try ( SequentialReader reader = in.createSequentialReader(bind, clazz)) {
            Object obj;
            while ((obj = reader.read()) != null) {
                if (condition == null || condition.check(obj)) {
                    Comparable key = (Comparable) ExpressionExecuter.execute(expression, obj);
                    int insertionPoint = -(Collections.binarySearch(keys, key) + 1);
                    if (objs.size() < count) {
                        objs.add(insertionPoint, obj);
                        keys.add(insertionPoint, key);
                    } else if (insertionPoint != objs.size()) {
                        objs.add(insertionPoint, obj);
                        objs.remove(objs.size() - 1);
                        keys.add(insertionPoint, key);
                        keys.remove(keys.size() - 1);
                    }
                }
            }
            return objs;
        } catch (Exception ex) {
            throw new SofofException(ex);
        }
    }

}
