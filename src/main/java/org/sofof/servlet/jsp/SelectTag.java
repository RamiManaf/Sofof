/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet.jsp;

import org.sofof.SofofException;
import org.sofof.command.Select;
import org.sofof.command.condition.Condition;
import org.sofof.command.sorter.Sorter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class SelectTag extends TagSupport {

    private Class clazz;
    private String expression;
    private String from;
    private Condition where;
    private Sorter sort;
    private boolean shuffle;
    private String var;

    public SelectTag() {
        shuffle = false;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setWhere(Condition where) {
        this.where = where;
    }

    public void setSort(Sorter sort) {
        this.sort = sort;
    }

    public void setShuffle(Boolean shuffle) {
        this.shuffle = shuffle;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            pageContext.setAttribute(var, SessionTag.getSession(this).query(new Select(clazz, expression, shuffle).from(from).where(where).sort(sort)), PageContext.PAGE_SCOPE);
        } catch (SofofException ex) {
            throw new JspTagException(ex);
        }
        return EVAL_BODY_INCLUDE;
    }

}
