/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet.jsp;

import org.sofof.SofofException;
import org.sofof.command.Update;
import org.sofof.command.condition.Condition;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author LENOVO PC
 */
public class UpdateTag extends TagSupport {

    private Object object;
    private Class clazz;
    private String from;
    private Object set;
    private Condition where;

    public void setObject(Object object) {
        this.object = object;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setSet(Object set) {
        this.set = set;
    }

    public void setWhere(Condition where) {
        this.where = where;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            if (clazz == null) {
                SessionTag.getSession(this).execute(new Update(object).from(from).set(set).where(where));
            } else {
                SessionTag.getSession(this).execute(new Update(clazz).from(from).set(set).where(where));
            }
        } catch (SofofException ex) {
            throw new JspTagException(ex);
        }
        return EVAL_BODY_INCLUDE;
    }

}
