/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.command.Bind;
import org.sofof.command.Operation;
import org.sofof.command.Select;
import org.sofof.command.Unbind;
import org.sofof.command.Update;
import org.sofof.command.condition.ObjectCondition;
import org.sofof.permission.User;
import java.io.File;
import java.util.List;
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
public class CommandsTest {

    private static Session session;
    private static Database db;
    private static Server s;

    public CommandsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws SofofException {
        Database.createDatabase(new File("test-db"));
        User admin = new User("Rami", "password");
        s = new Server().configure();
        s.getUsers().add(admin);
        s.startUp();
        db = new Database("localhost", 6969);
        session = db.startSession(admin);
    }

    @AfterClass
    public static void tearDownClass() throws SofofException {
        session.close();
        s.shutdown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void bind() throws SofofException {
        TestClass obj = new TestClass();
        session.execute(new Bind(obj).to("employee"));
        List list = session.query(new Select(TestClass.class).from("employee"));
        Assert.assertEquals(7, ((TestClass) list.get(0)).methodReturn7());
    }

    @Test
    public void unbind() throws SofofException {
        TestClass obj = new TestClass();
        obj.x = 3;
        session.execute(new Unbind(TestClass.class).from("employee"));
        session.execute(new Bind(obj).to("employee"));
        int x = session.execute(new Unbind(TestClass.class).from("employee"));
        Assert.assertEquals(1, x);
    }

    @Test
    public void update() throws SofofException {
        TestClass update = new TestClass();
        update.x = 25;
        TestClass obj = new TestClass();
        obj.x = 1000;
        session.execute(new Unbind(obj).from("worker"));
        session.execute(new Bind(obj).to("worker"));
        int x = session.execute(new Update(TestClass.class).from("worker").set(update).where(new ObjectCondition("#x", Operation.GreaterOrEqual, "#1000")));
        Assert.assertEquals(1, x);
    }

    @Test
    public void select() throws SofofException {
        session.execute(new Unbind(TestClass.class, TestClass2.class));
        session.execute(new Bind(new TestClass()));
        session.execute(new Bind(new TestClass2()));
        List testClass = session.query(new Select(TestClass.class));
        List testClass2 = session.query(new Select(TestClass2.class));
        Assert.assertEquals(1, testClass.size());
        Assert.assertEquals(1, testClass2.size());
    }

}
