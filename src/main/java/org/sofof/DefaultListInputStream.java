/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.EOFException;
import org.sofof.BindingNamesTree.BindClass;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sofof.serializer.Serializer;

/**
 *
 * @author Rami Manaf Abdullah
 * @see DefaultListOutputStream
 */
public class DefaultListInputStream implements ListInputStream {

    private File db;
    private BindingNamesTree bindTree;
    private Serializer serializer;

    /**
     *
     * @param db database folder
     * @param bindTree binding names tree
     * @param serializer serializer which will be used to read serialized data
     */
    public DefaultListInputStream(File db, BindingNamesTree bindTree, Serializer serializer) {
        this.db = db;
        this.bindTree = bindTree;
        this.serializer = serializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List read(String bind, Class c) throws SofofException {
        return read(bind, c, null);
    }

    private List read(String bind, Class c, List sharedReferances) throws SofofException {
        if (bind == null || bind.isEmpty()) {
            bind = "SofofNoName";
        }
        BindClass bc = bindTree.getBind(bind).getBindClass(c);
        File file = bc.getStorageFile();
        byte[] serializedData = new byte[0];
        if (file != null) {
            try {
                serializedData = readFile(file);
            } catch (IOException ex) {
                throw new SofofException("unable to read data from server files", ex);
            }
        }
        ArrayList list;
        if (serializedData.length == 0) {
            list = new ArrayList();
        } else {
            try {
                list = new ArrayList(Arrays.asList((Object[]) serializer.deserialize(serializedData)));
            } catch (ClassNotFoundException ex) {
                throw new SofofException("the class read is not found in the classpath");
            }
        }
        for (Object object : list) {
            reloadBranches(object, sharedReferances);
        }
        return list;
    }

    private byte[] readFile(File file) throws FileNotFoundException, IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            if(file.length() > Integer.MAX_VALUE){
                throw new RuntimeException(file.getName() +" is too big to read");
            }
            byte[] data = new byte[(int) file.length()];
            int position = 0;
            while (position < data.length) {
                int bytesRead = in.read(data, position, data.length - position);
                if (bytesRead == -1) {
                    throw new EOFException("had read only " + position + " of " + data.length + " and end of stream is reached");
                }
                position += bytesRead;
            }
            return data;
        }
    }

    private void reloadBranches(Object object, List sharedReferances) throws SofofException {
        if (object == null) {
            return;
        }
        if (sharedReferances == null) {
            sharedReferances = new ArrayList();
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
                    Object reloaded = getObjectByID(getBaseID(Array.get(object, x)));
                    sharedReferances.add(reloaded);
                    Array.set(object, x, reloaded);
                } else {
                    reloadBranches(Array.get(object, x), sharedReferances);
                }
            }
        } else {
            for (Field field : object.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object branch = field.get(object);
                    ID id = getBaseID(branch);
                    if (id != null) {
                        Object reloaded = getObjectByID(id);
                        sharedReferances.add(reloaded);
                        field.set(object, reloaded);
                    } else {
                        reloadBranches(branch, sharedReferances);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new SofofException(ex);
                }
            }
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

    private Object getObjectByID(ID id) throws SofofException {
        List matches = read(id.getBind(), id.getClazz());
        for (Object match : matches) {
            if (getBaseID(match).equals(id)) {
                return match;
            }
        }
        return null;
    }

}
