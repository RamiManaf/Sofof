/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.BindTree.BindClass;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

/**
 * قارئ القوائم
 *
 * @author Rami Manaf Abdullah
 * @see ListOutputStream
 */
public class ListInputStream {

    private File db;
    private BindTree bindTree;
    private ClassLoader classLoader;

    /**
     * إعداد قارئ القوائم من قاعدة البيانات
     *
     * @param db مجلد قاعدة البيانات
     * @param bindTree شجرة أسماء الربط
     * @see ListOutputStream#ListOutputStream(java.io.File,
     * com.sefr.sofof.BindTree, com.sefr.sofof.permission.User)
     */
    public ListInputStream(File db, BindTree bindTree) {
        this.db = db;
        this.bindTree = bindTree;
    }

    /**
     * إعداد قارئ القوائم من قاعدة البيانات
     *
     * @param db مجلد قاعدة البيانات
     * @param bindTree شجرة أسماء الربط
     * @param loader محمل الصفوف الذي سيستخدم لتحميل الصفوف
     * @see ListOutputStream#ListOutputStream(java.io.File,
     * com.sefr.sofof.BindTree, com.sefr.sofof.permission.User)
     */
    public ListInputStream(File db, BindTree bindTree, ClassLoader loader) {
        this(db, bindTree);
        this.classLoader = loader;
    }

    /**
     * تقرأ قائمة من كائنات الصف المربوط باسم الربط الممرر 
     * {@code 
     * List list = in.read("students", Student.class);
     * Student s = (Student)list.get(0);
     * }
     *
     * @param bind اسم الربط
     * @param c الصف
     * @return قائمة بكائنات الصف المقروءة وإذا لم يكن هناك كائنات ستعيد قائمة
     * فارغة
     * @throws SofofException
     * @see ListOutputStream#write(java.util.List, java.lang.String,
     * java.lang.Class)
     */
    public LinkedList read(String bind, Class c) throws SofofException {
        if (Database.isNoName(bind)) {
            bind = "SofofNoName";
        }
        BindClass bc = bindTree.getBind(bind).getBindClass(c);
        bc.tryLockRead();
        List<File> files = bc.getFiles();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (File file : files) {
                baos.write(Files.readAllBytes(file.toPath()));
            }
        } catch (IOException ex) {
            throw new SofofException("an IOException thrown when trying to read", ex);
        } finally {
            bc.unlockRead();
        }
        LinkedList list;
        if (baos.toByteArray().length == 0) {
            list = new LinkedList();
        } else {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(bais) {
                        @Override
                        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                            if (classLoader != null) {
                                try {
                                    return Class.forName(desc.getName(), false, classLoader);
                                } catch (ClassNotFoundException ex) {
                                    return super.resolveClass(desc);
                                }
                            } else {
                                return super.resolveClass(desc); //To change body of generated methods, choose Tools | Templates.
                            }
                        }
                    };
                    list = (LinkedList) ois.readObject();
                } catch (ClassNotFoundException ex) {
                    throw new SofofException("the class read is not found in the classpath");
                }
            } catch (IOException ex) {
                throw new SofofException(ex);
            }
        }
        for (Object object : list) {
            reloadBrunches(object, null);
        }
        return list;
    }

    private void reloadBrunches(Object object, LinkedList sharedReferances) throws SofofException {
        if (object == null) {
            return;
        }
        if (sharedReferances == null) {
            sharedReferances = new LinkedList();
        }
        if (object.getClass().isPrimitive() || (object.getClass().getPackage() != null
                && object.getClass().getPackage().getName().startsWith("java.lang"))) {
            return;
        }
        for (Object referance : sharedReferances) {
            if (referance == object) {
                return;
            }
        }
        sharedReferances.add(object);
        if (object.getClass().isArray()) {
            for (int x = 0; x < Array.getLength(object); x++) {
                if (getBaseID(Array.get(object, x)) != null) {
                    Object reloaded = reloadObject(Array.get(object, x));
                    sharedReferances.add(reloaded);
                    Array.set(object, x, reloaded);
                } else {
                    reloadBrunches(Array.get(object, x), sharedReferances);
                }
            }
        } else {
            for (Field field : object.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object brunch = field.get(object);
                    if (getBaseID(brunch) != null) {
                        Object reloaded = reloadObject(field.get(object));
                        sharedReferances.add(reloaded);
                        field.set(object, reloaded);
                    } else {
                        reloadBrunches(brunch, sharedReferances);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new SofofException(ex);
                }
            }
        }
    }

    private Object reloadObject(Object object) throws SofofException {
        if (getBaseID(object) == null) {
            return object;
        } else {
            return getMatch(getBaseID(object));
        }
    }

    private static ID getBaseID(Object object) throws SofofException {
        if (object == null || object.getClass().isPrimitive() || object.getClass().isArray() || (object.getClass().getPackage() != null && object.getClass().getPackage().getName().startsWith("java.lang"))) {
            return null;
        }
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.getType().equals(ID.class)) {
                field.setAccessible(true);
                try {
                    return (ID) field.get(object);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new SofofException(ex);
                }
            }
        }
        return null;
    }

    private Object getMatch(ID id) throws SofofException {
        LinkedList matches = read(id.getBind(), id.getClazz());
        for (Object match : matches) {
            if (getBaseID(match).equals(id)) {
                return match;
            }
        }
        return null;
    }

}
