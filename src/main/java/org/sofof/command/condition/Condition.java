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
     * @param condition الشرط الممرر
     * @return تعيد شرطا يطبق العملية المنطقية و على هذا الشرط والشرط الممرر
     */
    default public Condition and(Condition condition){
        return new LogicalAndCondition(this, condition);
    }
    
    /**
     * تقوم بالعملية أو المنطقية
     * @param condition الشرط الممرر
     * @return تعيد شرطا يطبق العملية المنطقية أو على هذا الشرط والشرط الممرر
     */
    default public Condition or(Condition condition){
        return new LogicalOrCondition(this, condition);
    }
    
    /**
     * تقوم بالعملية أو الخاصة
     * @param condition الشرط الممرر
     * @return تعيد شرطا يطبق العملية المنطقية أو الخاصة على هذا الشرط والشرط الممرر
     */
    default public Condition xor(Condition condition){
        return new LogicalXOrCondition(this, condition);
    }
    
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
