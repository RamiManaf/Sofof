/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

/**
 * This interface enable you to write objects one by one so you don't have to
 * load all of them at the same time.Objects of this class must be closed to
 * release resources. If you want to create a sequential reader and writer for
 * the same binding name and class at the same time you have to create the
 * writer then the reader and close the reader before the writer.
 *
 * @author Rami Manaf Abdullah
 */
public interface SequentialWriter extends AutoCloseable {

    /**
     * Write the object.
     *
     * @param obj
     * @return
     * @throws SofofException
     */
    public ID write(Object obj) throws SofofException;

}
