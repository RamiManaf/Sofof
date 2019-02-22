/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.bean.Game;
import org.sofof.bean.Student;
import org.sofof.command.Bind;
import org.sofof.command.Select;
import org.sofof.command.Unbind;
import org.sofof.permission.User;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author LENOVO PC
 */
public class SessionTest {

    static Database db;
    static Server server;
    static Session session;

    public SessionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws SofofException {
        Database.createDatabase(new File("test-db"));
        server = new Server(new File("test-db"), 6969, false);
        User admin = new User("Rami", "secret");
        server.getUsers().add(admin);
        server.startUp();
        db = new Database("localhost", 6969);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        server.shutdown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() throws SofofException {
        if (session != null) {
            session.close();
        }
    }

    /**
     * Test of startSession method, of class Database.
     */
    @Test
    public void testRegisteredUserStartSession() throws Exception {
        session = db.startSession(new User("Rami", "secret"));
    }

    /**
     * Test of startSession method, of class Database.
     */
    @Test(expected = SofofException.class)
    public void testUnRegisteredUserStartSession() throws Exception {
        session = db.startSession(new User("malik", "moaze"));
    }

    @Test
    public void testExecuting() throws SofofException, IOException {
        session = db.startSession(new User("Rami", "secret"));
        Student stu = new Student("Rami", 16, "syrian");
        session.execute(new Unbind(Student.class));
        session.execute(new Bind(stu));
        session.query(new Select(Student.class)).get(0).equals(stu);
    }

    @Test
    public void testCapture() throws SofofException {
        session = db.startSession(new User("Rami", "secret"));
        Game game = new Game(System.getProperty("user.name"));
        game.killEnemy();
        game.killEnemy();
        game.killEnemy();
        game.saveGame(session);
        Game loadedGame = Game.loadGame(session);
        Assert.assertEquals(3, loadedGame.getScore());
        Assert.assertEquals(System.getProperty("user.name"), loadedGame.getPlayerName());
    }

}
