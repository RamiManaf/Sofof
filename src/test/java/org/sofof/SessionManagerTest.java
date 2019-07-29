/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.Session;
import org.sofof.Server;
import org.sofof.SofofException;
import org.sofof.permission.User;
import java.io.File;
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
public class SessionManagerTest {
    
    private static Session session;
    private static Server server;
    
    @BeforeClass
    public static void setUpClass() throws SofofException, InterruptedException {
        Thread.sleep(2000);
        server = new Server().configure();
        User admin = new User("Rami", "secret");
        server.getUsers().add(admin);
        server.startUp();
    }
    
    @AfterClass
    public static void tearDownClass() throws SofofException {
        session.close();
        server.shutdown();
    }
    
    @Before
    public void setUp() throws SofofException {
        
    }
    
    @After
    public void tearDown() throws SofofException {
    }
    
    /**
     * Test of createDatabase method, of class Database.
     */
    @Test
    public void testCreateExistDatabase() throws Exception {
        assertFalse(server.createDatabase());
    }
    
    /**
     * Test of startSession method, of class Database.
     */
    @Test
    public void testStartSession() throws Exception {
        session = SessionManager.startSession("java:localhost:6969",new User("Rami", "secret"));
    }
    
}
