/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.id;

import org.sofof.ID;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class Student implements Serializable{
    
    private static final long serialVersionUID = 6825678;
    
    private ID id;
    private String name;
    private int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
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
        if (id != null && other.id != null && !id.equals(other.id)) {
            return false;
        }
        return Objects.equals(this.name, other.name);
    }
    
    
    
}
