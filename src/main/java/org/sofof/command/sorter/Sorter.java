/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.sorter;

import org.sofof.SofofException;
import org.sofof.command.Select;
import java.io.Serializable;
import java.util.List;

/**
 *مرتب
 * يقوم بترتيب عناصر قائمة في استعلام معين
 * @see  Select
 * @author Rami Manaf Abdullah
 */
public interface Sorter extends Serializable{
    
    static final long serialVersionUID = 766545;
    
    /**
     * تقوم بترتيب عناصر القائمة الممررة
     * @param list القائمة
     * @throws SofofException 
     */
    public void sort(List list) throws SofofException;
    
    /**
     * تقوم بترتيب الكائنات بالمرتب الممرر ثم ترتيبها بالمرتب الحالي, وهكذا تكون الأولوية للحالي.
     * @param sorter المرتب الممرر
     * @return المرتب المشترك
     */
    default public Sorter after(Sorter sorter){
        Sorter th = this;
        return (list) -> {
            sorter.sort(list);
            th.sort(list);
        };
    }
    
}
