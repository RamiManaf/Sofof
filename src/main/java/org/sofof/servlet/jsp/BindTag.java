/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet.jsp;

import org.sofof.SofofException;
import org.sofof.command.Bind;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author LENOVO PC
 */
public class BindTag extends TagSupport {

    private Object object;
    private String to;
    private String var;

    public void setObject(Object object) {
        this.object = object;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            if (var != null) {
                pageContext.setAttribute(var, Bind.bindAndReload(SessionTag.getSession(this), new Bind(object).to(to)).get(0));
            } else {
                SessionTag.getSession(this).execute(new Bind(object).to(to));
            }
        } catch (SofofException ex) {
            throw new JspTagException(ex);
        }
        return EVAL_BODY_INCLUDE;
    }

}
