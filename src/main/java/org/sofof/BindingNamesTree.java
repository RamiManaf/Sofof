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

    private ArrayList<BindingName> bindingNames;

    /**
     * binding name
     */
    public static class BindingName implements Serializable {

        private static final long serialVersionUID = 7980792384l;

        private String name;
        private ArrayList<BindingClass> classes;

        private BindingName(){}
        
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
        public List<BindingClass> getClasses() {
            return new ArrayList<>(classes);
        }

        /**
         * return BindClass object for the passed class. If there is no BindClass with this class the method will create new one
         * @param c class which you want to get his BindingClass object
         * @return BindingClass
         */
        public BindingClass getBindingClass(Class c) {
            for (BindingClass bindClass : classes) {
                if (bindClass.getClazz().equals(c)) {
                    return bindClass;
                }
            }
            BindingClass bc = new BindingClass(c);
            classes.add(bc);
            return bc;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeUTF(name);
            out.writeObject(classes.toArray(new BindingClass[classes.size()]));
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            name = in.readUTF();
            classes = new ArrayList(Arrays.asList((BindingClass[])in.readObject()));
        }

    }

    public static class BindingClass implements Serializable {

        private static final long serialVersionUID = -8986234l;

        private Class clazz;
        private File storageFile;

        private BindingClass(){}
        
        /**
         *
         * @param c class bound to the binding name
         */
        public BindingClass(Class c) {
            this.clazz = c;
        }

        /**
         * 
         * @return class object associated to this BindingClass
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
            out.writeObject(clazz);
            out.writeObject(storageFile);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            clazz = (Class) in.readObject();
            storageFile = (File) in.readObject();
        }

    }

    public BindingNamesTree() {
        bindingNames = new ArrayList<>();
    }

    /**
     * add binding name to the tree
     * @param bind binding name to be added
     */
    public synchronized void addBind(BindingName bind) {
        bindingNames.add(bind);
    }

    public List<BindingName> getBindingNames() {
        return bindingNames;
    }

    /**
     * return BindingName with the passed name and if there is no one it will create one
     *
     * @param bindName
     * @return
     */
    public BindingName getBindingName(String bindName) {
        for (BindingName bind : bindingNames) {
            if (bind.getName().equals(bindName)) {
                return bind;
            }
        }
        BindingName b = new BindingName(bindName);
        addBind(b);
        return b;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(bindingNames.toArray(new BindingName[bindingNames.size()]));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        bindingNames = new ArrayList<>(Arrays.asList((BindingName[])in.readObject()));
    }

}
