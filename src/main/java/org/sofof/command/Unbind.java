/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import org.sofof.command.condition.Condition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.sofof.ListOutputStream;
import org.sofof.ListInputStream;

/**
 * unbind (delete) objects bound to a specific binding name
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
     * unbind all objects of the passed classes
     *
     * @param c
     */
    public Unbind(Class... c) {
        classes = new LinkedList<>(Arrays.asList(c));
    }

    /**
     * unbind passed objects by checking equality with stored objects
     *
     * @param objs 
     */
    public Unbind(Object... objs) {
        this(Arrays.asList(objs));
    }

    /**
     * unbind passed objects by checking equality with stored objects
     *
     * @param objs 
     */
    public Unbind(List objs) {
        objects = new LinkedList<>(objs);
    }
    
    /**
     * Specify the binding name that objects are bound to. If the name is empty space filled strings or null then the name will be converted to SofofNoName.
     *
     * @param bind binding name
     * @return this object
     */
    public Unbind from(String bind) {
        this.bind = bind;
        return this;
    }

    /**
     * Add a condition that objects must meet it to unbind them. Only apply if you choose the constructor with classes argument.
     *
     * @param condition
     * @return this object
     */
    public Unbind where(Condition condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException {
        int affected = 0;
        if (classes != null) {
            for (Class clazz : classes) {
                List list = in.read(bind, clazz);
                for (Object element : new ArrayList(list)) {
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
                    if (Objects.equals(obj, listObj)) {
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
