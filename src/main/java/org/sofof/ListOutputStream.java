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
public interface ListOutputStream {

    /**
     * write the objects list to the database and any old objects stored previously will be removed
     * {@code 
     * List<Student> list;
     * ...
     * out.write(list, "Students", Student.class);
     * }
     *
     * @param objects
     * @param bindingName
     * @param clazz class which objects are instances of
     * @return objects after IDs has been generated
     * @see org.sofof.ID
     * @throws org.sofof.SofofException
     */
    public List write(List objects, String bindingName, Class clazz) throws SofofException;

    /**
     * start a transaction
     * @see ListOutputStream#commit() 
     */
    public void startTransaction();

    /**
     * commit the changes in the database
     * @see ListOutputStream#startTransaction() 
     */
    public void commit();

    /**
     * cancels the transaction
     * @see ListOutputStream#commit() 
     */
    public void rollback();
    
}
