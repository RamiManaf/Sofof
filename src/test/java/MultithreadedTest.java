
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
import org.sofof.serializer.SofofSerializer;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class MultithreadedTest {

    private final int COUNT = 5;
    
    @Test
    public void testMultithreadedClients() throws SofofException, InterruptedException {
        deleteDir(new File("test-db"));
        CountDownLatch latch = new CountDownLatch(COUNT);
        User rami = new User("rami", "");
        Server s = new Server(new File("test-db"), 6969, false, Arrays.asList(rami));
        s.setSerializer(new SofofSerializer());
        s.createDatabase();
        s.startUp();
        for (int i = 0; i < COUNT; i++) {
            int x = i;
            new Thread(() -> {
                try {
                    Session session = SessionManager.startSession("sofof:localhost:6969", rami, false);
                    long start = System.currentTimeMillis();
                    session.execute(new Bind(new byte[1024 * 10]));
                    System.out.println(x+ " = " + (System.currentTimeMillis()- start));
                    System.out.println("writed " + x);
                    session.close();
                    latch.countDown();
                } catch (SofofException ex) {
                    Logger.getLogger(MultithreadedTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        }
        latch.await();
        Session session = SessionManager.startSession("sofof:localhost:6969", rami, false);
        Assert.assertEquals(COUNT, session.query(new Select(byte[].class)).size());
        session.close();
        s.shutdown();
    }
    
    @Test
    public void testPool() throws SofofException, InterruptedException {
        deleteDir(new File("test-db"));
        CountDownLatch latch = new CountDownLatch(100);
        User rami = new User("rami", "");
        Server s = new Server(new File("test-db"), 6969, false, Arrays.asList(rami));
        s.setSerializer(new SofofSerializer());
        s.createDatabase();
        s.startUp();
        SessionManager.createPool(10, "sofof:localhost:6969", rami, false);
        for (int i = 0; i < 100; i++) {
            int x = i;
            new Thread(() -> {
                try {
                    Session session = SessionManager.getPooledSession();
                    long start = System.currentTimeMillis();
                    session.execute(new Bind(new byte[10]));
                    System.out.println(x+ " = " + (System.currentTimeMillis()- start));
                    System.out.println("writed " + x);
                    SessionManager.release(session);
                    latch.countDown();
                } catch (SofofException ex) {
                    Logger.getLogger(MultithreadedTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        }
        latch.await();
        Session session = SessionManager.startSession("sofof:localhost:6969", rami, false);
        Assert.assertEquals(100, session.query(new Select(byte[].class)).size());
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
