/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.command.condition.ObjectCondition;
import java.io.Serializable;

/**
 *<h3>عملية</h3>
 * يقوم الصف عملية بإجراء عمليات المقارنة
 * بين طرفين
 * @see ObjectCondition
 * @author Rami Manaf Abdullah
 */
public enum Operation implements Serializable{
    /**
     * يساوي
     */
    Equal,
    /**
     * لا يساوي
     */
    NotEqual,
    /**
     * أكبر
     */
    Greater,
    /**
     * أكبر أو يساوي
     */
    GreaterOrEqual,
    /**
     * أصغر
     */
    Less,
    /**
     * أصغر أو يساوي
     */
    LessOrEqual;
    
    private static final long serialVersionUID = 793298438908l;

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public boolean operate(double x, double y) {
        if (this.equals(Equal)) {
            return x == y;
        }
        if (this.equals(NotEqual)) {
            return x != y;
        }
        if (this.equals(Greater)) {
            return x > y;
        }
        if (this.equals(GreaterOrEqual)) {
            return x >= y;
        }
        if (this.equals(Less)) {
            return x < y;
        }
        if (this.equals(LessOrEqual)) {
            return x <= y;
        }
        return false;
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    public boolean operate(boolean a, boolean b) {
        if (this.equals(Equal)) {
            return a == b;
        } else if (this.equals(NotEqual)) {
            return a != b;
        }
        assert false;
        return false;
    }
    
    /**
     *
     * @param obj
     * @param obj2
     * @return
     */
    public boolean operate(Object obj, Object obj2){
        if(this.equals(Equal))return obj.equals(obj2);
        if(this.equals(NotEqual))return !obj.equals(obj2);
        assert false;
        return false;
    }
}
