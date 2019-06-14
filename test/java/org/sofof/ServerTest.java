/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.command.Unbind;
import org.sofof.permission.User;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author LENOVO PC
 */
public class ServerTest {
    
    private static Database db;
    private static Session session;
    private static Server server;
    
    public ServerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws SofofException {
        Database.createDatabase(new File("test-db"));
        server = new Server().configure().startUp();
        db = new Database("localhost", 6969);
    }
    
    @AfterClass
    public static void tearDownClass() throws SofofException {
        server.shutdown();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() throws SofofException {
        if(session!=null)session.close();
    }
    
    @Test
    public void testLoadXML1() throws SofofException{
        session = db.startSession(new User("Rami", "password"));
        session.execute(new Unbind(TestClass.class));
    }
    
    @Test(expected = SofofException.class)
    public void testLoadXML4() throws SofofException{
        session = db.startSession(new User("user3", "password"));
        session.execute(new Unbind(TestClass.class));
    }

}
