/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.sofof.bean.Student;
import org.sofof.command.Bind;
import org.sofof.command.Select;
import org.sofof.command.Unbind;
import org.sofof.permission.User;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class MultiThreadedTest {

    private static Server server;
    private static CountDownLatch latch;
    private static final int BIND_COUNT = 100;

    @BeforeClass
    public static void setUpClass() throws SofofException {
        latch = new CountDownLatch(BIND_COUNT);
        server = new Server().configure().startUp();
    }

    @AfterClass
    public static void tearDownClass() throws SofofException {
        server.shutdown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test() throws InterruptedException, SofofException, FileNotFoundException, IOException, ClassNotFoundException {
        try (Session sess = SessionManager.startSession("java:localhost:6969", new User("Rami", "password"))) {
            sess.execute(new Unbind(Student.class).from("students"));
            for (int x = 0; x < BIND_COUNT; x++) {
                new Client().start();
            }
            latch.await();
            List list = sess.query(new Select(Student.class).from("students"));
            Assert.assertEquals(BIND_COUNT, list.size());
        }
    }

    private class Client extends Thread {

        @Override
        public void run() {
            try {
                try (Session session = SessionManager.startSession("java:localhost:6969", new User("Rami", "password"))) {
                    Student student = new Student("rami", 16, "sy");
                    session.execute(new Bind(student).to("students"));
                    session.query(new Select(Student.class).from("students"));
                }
            } catch (SofofException ex) {
                Logger.getLogger(MultiThreadedTest.class.getName()).log(Level.SEVERE, "", ex);
            }
            latch.countDown();
        }

    }

}
