/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.Session;
import org.sofof.Server;
import org.sofof.Database;
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
public class DatabaseTest {
    
    private static Database db;
    private static Session session;
    private static Server server;
    
    @BeforeClass
    public static void setUpClass() throws SofofException, InterruptedException {
        Thread.sleep(2000);
        Database.createDatabase(new File("test-db"));
        server = new Server().configure();
        User admin = new User("Rami", "secret");
        server.getUsers().add(admin);
        server.startUp();
        db = new Database("localhost", 6969);
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
        assertFalse(Database.createDatabase(new File("test-db")));
    }
    
    /**
     * Test of startSession method, of class Database.
     */
    @Test
    public void testStartSession() throws Exception {
        session = db.startSession(new User("Rami", "secret"));
    }

    /**
     * Test of isNoName method, of class Database.
     */
    @Test
    public void testIsNoName() {
        assertTrue(Database.isNoName("     "));
    }

    /**
     * Test of isNoName method, of class Database.
     */
    @Test
    public void test2IsNoName() {
        assertTrue(Database.isNoName(null));
    }

    /**
     * Test of isNoName method, of class Database.
     */
    @Test
    public void test3IsNoName() {
        assertFalse(Database.isNoName("nd"));
    }
    
}
