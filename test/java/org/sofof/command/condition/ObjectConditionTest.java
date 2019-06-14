/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import org.sofof.TestClass;
import org.sofof.command.Operation;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author LENOVO PC
 */
public class ObjectConditionTest {
    
    public ObjectConditionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest1() throws Exception {
        Object obj = null;
        ObjectCondition instance = null;
        instance = new ObjectCondition("#5", Operation.Equal, "#5");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest2() throws Exception {
        Object obj = null;
        ObjectCondition instance = null;
        instance = new ObjectCondition("#7", Operation.Equal, "#5");
        boolean expResult = false;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest3() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#x", Operation.Equal, "#3234");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest4() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#methodReturn7()", Operation.Equal, "#7");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest5() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#methodReturnHello().length()", Operation.Equal, "#5");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest6() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#methodReturnHello().length()", Operation.Equal, "#methodReturn7()");
        boolean expResult = false;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest7() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#methodReturnParam(10i)", Operation.Equal, "#10");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest8() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#textLength(\"hi\"S)", Operation.Equal, "#2");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest9() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#methodReturn7()", Operation.Less, "#methodReturnParam(10i)");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest10() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#multiParams( \"hello\"S, 5i)", Operation.Equal, "#5");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
    /**
     * Test of check method, of class ObjectCondition.
     */
    @Test
    public void checkTest11() throws Exception {
        Object obj = new TestClass();
        ObjectCondition instance = null;
        instance = new ObjectCondition("#methodReturnHello().toCharArray()[0].equals('H'O)", Operation.Equal, "#true");
        boolean expResult = true;
        boolean result = instance.check(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
    
}
