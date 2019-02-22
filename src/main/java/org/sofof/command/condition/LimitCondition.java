/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import org.sofof.SofofException;

/**
 * يقوم هذا الشرط بتحديد عدد محدد من الكائنات للاستعلام عنها, ولا يجوز استخدام
 * نفس الكائن أكثر من مرة.
 *
 * @author Rami Manaf Abdullah
 */
public class LimitCondition implements Condition {

    private int limit;

    /**
     * يقوم هذا الشرط بتحديد عدد محدد من الكائنات للاستعلام عنها, ولا يجوز
     * استخدام نفس الكائن أكثر من مرة.
     * @param limit عدد الكائنات
     */
    public LimitCondition(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("minus number can not be a limit");
        }
        this.limit = limit;
    }

    @Override
    public boolean check(Object obj) throws SofofException {
        limit--;
        return limit >= 0;
    }

}
