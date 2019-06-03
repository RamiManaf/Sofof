/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import org.sofof.SofofException;
import org.sofof.command.ExpressionExecuter;
import org.sofof.command.Operation;
import java.io.Serializable;

/**
 * شرط كائني
 * <p>
 * يمكنك هذا الشرط من القيام ببعض العمليات على الكائن للتأكد من أنه الكائن
 * المطلوب
 * </p>
 * <p>
 * {@link org.sofof.command.ExpressionExecuter النص التنفيذي}
 *
 * يتم تطبيق النص التنفيذي وتحويله إما إلى كائن أو إلى قيمة عددية أو منطقية
 * وتتوفر لك مجموعة من عمليات المقارنة بحيث تستطيع مقارنة نواتج النصين
 * التنفيذيين عبر التعداد {@link Operation}
 * </p>
 * يمكنك تنفيذ الدوال واستدعاء الحقول على نواتج دوال أو حقول أخرى وهذه بعض
 * الأمثلة
 * <blockquote><pre>
 * ObjectCondition cond = new ObjectCondition("#getMark()", Operation.Greater, "#50");
 * ObjectCondition cond = new ObjectCondition("#getName()", Operation.Equal, "Rami");
 * </pre></blockquote>
 *
 * @see BooleanCondition
 * @author Rami Manaf Abdullah
 */
public class ObjectCondition implements Condition, Serializable {

    private static final long serialVersionUID = 8906943849l;

    private Object side1;
    private Object side2;
    private Operation operation;

    /**
     * إنشاء شرط كائني بتمرير نصين تنفيذيين على الكائن وعملية للمقارنة بين
     * نتيجتي النصين الممررين يمكن أن يكون الناتج عن النصين كائنا معينا وعندها
     * يمكن فقط تنفيذ العمليتين يساوي أو لا يساوي
     * {@link ExpressionExecuter النص التنفيذي}
     *
     * @param side1 النص التنفيذي الأول
     * @param operation العملية
     * @param side2 النص التنفيذي الثاني
     */
    public ObjectCondition(Object side1, Operation operation, Object side2) {
        this.side1 = side1;
        this.side2 = side2;
        this.operation = operation;
    }

    @Override
    public boolean check(Object obj) throws SofofException {
        Object obj2 = obj;
        obj = execute(side1, obj);
        obj2 = execute(side2, obj2);
        return operation.operate(obj, obj2);
    }

    private Object execute(Object side, Object obj) throws SofofException {
        if (!(side instanceof String)) {
            return side;
        }
        String expression = (String) side;
        if (!expression.startsWith("#")) {
            return expression;
        } else if (expression.startsWith("##")) {
            return expression.substring(1);
        } else {
            return ExpressionExecuter.execute(expression, obj);
        }
    }
}
