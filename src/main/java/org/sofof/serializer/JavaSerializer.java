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
import java.util.Objects;
import org.sofof.SofofException;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class JavaSerializer implements Serializer {

    private final String name;

    public JavaSerializer() {
        this("java");
    }

    public JavaSerializer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JavaSerializer other = (JavaSerializer) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.name);
        return hash;
    }

}
