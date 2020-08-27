/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import org.sofof.SofofException;

/**
 * Apply on n number of objects.
 *
 * @author Rami Manaf Abdullah
 */
public class LimitCondition implements Condition {

    private int limit;
    private int offset;

    /**
     * Apply only to the passed number of objects
     * @param limit number of objects that this condition apply on
     */
    public LimitCondition(int limit) {
        this(limit, 0);
    }

    /**
     * Apply only to the limit number of objects starting after the offset
     * @param limit number of objects that this condition apply on
     * @param offset number of objects that this condition will skip them
     */
    public LimitCondition(int limit, int offset) {
        if (limit < -1) {
            throw new IllegalArgumentException("number less than -1 can not be a limit");
        } else if (offset < 0) {
            throw new IllegalArgumentException("minus number can not be an offset");
        }
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public boolean check(Object obj) throws SofofException {
        if (offset > 0) {
            offset--;
            return false;
        }
        if (limit > 0) {
            limit--;
        }
        return limit >= 0 || limit == -1;
    }

}
