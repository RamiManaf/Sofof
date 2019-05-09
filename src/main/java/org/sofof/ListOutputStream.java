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
     * تكتب قائمة في قاعدة البيانات وتربطها باسم الربط 
     * {@code 
     * List<Student> list;
     * ...
     * out.write(list, "Students", Student.class);
     * }
     *
     * @param objects القائمة
     * @param bind اسم الربط
     * @param clazz الصف الذي تتكون منه القائمة
     * @return تعيد الصفوف بعد توليد معرف لها
     * @throws org.sofof.SofofException
     */
    public List<Object> write(List<Object> objects, String bind, Class clazz) throws SofofException;

    /**
     * تقوم ببدء نقلة
     * @see ListOutputStream#commite() 
     */
    public void startTransaction();

    /**
     * تقوم بإنهاء النقلة وتحقيقها
     * @see ListOutputStream#startTransaction() 
     */
    public void commite();

    /**
     * تقوم بإلغاء النقلة
     * @see ListOutputStream#commite() 
     */
    public void rollback();
    
}
