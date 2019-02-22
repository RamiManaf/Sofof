/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.Serializable;
import java.util.Objects;

/**
 *المعرف هو صف يتم استخدامه للإشارة للكائنات داخل قاعدة البيانات, إذ يتم وضعه كمعامل في أي صف وسيتم توليده عند كتابة الكائن في قاعدة البيانات, ولاسترجاعه يجب قراءة الكائن الموجود في قاعدة البيانات.
 * استخدم المعرف كعلامة مميزة فقط بين الكائنات الأخرى التي تمتلك معرفا تم توليده.
 * @author Rami Manaf Abdullah
 * @see com.sefr.sofof.command.Bind
 */
public class ID implements Serializable{
    
    private static final long serialVersionUID = 46212556;
    
    private String bind;
    private Class clazz;
    private int id;

    ID(String bind, Class clazz, int id) {
        this.bind = bind;
        this.clazz = clazz;
        this.id = id;
    }

    public String getBind() {
        return bind;
    }

    public Class getClazz() {
        return clazz;
    }

    public int getId() {
        return id;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "{"+bind+", "+clazz+", "+id+"}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ID other = (ID) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.bind, other.bind)) {
            return false;
        }
        if (!Objects.equals(this.clazz, other.clazz)) {
            return false;
        }
        return true;
    }
    
}
