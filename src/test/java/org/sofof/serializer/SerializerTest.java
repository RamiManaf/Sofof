/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

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
            PrimitivesTest p = (PrimitivesTest) s.deserialize(s.serialize(new PrimitivesTest().assignValues()));
            assertEquals(expected, p);
        }
    }

    @Test
    public void testArray() throws Exception {
        SofofSerializer s = new SofofSerializer();
        ArrayTest expected = new ArrayTest().assignValues();
        ArrayTest p = (ArrayTest) s.deserialize(s.serialize(new ArrayTest().assignValues()));
        assertEquals(expected, p);
    }

    @Test
    public void testReference() throws Exception {
        for (Serializer s : serializers) {
            ReferenceTest expected = new ReferenceTest().assignValues();
            ReferenceTest p = (ReferenceTest) s.deserialize(s.serialize(new ReferenceTest().assignValues()));
            assertEquals(expected, p);
        }
    }

}
