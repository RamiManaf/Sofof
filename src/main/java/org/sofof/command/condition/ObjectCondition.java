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
 * Preform operations on the objects to decide to select them or not using the {@link org.sofof.command.ExpressionExecuter }.
 * The expression get executed and return boolean value or numerical value that you can preform relational operator using 
 * {@link Operation} on it or objects that you can check equality with them.
 * 
 * e.g:
 * <blockquote><pre>
 * ObjectCondition cond = new ObjectCondition("#getMark()", Operation.Greater, "#50");
 * ObjectCondition cond = new ObjectCondition("#getName()", Operation.Equal, "Rami");
 * </pre></blockquote>
 *
 * @author Rami Manaf Abdullah
 */
public class ObjectCondition implements Condition, Serializable {

    private static final long serialVersionUID = 8906943849l;

    private Object side1;
    private Object side2;
    private Operation operation;

    /**
     * You can pass objects or expressions to be executed on database objects and finally preform the relational operation on them
     *
     * @param side1 first object or expression
     * @param operation relational operation
     * @param side2 second object or expression
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
