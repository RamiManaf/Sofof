/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet.jsp;

import org.sofof.SofofException;
import org.sofof.command.Unbind;
import org.sofof.command.condition.Condition;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author LENOVO PC
 */
public class UnBindTag extends TagSupport {

    private Class clazz;
    private Object object;
    private String from;
    private Condition where;

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setWhere(Condition where) {
        this.where = where;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            if (clazz == null) {
                SessionTag.getSession(this).execute(new Unbind(object).from(from).where(where));
            } else {
                SessionTag.getSession(this).execute(new Unbind(clazz).from(from).where(where));
            }
        } catch (SofofException ex) {
            throw new JspTagException(ex);
        }
        return EVAL_BODY_INCLUDE;
    }

}
