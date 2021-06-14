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
public class MaxTest {

    @Before
    public void setUp() throws SofofException {
        Utils.startupServer(new SofofSerializer());
    }

    @Test
    public void testMax() throws SofofException {
        Session session = Utils.startSession();
        for (int i = 0; i <= 50; i++) {
            session.execute(new Bind(i));
        }
        List<String> results = session.query(new Max(Integer.class, 5, "#"));
        session.close();
        Assert.assertEquals(5, results.size());
        Assert.assertEquals(Arrays.asList(46, 47, 48, 49, 50), results);
    }
    
    @Test
    public void testIndexedMax() throws SofofException{
        Session session = Utils.startSession();
        Student rami = new Student(0, "Rami", "Abdullah");
        Student yacine = new Student(1, "Yacine", "Yacine");
        Student nadeem = new Student(2, "Nadeem", "Salman");
        Student hani = new Student(3, "Hani", "Abdullah");
        session.execute(new Bind(rami, yacine, nadeem, hani));
        session.execute(new Index(Student.class, "#getId()"));
        List<Student> results = session.query(new Max(Student.class, 2, "#getId()"));
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(nadeem.getId(), results.get(0));
        Assert.assertEquals(hani.getId(), results.get(1));
        session.close();
    }

    @After
    public void tearDown() throws SofofException {
        Utils.shutdownServer();
    }

}
