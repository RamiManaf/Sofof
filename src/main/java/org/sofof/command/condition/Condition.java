/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import org.sofof.SofofException;
import java.io.Serializable;

/**
 *شرط
 * @author Rami Manaf Abdullah
 */
public interface Condition extends Serializable{
    
    /**
     * تقوم بتنفيذ الشرط على الكائن الممرر
     * @param obj الكائن
     * @return تعيد صحيح إذا انطبق الشرط وخاطئ إذا لم ينطبق
     * @throws SofofException 
     */
    public boolean check(Object obj) throws SofofException;
    
    /**
     * تقوم بالعملية و المنطقية
     * @param cond الشرط الممرر
     * @return تعيد شرطا يطبق العملية المنطقية و على هذا الشرط والشرط الممرر
     */
    default public Condition and(Condition cond){
        Condition th=this;
        return (Object obj) -> th.check(obj)&&cond.check(obj);
    }
    
    /**
     * تقوم بالعملية أو المنطقية
     * @param cond الشرط الممرر
     * @return تعيد شرطا يطبق العملية المنطقية أو على هذا الشرط والشرط الممرر
     */
    default public Condition or(Condition cond){
        Condition th=this;
        return (Object obj) -> th.check(obj)||cond.check(obj);
    }
    
    /**
     * تقوم بالعملية أو الخاصة
     * @param cond الشرط الممرر
     * @return تعيد شرطا يطبق العملية المنطقية أو الخاصة على هذا الشرط والشرط الممرر
     */
    default public Condition xor(Condition cond){
        Condition th=this;
        return (Object obj) -> Boolean.logicalXor(th.check(obj),cond.check(obj));
    }
    
    default public Condition not(){
        return (Object obj) -> !this.check(obj);
    }
}
