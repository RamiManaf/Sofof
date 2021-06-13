package org.sofof;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.sofof.command.Bind;
import org.sofof.command.Select;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class MultithreadedTest {

    private final int COUNT = 5;

    @Test
    public void testMultithreadedClients() throws SofofException, InterruptedException {
        Utils.startupServer();
        CountDownLatch latch = new CountDownLatch(COUNT);
        for (int i = 0; i < COUNT; i++) {
            int x = i;
            new Thread(() -> {
                try {
                    Session session = Utils.startSession();
                    long start = System.currentTimeMillis();
                    session.execute(new Bind(new byte[1024 * 10]));
                    System.out.println(x + " = " + (System.currentTimeMillis() - start));
                    System.out.println("writed " + x);
                    session.close();
                    latch.countDown();
                } catch (SofofException ex) {
                    Logger.getLogger(MultithreadedTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        }
        latch.await();
        Session session = Utils.startSession();
        Assert.assertEquals(COUNT, session.query(new Select(byte[].class)).size());
        session.close();
        Utils.shutdownServer();
    }

    @Test
    public void testPool() throws SofofException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(100);
        Server server = Utils.startupServer();
        SessionManager.createPool(10, server, Utils.USER);
        for (int i = 0; i < 100; i++) {
            int x = i;
            new Thread(() -> {
                try {
                    Session session = SessionManager.getPooledSession();
                    long start = System.currentTimeMillis();
                    session.execute(new Bind(new byte[10]));
                    System.out.println(x + " = " + (System.currentTimeMillis() - start));
                    System.out.println("writed " + x);
                    SessionManager.release(session);
                    latch.countDown();
                } catch (SofofException ex) {
                    Logger.getLogger(MultithreadedTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        }
        latch.await();
        Session session = Utils.startSession();
        Assert.assertEquals(100, session.query(new Select(byte[].class)).size());
        session.close();
        Utils.shutdownServer();
    }

}
