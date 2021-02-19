/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import org.sofof.SofofException;

/**
 * This class allow you to define a serializer for one class.This is useful if
 * you want to read/write object in a different way that the serializer use.
 *
 * @author Rami Manaf Abdullah
 * @param <T> the class that this serializer could read/write
 */
public interface ClassSpecificSerializer<T> {

    /**
     * the class that this serializer could read/write and all it's children
     *
     * @return
     */
    public Class<T> getClazz();

    /**
     * the method that is called to serialize an object
     *
     * @param serializer the serializer instance so you can use this to
     * serialize fields of the class
     * @param obj the object that should be serialized
     * @param out the output stream that the result should be written to it
     * @throws SofofException
     */
    public void serialize(Serializer serializer, T obj, OutputStream out) throws SofofException;

    /**
     * the method that is called to deserialize an object
     *
     * @param serializers the serializer instance so you can use it to
     * deserialize fields of the class
     * @param clazz the class of the object that should be deserialized
     * @param in the input stream that should be read from
     * @return
     * @throws SofofException
     */
    public T deserialize(Serializer serializers, Class clazz, InputStream in) throws SofofException;

}
