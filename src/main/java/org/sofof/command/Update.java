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
import org.sofof.ListOutputStream;
import org.sofof.ListInputStream;

/**
 * Update objects with new ones
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
     * Updates objects with the specified class
     *
     * @param c
     */
    public Update(Class c) {
        clazz = c;
    }

    /**
     * Updates all objects from the object class that equals it
     *
     * @param obj
     */
    public Update(Object obj) {
        object = obj;
    }

    /**
     * Specify the binding name that objects are bound to. If the name is empty space filled strings or null then the name will be converted to SofofNoName.
     *
     * @param bind binding name
     * @return this object
     */
    public Update from(String bind) {
        this.bind = bind;
        return this;
    }

    /**
     * Specify the new object that will replace the selected objects
     *
     * @param update new object
     * @return this object
     */
    public Update set(Object update) {
        this.update = update;
        return this;
    }

    /**
     * Add a condition that will be applied on objects to update them. Only for the constructor with class argument.
     *
     * @param condition 
     * @return this object
     */
    public Update where(Condition condition) {
        this.condition = condition;
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
