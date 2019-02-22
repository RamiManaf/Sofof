/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import java.io.Serializable;

/**
 *شرط ثابت أو بوليني
 * @author Rami Manaf Abdullah
 * @see ObjectCondition
 */
public class BooleanCondition implements Condition, Serializable{
    
    private static final long serialVersionUID = 876567687764l;
    
    private boolean cond;
    
    /**
     * إنشاء شرط بالقيمة المحددة
     * @param cond القيمة
     */
    public BooleanCondition(boolean cond) {
        this.cond = cond;
    }
    
    /**
     * وضع قيمة جديدة للشرط
     * @param cond القيمة الجديدة
     */
    public void set(boolean cond){
        this.cond = cond;
    }
    
    /**
     * يعيد قيمة هذا الشرط
     * @return قيمة الشرط
     */
    public boolean get(){
        return cond;
    }
    
    /**
     * تنفيذ العملية و على قيمة الشرط وتحتفظ بالقيمة الجديدة
     * @param cond الطرف الثاني للعملية و
     */
    public void and(boolean cond){
        this.cond = this.cond&&cond;
    }
    
    /**
     * تنفيذ العملية أو على قيمة الشرط وتحتفظ بالقيمة الجديدة
     * @param cond الطرف الثاني للعملية أو
     */
    public void or(boolean cond){
        this.cond = this.cond||cond;
    }
    
    /**
     * تنفيذ العملية أو الخاصة على قيمة الشرط وتحتفظ بالقيمة الجديدة
     * @param cond الطرف الثاني للعملية أو الخاصة
     */
    public void xOr(boolean cond){
        this.cond = Boolean.logicalXor(this.cond, cond);
    }

    @Override
    public boolean check(Object obj) {
        return cond;
    }
}
