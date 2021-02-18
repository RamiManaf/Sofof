/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.permission;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represent user object for login the database and executing commands on it
 * <blockquote><pre>
 * Server s = new Server(new File("db"), 6969, false);
 * User rami = new User("rami", "password");
 * s.getUsers().add(admin);
 * s.startUp();
 * Session sess = SessionManager.startSession("sofof:localhost:6969", new User("rami", "password"), false);
 * </pre></blockquote>
 * @author Rami Manaf Abdullah
 * @see org.sofof.Server
 * @see SofofSecurityManager
 */
public class User implements Serializable{
    
    private static final long serialVersionUID = 7072570230l;
    
    private String name;
    private String password;

    private User(){}
    
    /**
     * Creates a new User with the passed username and password
     * @param name 
     * @param password 
     */
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    /**
     * gets user name
     * @return username
     */
    public String getName() {
        return name;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException{
        out.writeUTF(name);
        out.writeUTF(password);
    }
    
    private void readObject(ObjectInputStream in) throws IOException{
        name = in.readUTF();
        password = in.readUTF();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)return false;
        if(!(obj instanceof User))return false;
        User user = (User) obj;
        if(!user.name.equals(name))return false;
        if(!user.password.equals(password))return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.password);
        return hash;
    }
    
}
