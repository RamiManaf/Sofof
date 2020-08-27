/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import org.sofof.SofofException;

/**
 * Offer converting objects to bytes and vice versa
 * @author Rami Manaf Abdullah
 */
public interface Serializer {
    
    public String getName();
    
    public byte[] serialize(Object obj) throws SofofException;
    
    public Object deserialize(byte[] bytes) throws SofofException, ClassNotFoundException;
    
}
