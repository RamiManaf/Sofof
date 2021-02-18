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
 *
 * @author Rami Manaf Abdullah
 */
public interface ClassSpecificSerializer<T> {

    public Class<T> getClazz();

    public void serialize(Serializer serializer, T obj, OutputStream out) throws SofofException;

    public T deserialize(Serializer serializers, Class clazz, InputStream in) throws SofofException;

}
