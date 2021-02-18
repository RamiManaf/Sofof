/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

/**
 * SofofException is thrown when any error in sofof appear
 *@author Rami Manaf Abdullah
 */
public class SofofException extends Exception{

    /**
     *
     * @param message
     */
    public SofofException(String message) {
        super(message);
    }

    /**
     *
     * @param ex
     * @param th
     */
    public SofofException(String ex, Throwable th) {
        super(ex, th);
    }

    public SofofException() {
        super();
    }
    
    public SofofException(Throwable th){
        super(th);
    }
    
}
