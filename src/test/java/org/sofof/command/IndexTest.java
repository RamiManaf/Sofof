/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sofof.Session;
import org.sofof.SofofException;
import org.sofof.Utils;
import org.sofof.command.condition.ObjectCondition;
import org.sofof.model.Student;
import org.sofof.serializer.SofofSerializer;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class IndexTest {

    private Session session;

    @Before
    public void setUp() throws SofofException {
        Utils.startupServer(new SofofSerializer());
    }

    @Test
    public void testIndex() throws SofofException, IOException {
        Session session = Utils.startSession(new SofofSerializer());
        ArrayList students = new ArrayList(10000);
        int i = 0;
        for (; i < 5000; i++) {
            students.add(new Student(i, "rami" + i, "abdullah" + i));
        }
        long start = System.currentTimeMillis();
        session.execute(new Bind(students));
        System.out.println("Binding: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        session.execute(new Index(Student.class, "#getId()"));
        System.out.println("Indexing: " + (System.currentTimeMillis() - start));
        students.clear();
        for (; i < 10000; i++) {
            students.add(new Student(i, "rami" + i, "abdullah" + i));
        }
        start = System.currentTimeMillis();
        session.execute(new Bind(students));
        System.out.println("Binding 2: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        session.query(new Index(Student.class, new ArrayList<>(Arrays.asList(9999))));
        System.out.println("Indexed: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        session.query(new Select(Student.class).where(new ObjectCondition("#getId()", Operation.Equal, 9999)));
        System.out.println("Normal: " + (System.currentTimeMillis() - start));
        session.close();
    }

    @Test
    public void testUnbindIndex() throws SofofException {
        Session session = Utils.startSession();
        Student rami = new Student(0, "Rami", "Abdullah");
        Student yacine = new Student(1, "Yacine", "Yacine");
        Student nadeem = new Student(2, "Nadeem", "Salman");
        Student hani = new Student(3, "Hani", "Abdullah");
        session.execute(new Bind(rami, yacine, nadeem, hani));
        session.execute(new Index(Student.class, "#getId()"));
        session.execute(new Unbind(rami, nadeem));
        List list = session.query(new Index(Student.class, new ArrayList<>(Arrays.asList(1, 3))));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(yacine, list.get(0));
        Assert.assertEquals(hani, list.get(1));
        session.close();
    }

    @Test
    public void testUpdateIndex() throws SofofException {
        Session session = Utils.startSession();
        Student rami = new Student(0, "Rami", "Abdullah");
        Student yacine = new Student(1, "Yacine", "Yacine");
        Student nadeem = new Student(2, "Nadeem", "Salman");
        Student hani = new Student(3, "Hani", "Abdullah");
        session.execute(new Bind(rami, yacine, nadeem, hani));
        session.execute(new Index(Student.class, "#getId()"));
        nadeem.setLastName("Mohammed");
        session.execute(new Update(nadeem));
        List list = session.query(new Index(Student.class, new ArrayList<>(Arrays.asList(2))));
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(nadeem, list.get(0));
        session.close();
    }

    @After
    public void tearDown() throws SofofException {
        Utils.shutdownServer();
    }
}
