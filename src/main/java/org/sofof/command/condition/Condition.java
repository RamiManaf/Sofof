/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import org.sofof.SofofException;
import java.io.Serializable;

/**
 * Represent condition that can be used by commands to decide which objects to apply on
 * @author Rami Manaf Abdullah
 */
public interface Condition extends Serializable{
    
    /**
     * check if this condition apply on the passed object
     * @param obj object to apply condition on
     * @return true if the condition apply to the object and false otherwise
     * @throws SofofException 
     */
    public boolean check(Object obj) throws SofofException;
    
    /**
     * Create a new condition that preform logical and operator on this and the passed condition
     * @param condition 
     * @return the new condition
     */
    default public Condition and(Condition condition){
        return new LogicalAndCondition(this, condition);
    }
    
    /**
     * Create a new condition that preform logical or operator on this and the passed condition
     * @param condition 
     * @return the new condition
     */
    default public Condition or(Condition condition){
        return new LogicalOrCondition(this, condition);
    }
    
    /**
     * Create a new condition that preform logical xor operator on this and the passed condition
     * @param condition 
     * @return the new condition
     */
    default public Condition xor(Condition condition){
        return new LogicalXOrCondition(this, condition);
    }
    
    /**
     * Create a new condition that preform logical not operator on this condition
     * @return the new condition
     */
    default public Condition not(){
        return new LogicalNotCondition(this);
    }
    
    class LogicalOrCondition implements Condition{
        
        private Condition first;
        private Condition second;

        public LogicalOrCondition(Condition first, Condition second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean check(Object obj) throws SofofException {
            return first.check(obj) || second.check(obj);
        }
        
    }
    
    class LogicalXOrCondition implements Condition{
        
        private Condition first;
        private Condition second;

        public LogicalXOrCondition(Condition first, Condition second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean check(Object obj) throws SofofException {
            return first.check(obj) ^ second.check(obj);
        }
        
    }
    
    class LogicalNotCondition implements Condition{
        
        private Condition condition;

        public LogicalNotCondition(Condition condition) {
            this.condition = condition;
        }

        @Override
        public boolean check(Object obj) throws SofofException {
            return !condition.check(obj);
        }
        
    }
    
    class LogicalAndCondition implements Condition{
        
        private Condition first;
        private Condition second;

        public LogicalAndCondition(Condition first, Condition second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean check(Object obj) throws SofofException {
            return first.check(obj) && second.check(obj);
        }
        
    }
}
