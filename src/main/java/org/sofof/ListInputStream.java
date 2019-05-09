/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.util.LinkedList;

/**
 *
 * @author Rami Manaf Abdullah
 */
public interface ListInputStream {

    /**
     * /**
     * تقرأ قائمة من كائنات الصف المربوط باسم الربط الممرر 
     * {@code 
     * List list = in.read("students", Student.class);
     * Student s = (Student)list.get(0);
     * }
     *
     * @param bind اسم الربط
     * @param c الصف
     * @return قائمة بكائنات الصف المقروءة وإذا لم يكن هناك كائنات ستعيد قائمة
     * فارغة
     * @throws SofofException
     */
    public LinkedList read(String bind, Class c) throws SofofException;

}
