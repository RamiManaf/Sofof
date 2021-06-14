/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sofof.Session;
import org.sofof.SofofException;
import org.sofof.Utils;
import org.sofof.model.Student;
import org.sofof.serializer.SofofSerializer;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class MinTest {

    @Before
    public void setUp() throws SofofException {
        Utils.startupServer(new SofofSerializer());
    }

    @Test
    public void testMin() throws SofofException {
        Session session = Utils.startSession();
        for (int i = 50; i >= 0; i--) {
            session.execute(new Bind(i));
        }
        List<String> results = session.query(new Min(Integer.class, 5, "#"));
        session.close();
        Assert.assertEquals(5, results.size());
        Assert.assertEquals(Arrays.asList(0, 1, 2, 3, 4), results);
    }

    @Test
    public void testIndexedMin() throws SofofException {
        Session session = Utils.startSession();
        Student rami = new Student(0, "Rami", "Abdullah");
        Student yacine = new Student(1, "Yacine", "Yacine");
        Student nadeem = new Student(2, "Nadeem", "Salman");
        Student hani = new Student(3, "Hani", "Abdullah");
        session.execute(new Bind(rami, yacine, nadeem, hani));
        session.execute(new Index(Student.class, "#getId()"));
        List<Student> results = session.query(new Min(Student.class, 2, "#getId()"));
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(rami.getId(), results.get(0));
        Assert.assertEquals(yacine.getId(), results.get(1));
        session.close();
    }

    @After
    public void tearDown() throws SofofException {
        Utils.shutdownServer();
    }

}
