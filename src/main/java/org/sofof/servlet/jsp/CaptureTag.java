/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet.jsp;

import org.sofof.SofofException;
import org.sofof.command.Capture;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author LENOVO PC
 */
public class CaptureTag extends TagSupport {

    private Object object;

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            Capture.capture(SessionTag.getSession(this), object);
        } catch (SofofException ex) {
            throw new JspTagException(ex);
        }
        return EVAL_BODY_INCLUDE;
    }

}
