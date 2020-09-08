
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.sofof.Server;
import org.sofof.Session;
import org.sofof.SessionManager;
import org.sofof.SofofException;
import org.sofof.command.Bind;
import org.sofof.command.Select;
import org.sofof.permission.User;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rami Manaf Abdullah
 */
public class MultithreadedTest {

    @Test
    public void testMultithreadedClients() throws SofofException, InterruptedException {
        deleteDir(new File("test-db"));
        CountDownLatch latch = new CountDownLatch(100);
        User rami = new User("rami", "");
        Server s = new Server(new File("test-db"), 6969, false, Arrays.asList(rami));
        s.createDatabase();
        s.startUp();
        for (int i = 0; i < 100; i++) {
            int x = i;
            new Thread(() -> {
                try {
                    Session session = SessionManager.startSession("sofof:localhost:6969", rami, false);
                    session.execute(new Bind("wewo"));
                    System.out.println("writed " + x);
                    session.query(new Select(String.class));
                    System.out.println("readed " + x);
                    session.close();
                    latch.countDown();
                } catch (SofofException ex) {
                    Logger.getLogger(MultithreadedTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        }
        latch.await();
        Session session = SessionManager.startSession("sofof:localhost:6969", rami, false);
        Assert.assertTrue(session.query(new Select(String.class)).size() == 100);
        session.close();
        s.shutdown();
    }

    private void deleteDir(File file) {
        if (!file.exists()) {
            return;
        }
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

}
