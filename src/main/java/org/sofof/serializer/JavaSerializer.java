/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.sofof.SofofException;

/**
 * Use the java API to serialize objects.
 * @author Rami Manaf Abdullah
 */
public class JavaSerializer implements Serializer {

    @Override
    public String getName() {
        return "java";
    }

    @Override
    public byte[] serialize(Object obj) throws SofofException {
        ByteArrayOutputStream writingBytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(writingBytes)) {
            out.writeObject(obj);
            return writingBytes.toByteArray();
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }
    @Override
    public Object deserialize(byte[] bytes) throws SofofException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return in.readObject();
        } catch (IOException ex) {
            throw new SofofException(ex);
        }
    }

}
