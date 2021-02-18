/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class SerializerTest {

    private ArrayList<Serializer> serializers = new ArrayList<>(Arrays.asList(new SofofSerializer(), new JsonSerializer()));

    @BeforeClass
    public static void setUpClass() {
    }

    @Test
    public void testPrimitives() throws Exception {
        for (Serializer s : serializers) {
            PrimitivesTest expected = new PrimitivesTest().assignValues();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            s.serialize(new PrimitivesTest().assignValues(), out);
            PrimitivesTest p = (PrimitivesTest) s.deserialize(new ByteArrayInputStream(out.toByteArray()));
            assertEquals(expected, p);
        }
    }

    @Test
    public void testArray() throws Exception {
        for (Serializer s : serializers) {
            ArrayTest expected = new ArrayTest().assignValues();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            s.serialize(new ArrayTest().assignValues(), out);
            ArrayTest p = (ArrayTest) s.deserialize(new ByteArrayInputStream(out.toByteArray()));
            assertEquals(expected, p);
        }
    }

    @Test
    public void testReference() throws Exception {
        for (Serializer s : serializers) {
            ReferenceTest expected = new ReferenceTest().assignValues();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            s.serialize(new ReferenceTest().assignValues(), out);
            ReferenceTest p = (ReferenceTest) s.deserialize(new ByteArrayInputStream(out.toByteArray()));
            assertEquals(expected, p);
        }
    }

}
