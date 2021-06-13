/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

/**
 * This interface enable you to read stored objects one by one so they could be
 * cleared from the RAM if you don't want all of them. Objects of this class
 * must be closed to release resources. If you want to create a sequential
 * reader and writer for the same binding name and class at the same time you
 * have to create the writer then the reader and close the reader before the
 * writer.
 *
 * @author Rami Manaf Abdullah
 */
public interface SequentialReader extends AutoCloseable {

    /**
     * Reads the next object
     *
     * @return
     * @throws SofofException
     */
    public Object read() throws SofofException;
    
    public void skip() throws SofofException, ClassNotFoundException;

}
