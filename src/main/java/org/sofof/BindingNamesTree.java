/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data structure that link data files to classes to binding names
 *
 * @author Rami Manaf Abdullah
 */
public class BindingNamesTree implements Serializable {

    private static final long serialVersionUID = 725607124l;

    private ArrayList<BindingName> binds;

    /**
     * binding name
     */
    public static class BindingName implements Serializable {

        private static final long serialVersionUID = 7980792384l;

        private String name;
        private ArrayList<BindClass> classes;

        /**
         *
         * @param name binding name
         */
        public BindingName(String name) {
            this.name = name;
            classes = new ArrayList<>();
        }

        /**
         *
         * @return binding name
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @return classes bound to this binding name
         */
        public List<BindClass> getClasses() {
            return new ArrayList<>(classes);
        }

        /**
         * return BindClass object for the passed class. If there is no BindClass with this class the method will create new one
         * @param c class which you want to get his BindClass object
         * @return BindClass
         */
        public BindClass getBindClass(Class c) {
            for (BindClass bindClass : classes) {
                if (bindClass.getClazz().equals(c)) {
                    return bindClass;
                }
            }
            BindClass bc = new BindClass(this, c);
            classes.add(bc);
            return bc;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeUTF(name);
            out.writeObject(classes.toArray(new BindClass[classes.size()]));
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            name = in.readUTF();
            classes = new ArrayList(Arrays.asList((BindClass[])in.readObject()));
        }

    }

    public static class BindClass implements Serializable {

        private static final long serialVersionUID = -8986234l;

        private BindingName bind;
        private Class clazz;
        private File storageFile;

        /**
         *
         * @param bind binding name which this object will be bound to
         * @param c class bound to the binding name
         */
        public BindClass(BindingName bind, Class c) {
            this.bind = bind;
            this.clazz = c;
        }

        /**
         *
         * @return binding name which this class is linked to
         */
        public BindingName getBind() {
            return bind;
        }

        /**
         * 
         * @return class object associated to this BindClass
         */
        public Class getClazz() {
            return clazz;
        }

        /**
         *
         * @return storage file that objects are stored in it
         */
        public File getStorageFile() {
            return storageFile;
        }

        /**
         * set the storage file that objects of this class are saved in it
         * @param storageFile 
         */
        public void setStorageFile(File storageFile) {
            this.storageFile = storageFile;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(bind);
            out.writeObject(clazz);
            out.writeObject(storageFile);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            bind = (BindingName) in.readObject();
            clazz = (Class) in.readObject();
            storageFile = (File) in.readObject();
        }

    }

    public BindingNamesTree() {
        binds = new ArrayList<>();
    }

    /**
     * add binding name to the tree
     * @param bind binding name to be added
     */
    public synchronized void addBind(BindingName bind) {
        binds.add(bind);
    }

    public List<BindingName> getBinds() {
        return binds;
    }

    /**
     * return BindingName with the passed name and if there is no one it will create one
     *
     * @param bindName
     * @return
     */
    public BindingName getBind(String bindName) {
        for (BindingName bind : binds) {
            if (bind.getName().equals(bindName)) {
                return bind;
            }
        }
        BindingName b = new BindingName(bindName);
        addBind(b);
        return b;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(binds.toArray(new BindingName[binds.size()]));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        binds = new ArrayList<>(Arrays.asList((BindingName[])in.readObject()));
    }

}
