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
 * شجرة أسماء الربط
 *
 * @author Rami Manaf Abdullah
 */
public class BindTree implements Serializable {

    private static final long serialVersionUID = 725607124l;

    private ArrayList<Bind> binds;

    /**
     * <p>
     * إنشاء اسم ربط.</p>
     */
    public static class Bind implements Serializable {

        private static final long serialVersionUID = 7980792384l;

        private String name;
        private ArrayList<BindClass> classes;

        /**
         *
         * @param name اسم الريط
         */
        public Bind(String name) {
            this.name = name;
            classes = new ArrayList<>();
        }

        /**
         *
         * @return اسم الربط
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @return الصفوف المرتبطة باسم الربط
         */
        public List<BindClass> getClasses() {
            return new ArrayList<>(classes);
        }

        /**
         *
         * @param c الصف الذي سيتم إعادة كائن الصف المرتبط
         * @return الصف المرتبط
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

    /**
     * الصف المرتبط
     */
    public static class BindClass implements Serializable {

        private static final long serialVersionUID = -8986234l;

        private Bind bind;
        private Class clazz;
        private File storageFile;

        /**
         * الصف المرتبط
         *
         * @param bind اسم الربط
         * @param c الصف المرتبط باسم الربط
         */
        public BindClass(Bind bind, Class c) {
            this.bind = bind;
            this.clazz = c;
        }

        /**
         *
         * @return كائن اسم الربط
         */
        public Bind getBind() {
            return bind;
        }

        /**
         *
         * @return الصف المرتبط
         */
        public Class getClazz() {
            return clazz;
        }

        /**
         *
         * @return الملف الذي يتم تخزين كائنات الصف فيه
         */
        public File getStorageFile() {
            return storageFile;
        }

        /**
         * تحدد الملفات التي تخزن الكائنات
         *
         * @param storageFile الملفات
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
            bind = (Bind) in.readObject();
            clazz = (Class) in.readObject();
            storageFile = (File) in.readObject();
        }

    }

    /**
     * تنشئ شجرة أسماء ربط
     */
    public BindTree() {
        binds = new ArrayList<>();
    }

    /**
     * <p>
     * تضيف كائن اسم ربط</p>
     *
     * @param bind كائن اسم الربط
     */
    public synchronized void addBind(Bind bind) {
        binds.add(bind);
    }

    public List<Bind> getBinds() {
        return binds;
    }

    /**
     * تعيد كائن اسم الربط الخاص باسم الربط الممرر
     *
     * @param bindName اسم الربط
     * @return
     */
    public Bind getBind(String bindName) {
        for (Bind bind : binds) {
            if (bind.getName().equals(bindName)) {
                return bind;
            }
        }
        Bind b = new Bind(bindName);
        addBind(b);
        return b;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(binds.toArray(new Bind[binds.size()]));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        binds = new ArrayList<>(Arrays.asList((Bind[])in.readObject()));
    }

}
