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
    
    private boolean condition;
    
    /**
     * إنشاء شرط بالقيمة المحددة
     * @param cond القيمة
     */
    public BooleanCondition(boolean cond) {
        this.condition = cond;
    }
    
    /**
     * وضع قيمة جديدة للشرط
     * @param cond القيمة الجديدة
     */
    public void set(boolean cond){
        this.condition = cond;
    }
    
    /**
     * يعيد قيمة هذا الشرط
     * @return قيمة الشرط
     */
    public boolean get(){
        return condition;
    }

    @Override
    public boolean check(Object obj) {
        return condition;
    }
}
