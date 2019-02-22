/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

/**
 *<h3>استثناء صفوف</h3>
 * يتم إطلاقه في حالة حدوث أي أمر غير متوقع
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
