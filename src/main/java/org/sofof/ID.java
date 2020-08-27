/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * This class is used to write cross binding names and classes references. You can add an ID field to a class and when an object of that class written to the database, the database will generate an ID and store it in that field. You can get the ID object by reading the stored object.
 * You can use ID to decide equality but only when the two objects have an ID object.
 * @author Rami Manaf Abdullah
 * @see org.sofof.command.Bind
 */
public class ID implements Serializable{
    
    private static final long serialVersionUID = 46212556;
    
    private String bind;
    private Class clazz;
    private int id;

    public ID(String bind, Class clazz, int id) {
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
    
    private void writeObject(ObjectOutputStream out) throws IOException{
        out.writeUTF(bind);
        out.writeObject(clazz);
        out.writeInt(id);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        bind = in.readUTF();
        clazz = (Class) in.readObject();
        id = in.readInt();
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
