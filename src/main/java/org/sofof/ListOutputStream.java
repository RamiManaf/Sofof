/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.BindTree.BindClass;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * كاتب القوائم
 *
 * @author Rami Manaf Abdullah
 * @see ListInputStream
 */
public class ListOutputStream {

    private File db;
    private BindTree bindTree;
    /**
     * حجم قطعة واحدة من البيانات بالبايت
     */
    private final static int BLOCK_SIZE = 1024 * 1024 * 64;

    /**
     * إعداد كاتب القوائم
     *
     * @param db مجلد قاعدة البيانات
     * @param bindTree شجرة أسماء الربط
     * @see ListInputStream#ListInputStream(java.io.File,
     * com.sefr.sofof.BindTree, com.sefr.sofof.permission.User)
     */
    public ListOutputStream(File db, BindTree bindTree) {
        this.db = db;
        this.bindTree = bindTree;
    }

    /**
     * تكتب قائمة في قاعدة البيانات وتربطها باسم الربط 
     * {@code 
     * List<Student> list;
     * ...
     * out.write(list, "Students", Student.class);
     * }
     *
     * @param objects القائمة
     * @param bind اسم الربط
     * @param clazz الصف الذي تتكون منه القائمة
     * @return تعيد الصفوف بعد توليد معرف لها
     * @throws org.sofof.SofofException
     * @see ListInputStream#read(java.lang.String, java.lang.Class)
     */
    public List<Object> write(List<Object> objects, String bind, Class clazz) throws SofofException {
        if (Database.isNoName(bind)) {
            bind = "SofofNoName";
        }
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.getType().equals(ID.class)) {
                    field.setAccessible(true);
                    LinkedList<Integer> usedIDNumbers = new LinkedList<>();
                    LinkedList<Object> toGenerateID = new LinkedList<>();
                    for (Object object : objects) {
                        ID baseID = (ID) field.get(object);
                        if (baseID != null) {
                            if (usedIDNumbers.contains(baseID.getId())) {
                                throw new SofofException("ID " + baseID + " already in use to be asigned to " + object);
                            } else {
                                usedIDNumbers.add(baseID.getId());
                            }
                        }else toGenerateID.add(object);
                    }
                    int id = 0;
                    for (Object object : toGenerateID) {
                        try {
                            while(usedIDNumbers.contains(id))id++;
                            field.set(object, new ID(bind, clazz, id));
                            usedIDNumbers.add(id);
                        } catch (IllegalAccessException | IllegalArgumentException ex) {
                            throw new SofofException(ex);
                        }
                    }
                    break;
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException ex) {
            throw new SofofException(ex);
        }
        File bindFolder = new File(db, bind);
        bindFolder.mkdir();
        BindClass bc = bindTree.getBind(bind).getBindClass(clazz);
        bc.tryLockWrite();
        LinkedList<File> storageFiles = bc.getFiles();
        try (
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(byteOutputStream)) {
            oos.writeObject(objects);
            for (int p = 0; p < storageFiles.size(); p++) {
                storageFiles.get(p).delete();
            }
            storageFiles.clear();
            byte[] bytes = byteOutputStream.toByteArray();
            float filesNum = ((float) bytes.length) / (float) BLOCK_SIZE;
            filesNum = filesNum > (int) filesNum ? (int) filesNum + 1 : (int) filesNum;
            int pos = 0;
            for (int x = 0; x < filesNum; x++) {
                File writeFile = createHexFile(bindFolder);
                storageFiles.add(writeFile);
                try (FileOutputStream fos = new FileOutputStream(writeFile, false)) {
                    fos.write(bytes, pos, (bytes.length - pos) > BLOCK_SIZE ? BLOCK_SIZE : bytes.length - pos);
                    pos += (bytes.length - pos) > BLOCK_SIZE ? BLOCK_SIZE : bytes.length - pos;
                    fos.flush();
                }
            }
        } catch (IOException ex) {
            throw new SofofException("an IOException thrown when trying to write. the database could be damaged", ex);
        } finally {
            bc.unlockWrite();
        }
        bc.setFiles(storageFiles);
        return objects;
    }

    /**
     * <p>
     * تنشئ ملف باسم رقم من نظام العد السادس عشري.</p>
     *
     * @param folder المجلد الذي سيتم إنشاء الملف داخله
     * @return الملف المنشأ
     * @throws IOException
     */
    private File createHexFile(File folder) throws IOException {
        long level = 0;
        while (new File(folder, Long.toHexString(level)).exists()) {
            level += 1;
        }
        File f = new File(folder, Long.toHexString(level));
        f.createNewFile();
        return f;
    }

    private static ID getBaseID(Object object) throws SofofException {
        if (object == null || (object.getClass().getPackage() != null && object.getClass().getPackage().getName().startsWith("java."))) {
            return null;
        }
        for (Field field : object.getClass().getFields()) {
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

}
