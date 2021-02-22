/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import org.sofof.command.condition.Condition;
import org.sofof.command.sorter.Sorter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sofof.ListInputStream;
import org.sofof.SequentialReader;

/**
 * Can query for objects with certain conditions and in sorting way
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
    private boolean shuffle;

    private Select() {
    }

    /**
     * Query for all objects of that class
     *
     * @param c
     */
    public Select(Class c) {
        this(c, null);
    }

    /**
     * Query for result of executed expression on the selected objects
     * <blockquote><pre>
     * List marks = session.query(new Select(Student.class, "#getMark()"));
     * </pre></blockquote>
     *
     * @param c
     * @param expression expression that will be executed on the selected
     * objects
     * @see ExpressionExecuter
     */
    public Select(Class c, String expression) {
        this(c, expression, false);
    }

    /**
     * Query for result of executed expression on the selected objects
     * <blockquote><pre>
     * List marks = session.query(new Select(Student.class, "#getMark()"));
     * </pre></blockquote>
     *
     * @param c
     * @param expression expression that will be executed on the selected
     * objects
     * @param shuffle if true the query will shuffle the data before applying
     * conditions to it. This is useful if you want to select limited random
     * objects.
     * @see ExpressionExecuter
     */
    public Select(Class c, String expression, boolean shuffle) {
        clazz = c;
        this.expression = expression;
        this.shuffle = shuffle;
    }

    /**
     * Specify the binding name that objects are bound to. If the name is empty
     * space filled strings or null then the name will be converted to
     * SofofNoName
     *
     * @param bind binding name
     * @return this object
     */
    public Select from(String bind) {
        this.bind = bind;
        return this;
    }

    /**
     * Specify a condition that will be applied on all queried objects
     *
     * @param condition
     * @return this object
     */
    public Select where(Condition condition) {
        this.condition = condition;
        return this;
    }

    /**
     * Specify a sorter to sort queried objects
     *
     * @param sorter sorter
     * @return this object
     */
    public Select sort(Sorter sorter) {
        this.sorter = sorter;
        return this;
    }

    @Override
    public List query(ListInputStream in) throws SofofException {
        List objs;
        if (shuffle || condition == null) {
            objs = in.readAll(bind, clazz);
            if(shuffle)Collections.shuffle(objs);
            if (condition != null) {
                for (Object obj : new ArrayList(objs)) {
                    if (!condition.check(obj)) {
                        objs.remove(obj);
                    }
                }
            }
        } else {
            objs = new ArrayList();
            Object obj;
            try (SequentialReader reader = in.createSequentialReader(bind, clazz)) {
                while ((obj = reader.read()) != null) {
                    if (condition.check(obj)) {
                        objs.add(obj);
                    }
                }
            }catch(Exception ex){
                throw new SofofException(ex);
            }
        }
        if (sorter != null) {
            sorter.sort(objs);
        }
        if (expression != null) {
            ArrayList result = new ArrayList();
            for (Object obj : objs) {
                result.add(ExpressionExecuter.execute(expression, obj));
            }
            return result;
        } else {
            return objs;
        }
    }

}
