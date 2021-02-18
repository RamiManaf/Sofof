/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import java.io.Serializable;

/**
 * A condition that checks a passed boolean value
 * @author Rami Manaf Abdullah
 * @see ObjectCondition
 */
public class BooleanCondition implements Condition, Serializable{
    
    private static final long serialVersionUID = 876567687764l;
    
    private boolean condition;
    
    private BooleanCondition(){}
    
    /**
     * 
     * @param condition 
     */
    public BooleanCondition(boolean condition) {
        this.condition = condition;
    }
    
    /**
     * changes the boolean value
     * @param condition new value
     */
    public void set(boolean condition){
        this.condition = condition;
    }
    
    /**
     * return the boolean value
     * @return 
     */
    public boolean get(){
        return condition;
    }

    @Override
    public boolean check(Object obj) {
        return condition;
    }
}
