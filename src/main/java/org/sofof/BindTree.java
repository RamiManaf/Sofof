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
import java.util.LinkedList;
import java.util.List;

/**
 * شجرة أسماء الربط
 *
 * @author Rami Manaf Abdullah
 */
public class BindTree implements Serializable {

    private static final long serialVersionUID = 725607124l;

    private LinkedList<Bind> binds;

    /**
     * <p>
     * إنشاء اسم ربط.</p>
     */
    public static class Bind implements Serializable {

        private static final long serialVersionUID = 7980792384l;

        private String name;
        private LinkedList<BindClass> classes;

        /**
         *
         * @param name اسم الريط
         */
        public Bind(String name) {
            this.name = name;
            classes = new LinkedList<>();
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
        public synchronized List<BindClass> getClasses() {
            return classes;
        }

        /**
         * تحدد الصفوف المربوطة باسم الربط
         *
         * @param classes الصفوف
         */
        public synchronized void setClasses(LinkedList<BindClass> classes) {
            this.classes = classes;
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
            getClasses().add(bc);
            return bc;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeUTF(name);
            out.writeObject(classes);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            name = in.readUTF();
            classes = (LinkedList<BindClass>) in.readObject();
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
        private transient Boolean readLock = false;
        private transient int readingCount = 0;
        private transient Boolean writeLock = false;

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

        /**
         * <p>
         * تحاول امتلاك قفل القراءة, وإذا كان قفل الكتابة ممتلكا ستنتظره</p>
         */
        public synchronized void tryLockRead() {
            while (writeLock) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
            readingCount+=1;
            readLock = true;
        }

        /**
         * <p>
         * تحاول امتلاك قفل الكتابة, وإذا كان قفل الكتابة أو القراءة ممتلكين
         * ستنتظرهما.</p>
         */
        public synchronized void tryLockWrite() {
            while (writeLock || readLock) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
            writeLock = true;
        }

        /**
         * <p>
         * تحرر قفل القراءة, وإذا كانت كل أقفال القراءة محررة سيسمح
         * بالكتابة.</p>
         */
        public synchronized void unlockRead() {
            readingCount-=1;
            if (readingCount == 0) {
                readLock = false;
                notify();
            }
        }

        /**
         * <p>
         * تحرر قفل الكتابة, مما يسمح بعملية كتابة أو عمليات قراءة
         * بالمتابعة.</p>
         */
        public synchronized void unlockWrite() {
            writeLock = false;
            notifyAll();
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
            readLock = false;
            writeLock = false;
            readingCount = 0;
        }

    }

    /**
     * تنشئ شجرة أسماء ربط
     */
    public BindTree() {
        binds = new LinkedList<>();
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

    public synchronized List<Bind> getBinds() {
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
        out.writeObject(binds);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        binds = (LinkedList<Bind>) in.readObject();
    }

}
