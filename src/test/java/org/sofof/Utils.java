package org.sofof;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import org.sofof.permission.User;
import org.sofof.serializer.Serializer;
import org.sofof.serializer.SofofSerializer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Rami Manaf Abdullah
 */
public class Utils {
    
    public static final User USER = new User("rami", "");
    private static final File dbFile = new File("test-db");
    private static Server server;
    
    public static Server startupServer(Serializer serializer) throws SofofException{
        server = new Server(new File("test-db"), -1, false, Arrays.asList(USER));
        server.setSerializer(serializer);
        server.createDatabase();
        server.startUp();
        return server;
    }
    
    public static Server startupServer() throws SofofException{
        return startupServer(new SofofSerializer());
    }
    
    public static Session startSession(Serializer serializer) throws SofofException{
        return server.createLocalSession(USER);
    }
    
    public static Session startSession() throws SofofException{
        return startSession(new SofofSerializer());
    }
    
    public static void shutdownServer() throws SofofException{
        server.shutdown();
        Utils.deleteDir(dbFile);
    }
    
    public static void deleteDir(File file) {
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
