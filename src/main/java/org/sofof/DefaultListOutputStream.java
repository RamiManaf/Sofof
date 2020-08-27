/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.BindingNamesTree.BindClass;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.sofof.serializer.Serializer;

/**
 *
 * @author Rami Manaf Abdullah
 * @see DefaultListInputStream
 */
public class DefaultListOutputStream implements ListOutputStream {

    private File db;
    private BindingNamesTree bindTree;
    private Serializer serializer;
    private boolean transaction;
    private ArrayList<File> tempFiles;

    /**
     *
     * @param db database folder
     * @param bindTree binding names tree
     * @param serializer serializer which will be used to serialize objects
     */
    public DefaultListOutputStream(File db, BindingNamesTree bindTree, Serializer serializer) {
        this.db = db;
        this.bindTree = bindTree;
        this.serializer = serializer;
        tempFiles = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List write(List objects, String bind, Class clazz) throws SofofException {
        if (bind == null || bind.trim().isEmpty()) {
            bind = "SofofNoName";
        }
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.getType().equals(ID.class)) {
                    field.setAccessible(true);
                    ArrayList<Integer> usedIDNumbers = new ArrayList<>();
                    ArrayList<Object> toGenerateID = new ArrayList<>();
                    for (Object object : objects) {
                        ID baseID = (ID) field.get(object);
                        if (baseID != null) {
                            if (usedIDNumbers.contains(baseID.getId())) {
                                throw new SofofException("ID " + baseID + " already in use to be asigned to " + object);
                            } else {
                                usedIDNumbers.add(baseID.getId());
                            }
                        } else {
                            toGenerateID.add(object);
                        }
                    }
                    int id = 0;
                    for (Object object : toGenerateID) {
                        try {
                            while (usedIDNumbers.contains(id)) {
                                id++;
                            }
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
        File oldStorage = bc.getStorageFile();
        File newStorage, temp = null;
        if (oldStorage != null) {
            temp = new File(bindFolder, "temp-" + oldStorage.getName());
            newStorage = new File(bindFolder, oldStorage.getName());
            oldStorage.renameTo(temp);
            if (transaction) {
                tempFiles.add(temp);
            }
        } else {
            try {
                newStorage = createHexFile(bindFolder);
            } catch (IOException ex) {
                throw new SofofException("couldn't create a new file", ex);
            }
        }
        try {
            try ( FileOutputStream fos = new FileOutputStream(newStorage, false)) {
                fos.write(serializer.serialize(objects.toArray(new Object[objects.size()])));
            }
            if (temp != null && !transaction) {
                temp.delete();
            }
            bc.setStorageFile(newStorage);
        } catch (IOException ex) {
            if (temp != null && !transaction) {
                newStorage.delete();
                recover(temp);
            }
            throw new SofofException("an IOException thrown when trying to write. the database could be damaged", ex);
        }
        return objects;
    }

    private File createHexFile(File folder) throws IOException {
        long level = 0;
        while (new File(folder, Long.toHexString(level)).exists()) {
            level += 1;
        }
        File f = new File(folder, Long.toHexString(level));
        f.createNewFile();
        return f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTransaction() {
        transaction = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        transaction = false;
        tempFiles.forEach((file) -> file.delete());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() {
        transaction = false;
        tempFiles.forEach((temp) -> {
            File oldFile = new File(temp.getParentFile(), temp.getName().substring(5));
            oldFile.delete();
            recover(temp);
        });
    }

    private void recover(File temp) {
        temp.renameTo(new File(temp.getParentFile(), temp.getName().substring(5)));
    }

}
