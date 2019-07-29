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
    private int offset;

    public LimitCondition(int limit) {
        this(limit, 0);
    }

    /**
     * يقوم هذا الشرط بتحديد عدد محدد من الكائنات للاستعلام عنها
     *
     * @param limit عدد الكائنات
     * @param offset عدد الكائنات التي سيبدأ بعدها هذا الشرط بقبول العدد المحدد
     * من الكائنات
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
