/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.util.List;

/**
 *
 * @author Rami Manaf Abdullah
 */
public interface ListInputStream {

    /**
     * reads objects of the passed class bound to the passed binding name
     * {@code 
     * List list = in.read("students", Student.class);
     * Student s = (Student)list.get(0);
     * }
     *
     * @param bindingName
     * @param c
     * @return return a list of the objects had been read
     * @throws SofofException
     */
    public List readAll(String bindingName, Class c) throws SofofException;
    
    /**
     * Create a {@link SequentialReader}.
     * @param bindingName
     * @param clazz
     * @return new Sequential reader for the bindingName and class passed
     * @throws SofofException 
     */
    public SequentialReader createSequentialReader(String bindingName, Class clazz) throws SofofException;
    
    public BindingNamesTree getBindingNamesTree();

}
