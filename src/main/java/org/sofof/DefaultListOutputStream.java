/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import org.sofof.BindingNamesTree.BindingClass;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private ArrayList<BindingClass> newData;//for transaction to remove when rollback

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
        newData = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List write(List objects, String bind, Class clazz) throws SofofException {
        if (bind == null || bind.trim().isEmpty()) {
            bind = "SofofNoName";
        }
        try {
            for (Field field : clazz.getDeclaredFields()) {
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
        BindingClass bc = bindTree.getBindingName(bind).getBindingClass(clazz);
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
                fos.write(serializer.getStartCode());
                for (int i = 0; i < objects.size(); i++) {
                    serializer.serialize(objects.get(i), fos);
                    if (i != objects.size() - 1) {
                        fos.write(serializer.getSeparatorCode());
                    }
                }
                fos.write(serializer.getEndCode());
            }
            if (temp != null && !transaction) {
                temp.delete();
            }
            bc.setStorageFile(newStorage);
            if (transaction && oldStorage == null) {
                newData.add(bc);
            }
        } catch (IOException ex) {
            if (temp != null && !transaction) {
                newStorage.delete();
                recover(temp);
            }
            throw new SofofException("an IOException thrown when trying to write. the database could be damaged", ex);
        }
        return objects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List add(List objects, String bind, Class clazz) throws SofofException {
        if (bind == null || bind.trim().isEmpty()) {
            bind = "SofofNoName";
        }
        File bindFolder = new File(db, bind);
        bindFolder.mkdir();
        BindingClass bc = bindTree.getBindingName(bind).getBindingClass(clazz);
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType().equals(ID.class)) {
                    field.setAccessible(true);
                    ArrayList<Integer> usedIDNumbers = new ArrayList<>();
                    ArrayList<Object> toGenerateID = new ArrayList<>();
                    if (bc.getStorageFile() != null) {
                        try ( FileInputStream in = new FileInputStream(bc.getStorageFile())) {
                            Object obj;
                            while ((obj = serializer.deserialize(in)) != null) {
                                usedIDNumbers.add(((ID) field.get(obj)).getId());
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            throw new SofofException(ex);
                        }
                    }
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
        File oldStorage = bc.getStorageFile();
        File newStorage, temp = null;
        if (oldStorage != null) {
            temp = new File(bindFolder, "temp-" + oldStorage.getName());
            newStorage = new File(bindFolder, oldStorage.getName());
            oldStorage.renameTo(temp);
            try ( InputStream in = new BufferedInputStream(new FileInputStream(temp));  OutputStream out = new BufferedOutputStream(new FileOutputStream(newStorage))) {
                long sizeToRead = temp.length() - serializer.getEndCode().length;
                while (sizeToRead-- != 0) {
                    out.write(in.read());
                }
            } catch (IOException ex) {
                if (!transaction) {
                    newStorage.delete();
                    recover(temp);
                }
                throw new SofofException("couldn't copy data from " + temp.getName() + " to " + newStorage.getName() + ". The system will recover old data", ex);
            }
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
            try ( OutputStream out = new BufferedOutputStream(new FileOutputStream(newStorage, true))) {
                if (oldStorage == null) {
                    out.write(serializer.getStartCode());
                }
                for (int i=0;i<objects.size();i++) {
                    if(!(oldStorage==null && i==0))
                    out.write(serializer.getSeparatorCode());
                    serializer.serialize(objects.get(i), out);
                }
                out.write(serializer.getEndCode());
            }
            if (temp != null && !transaction) {
                temp.delete();
            }
            bc.setStorageFile(newStorage);
            if (transaction && oldStorage == null) {
                newData.add(bc);
            }
        } catch (IOException ex) {
            if (temp != null && !transaction) {
                newStorage.delete();
                recover(temp);
            }
            throw new SofofException("an IOException thrown when trying to write. the database could be damaged", ex);
        }
        return objects;
    }

    /**
     *
     * @param bindingName
     * @param clazz
     * @return
     * @throws SofofException
     */
    @Override
    public SequentialWriter createSequentialWriter(String bindingName, Class clazz) throws SofofException {
        if (bindingName == null || bindingName.trim().isEmpty()) {
            bindingName = "SofofNoName";
        }
        return new DefaultSequentialWriter(bindingName, clazz);
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
        bindTree.getBindingNames().forEach((bindingName) -> {
            bindingName.getClasses().forEach((clazz) -> {
                if (clazz.getStorageFile() != null && clazz.getStorageFile().getName().startsWith("temp-")) {
                    clazz.setStorageFile(new File(clazz.getStorageFile().getParentFile(), clazz.getStorageFile().getName().substring(5)));
                }
            });
        });
        tempFiles.forEach((temp) -> {
            File oldFile = new File(temp.getParentFile(), temp.getName().substring(5));
            oldFile.delete();
            recover(temp);
        });
        newData.forEach((bc) -> {
            bc.getStorageFile().delete();
            bc.setStorageFile(null);
        });
    }
    
    private void recover(File temp) {
        temp.renameTo(new File(temp.getParentFile(), temp.getName().substring(5)));
    }
    
    public class DefaultSequentialWriter implements SequentialWriter {
        
        private OutputStream out;
        private String bindingName;
        private Class clazz;
        private Field field;
        private List<Integer> ids = new ArrayList<>();
        private File newStorage, temp;
        private BindingClass bc;
        private boolean needSeparator = false;
        
        private DefaultSequentialWriter(String bindingName, Class clazz) throws SofofException {
            File bindFolder = new File(db, bindingName);
            bindFolder.mkdir();
            bc = bindTree.getBindingName(bindingName).getBindingClass(clazz);
            File oldStorage = bc.getStorageFile();
            if (oldStorage != null) {
                temp = new File(bindFolder, "temp-" + oldStorage.getName());
                newStorage = new File(bindFolder, oldStorage.getName());
                oldStorage.renameTo(temp);
                bc.setStorageFile(temp);
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
                out = new BufferedOutputStream(new FileOutputStream(newStorage, false));
                out.write(serializer.getStartCode());
            } catch (IOException ex) {
                throw new SofofException(ex);
            }
            this.bindingName = bindingName;
            this.clazz = clazz;
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType().equals(ID.class)) {
                    field.setAccessible(true);
                    this.field = field;
                    break;
                }
            }
        }
        
        @Override
        public ID write(Object obj) throws SofofException {
            ID sofofID = null;
            if (field != null) {
                try {
                    sofofID = (ID) field.get(obj);
                    if (sofofID == null) {
                        int id = 0;
                        while (ids.contains(id)) {
                            id++;
                        }
                        field.set(obj, new ID(bindingName, clazz, id));
                        ids.add(id);
                    } else {
                        if (ids.contains(sofofID.getId())) {
                            throw new SofofException("ID " + sofofID + " already in use to be asigned to " + obj);
                        } else {
                            ids.add(sofofID.getId());
                        }
                    }
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    throw new SofofException(ex);
                }
            }
            try {
                if (needSeparator) {
                    out.write(serializer.getSeparatorCode());
                }
                serializer.serialize(obj, out);
                needSeparator = true;
            } catch (SofofException | IOException ex) {
                if (temp != null && !transaction) {
                    newStorage.delete();
                    recover(temp);
                    bc.setStorageFile(newStorage);
                }
                throw new SofofException("an IOException thrown when trying to write. the system will try to recover", ex);
            }
            return sofofID;
        }
        
        @Override
        public void close() throws IOException {
            if (needSeparator) {
                out.write(serializer.getSeparatorCode());
            }
            out.write(serializer.getEndCode());
            out.close();
            if (temp != null && !transaction) {
                temp.delete();
            }
            bc.setStorageFile(newStorage);
            if (temp == null && transaction) {
                newData.add(bc);
            }
        }
        
    }
    
}
