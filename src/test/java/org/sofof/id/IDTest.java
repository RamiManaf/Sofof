/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.id;

import org.sofof.Database;
import org.sofof.Server;
import org.sofof.Session;
import org.sofof.SofofException;
import org.sofof.command.Bind;
import org.sofof.command.Select;
import org.sofof.command.Update;
import java.io.File;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class IDTest {
    
    private static Server server;
    private static Session session;
    
    public IDTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws SofofException {
        Database.createDatabase(new File("test-db"));
        server = new Server().configure().startUp();
        Database.configure();
        session = Database.getSession("test");
    }
    
    @AfterClass
    public static void tearDownClass() throws SofofException {
        session.close();
        server.shutdown();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void relationsTest() throws SofofException {
        Student rami = new Student("Rami", 17);
        Bind bindStudent = new Bind(rami);
        session.execute(bindStudent);
        //load ID Generated copy
        rami = (Student) session.query(bindStudent).get(0);
        Teacher samer = new Teacher("Samer", 40);
        Bind bindTeacher = new Bind(samer);
        session.execute(bindTeacher);
        ////load ID Generated copy
        samer = (Teacher) session.query(bindTeacher).get(0);
        Course math = new Course("Math", samer, Arrays.asList(rami));
        session.execute(new Bind(math));
        //edit student age
        rami.setAge(18);
        session.execute(new Update(rami).set(rami));
        //now student new age will be applied on course object
        Student student = (Student) session.query(new Select(Course.class, "#getStudents().get(0i)")).get(0);
        assertEquals(18, student.getAge());
    }
    
}
