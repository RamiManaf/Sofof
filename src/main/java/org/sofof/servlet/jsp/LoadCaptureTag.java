/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet.jsp;

import org.sofof.SofofException;
import org.sofof.command.Capture;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author LENOVO PC
 */
public class LoadCaptureTag extends TagSupport {

    private Class clazz;

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            Capture.load(SessionTag.getSession(this), clazz);
        } catch (SofofException ex) {
            throw new JspException(ex);
        }
        return EVAL_BODY_INCLUDE;
    }

}
