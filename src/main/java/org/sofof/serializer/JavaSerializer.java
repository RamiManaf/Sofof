/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.sofof.SofofException;

/**
 * Use the java API to serialize objects.
 *
 * @author Rami Manaf Abdullah
 */
public class JavaSerializer implements Serializer {

    @Override
    public String getName() {
        return "java";
    }

    @Override
    public void serialize(Object obj, OutputStream o) throws SofofException {
        try {
            ObjectOutputStream out = new ObjectOutputStream(o);
            out.writeObject(obj);
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }

    @Override
    public Object deserialize(InputStream in) throws SofofException, ClassNotFoundException {
        try ( ObjectInputStream ois = new ObjectInputStream(in)) {
            return ois.readObject();
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }

    @Override
    public void skip(InputStream in) throws SofofException, ClassNotFoundException {
        deserialize(in);
    }

}
