package org.sofof.bean;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author LENOVO PC
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 4702347432L;
    
    private SimpleStringProperty name;
    private SimpleIntegerProperty age;
    private SimpleStringProperty nationality;

    public Student() {
        this.nationality = new SimpleStringProperty(this, "nationality", "");
        this.age = new SimpleIntegerProperty(this, "age", 0);
        this.name = new SimpleStringProperty(this, "name", "");
    }
    
    public Student(String name, int age, String nationality){
        this.nationality = new SimpleStringProperty(this, "nationality", nationality);
        this.age = new SimpleIntegerProperty(this, "age", age);
        this.name = new SimpleStringProperty(this, "name", name);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleIntegerProperty ageProperty() {
        return age;
    }

    public SimpleStringProperty nationalityProperty() {
        return nationality;
    }

    private void readObject(ObjectInputStream in) throws IOException {
        name = new SimpleStringProperty(this, "name", in.readUTF());
        age = new SimpleIntegerProperty(this, "age", in.readInt());
        nationality = new SimpleStringProperty(this, "nationality", in.readUTF());
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(name.get());
        out.writeInt(age.get());
        out.writeUTF(nationality.get());
    }

    @Override
    public String toString() {
        return "Name:"+name.get()+", age"+age.get()+", nation:"+nationality.get();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.age);
        hash = 41 * hash + Objects.hashCode(this.nationality);
        return hash;
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
        final Student other = (Student) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.age, other.age)) {
            return false;
        }
        if (!Objects.equals(this.nationality, other.nationality)) {
            return false;
        }
        return true;
    }

    

}
