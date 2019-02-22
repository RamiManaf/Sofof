/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.ListInputStream;
import org.sofof.SofofException;
import java.io.Serializable;
import java.util.List;

/**
 * استعلام يقوم بالاستعلام عن كائنات محددة
 * @author Rami Manaf Abdullah
 */
public interface Query extends Command, Serializable{
    
    /**
     * تنفيذ الاستعلام
     * @param in قارئ القوائم
     * @return تعيد خريطة مفاتيحها أسماء الصفوف التي
     * تم الاستعلام عنها وقيمها قوائم تلك الصفوف
     * إذا لم يكن هناك كائن مسجل ستعيد الخريطة قائمة فارغة
     * @throws SofofException 
     */
    public List query(ListInputStream in) throws SofofException;
    
}
