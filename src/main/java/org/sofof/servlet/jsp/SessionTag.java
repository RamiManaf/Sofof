/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.servlet.jsp;

import org.sofof.Session;
import org.sofof.SofofException;
import org.sofof.permission.User;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import org.sofof.SessionManager;

/**
 *
 * @author LENOVO PC
 */
public class SessionTag extends TagSupport {

    private Session session;
    private String url;
    private String username;
    private String password;
    private boolean ssl;
    private String name;

    public Session getSession() {
        return session;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            if (name != null) {
                session = SessionManager.getSession(name);
            } else {
                session = SessionManager.startSession(url, new User(username, password), ssl);
            }
        } catch (SofofException ex) {
            throw new JspException(ex);
        }
        return EVAL_BODY_INCLUDE;
    }
    
    public static Session getSession(Tag tag) throws JspTagException{
        while (tag != null && !(tag instanceof SessionTag)) {
            tag = tag.getParent();
        }
        if(tag == null){
            throw new JspTagException("no session tag parent found");
        }
        return ((SessionTag)tag).getSession();
    }

    @Override
    public int doEndTag() throws JspException {
        if(name == null)try {
            session.close();
        } catch (SofofException ex) {
            throw new JspException(ex);
        }
        return EVAL_PAGE;
    }

}
