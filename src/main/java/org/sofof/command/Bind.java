/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.Session;
import org.sofof.SofofException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.sofof.ListOutputStream;
import org.sofof.ListInputStream;

/**
 * binds objects to binding name and saves them in the database. binding name should not have any signs can not be used as a file name in your system like /|\*:?" &lt; &gt;
 * if you try to query this object after executing it, it will return list of the same objects with the generated ID
 *
 * @author Rami Manaf Abdullah
 * @see Unbind
 * @see Update
 */
public class Bind implements Executable, Query, Serializable {

    private static final long serialVersionUID = 74849927l;

    private String bind;
    private List<Object> objects;
    
    /**
     * save objects in the database
     *
     * @param obj
     */
    public Bind(Object... obj) {
        this.objects = new LinkedList<>(Arrays.asList(obj));
    }
    
    /**
     * save objects in the database
     *
     * @param objs list of objects
     */
    public Bind(List objs) {
        this.objects = new LinkedList<>(objs);
    }

    /**
     * bind objects to passed binding name. if the name was null or not specified or space filled empty string the name will changes to SofofNoName
     *
     * @param bindingName
     * @return this object
     */
    public Bind to(String bindingName) {
        this.bind = bindingName;
        return this;
    }

    @Override
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException {
        if (objects.isEmpty()) {
            throw new SofofException("no objects passed to bind");
        }
        List list = in.read(bind, objects.get(0).getClass());
        list.addAll(objects);
        out.write(list, bind, objects.get(0).getClass());
        return objects.size();
    }

    @Override
    public List query(ListInputStream in) throws SofofException {
        if(objects.isEmpty())throw new SofofException("no objects to be recovered");
        List recovered = in.read(bind, objects.get(0).getClass());
        LinkedList matches = new LinkedList();
        objects.forEach((object) -> {
            if(recovered.contains(object))matches.add(recovered.get(recovered.indexOf(object)));
        });
        return matches;
    }
    
    /**
     * save objects and query them then return a list of those objects with ID generated
     * @param session session to execute on
     * @param bind
     * @return objects after saving and generating IDs
     * @see org.sofof.ID
     * @throws SofofException 
     */
    public static List bindAndReload(Session session, Bind bind) throws SofofException{
        session.execute(bind);
        return session.query(bind);
    }

}
