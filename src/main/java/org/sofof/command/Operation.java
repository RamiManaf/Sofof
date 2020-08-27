/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.command.condition.ObjectCondition;
import java.io.Serializable;
import org.sofof.SofofException;

/**
 * Preform relational operations
 *
 * @see ObjectCondition
 * @author Rami Manaf Abdullah
 */
public enum Operation implements Serializable {

    Equal,
    NotEqual,
    Greater,
    GreaterOrEqual,
    Less,
    LessOrEqual;

    private static final long serialVersionUID = 793298438908l;

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public boolean operate(double x, double y) {
        switch (this) {
            case Equal:
                return x == y;
            case NotEqual:
                return x != y;
            case Greater:
                return x > y;
            case GreaterOrEqual:
                return x >= y;
            case Less:
                return x < y;
            case LessOrEqual:
                return x <= y;
            default:
                return false;
        }
    }

    /**
     *
     * @param obj
     * @param obj2
     * @return
     * @throws org.sofof.SofofException
     */
    public boolean operate(Object obj, Object obj2) throws SofofException {
        if (obj instanceof Number && obj2 instanceof Number) {
            return operate(((Number) obj).doubleValue(), ((Number) obj2).doubleValue());
        }
        switch (this) {
            case Equal:
                return obj.equals(obj2);
            case NotEqual:
                return !obj.equals(obj2);
            default:
                throw new SofofException("the operation " + name() + " can not be operated on " + obj.getClass().getName());
        }
    }
}
