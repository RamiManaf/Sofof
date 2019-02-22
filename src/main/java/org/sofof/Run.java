/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.permission.User;
import java.io.File;
import java.util.Scanner;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author Rami Manaf Abdullah
 */
class Run {

    private String[] args;
    private Scanner scanner;

    public Run(String[] args) {
        this.args = args;
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) throws Exception {
        new Run(args).run();
    }

    private void run() throws Exception {
        while (true) {
            if (args.length == 0 || args[0].equals("-h")) {
                help();
            } else {
                //commands
                switch (args[0]) {
                    case "startSession": {
                        if(args.length >1 && args[1].equals("-h")){
                            System.out.println("pattern Host Port Username Password");
                            break;
                        }
                        String host = requestParam(1, "Host:");
                        int port = Integer.parseInt(requestParam(2, "Port:"));
                        String username = requestParam(3, "Username:");
                        String password = requestParam(4, "Password:");
                        try (Session session = new Database(host, port).startSession(new User(username, password))) {
                            ScriptEngine engine = getEngine();
                            engine.put("session", session);
                            String token;
                            System.out.println(username + ">");
                            while (!(token = scanner.nextLine()).equals("exit")) {
                                System.out.println(username + ">");
                                engine.eval(token);
                            }
                        }
                        break;
                    }

                    case "startServer": {
                        if(args.length >1 && args[1].equals("-h")){
                            System.out.println("pattern Database_Folder Port SSL(true,false)");
                            break;
                        }
                        File folder = new File(requestParam(1, "Database Folder:"));
                        int port = Integer.parseInt(requestParam(2, "Port:"));
                        boolean ssl = Boolean.valueOf(requestParam(3, "SSL: (true, false)"));
                        Server server = new Server(folder, port, ssl);
                        ScriptEngine engine = getEngine();
                        engine.put("server", server);
                        String token;
                        System.out.println(folder.getName() + ":" + port + ">");
                        while (!(token = scanner.nextLine()).equals("exit")) {
                            System.out.println(folder.getName() + ":" + port + ">");
                            engine.eval(token);
                        }
                        break;
                    }
                    case "create": {
                        if(args.length >1 && args[1].equals("-h")){
                            System.out.println("pattern Location Database_Name");
                            break;
                        }
                        String folder = requestParam(1, "Location:");
                        String name = requestParam(2, "Database Name:");
                        new File(folder).mkdirs();
                        Database.createDatabase(new File(folder, name));
                        System.out.println("Database created successfully");
                        break;
                    }
                    case "exit": {
                        System.exit(0);
                    }
                    default: {
                        System.out.println("no command with name " + args[0]);
                        break;
                    }
                }
            }
            System.out.println("Sofof>");
            args = escape(scanner.nextLine());
        }
    }

    private String requestParam(int index, String message) {
        if (args.length > index) {
            return args[index];
        } else {
            System.out.println(message);
            return escape(scanner.nextLine())[0];
        }
    }

    public String[] escape(String text) {
        String[] tempArr = text.split(" ");
        int len = 0;
        for (int x = 0; x < tempArr.length; x++) {
            if (tempArr[x].startsWith("\"") && !tempArr[x].endsWith("\"")) {
                tempArr[x + 1] = tempArr[x] + " " + tempArr[x + 1];
                tempArr[x] = null;
            } else {
                len++;
            }
        }
        String[] arr = new String[len];
        len = 0;
        for (int x = 0; x < tempArr.length; x++) {
            if (tempArr[x] != null) {
                arr[len] = tempArr[x].startsWith("\"") && tempArr[x].endsWith("\"") ? tempArr[x].substring(1, tempArr[x].length() - 1) : tempArr[x];
                len++;
            }
        }
        return arr;
    }

    private ScriptEngine getEngine() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        engine.eval("load('nashorn:mozilla_compat.js');");
        engine.eval("importPackage('com.sefr.sofof');");
        engine.eval("importPackage('com.sefr.sofof.annotation');");
        engine.eval("importPackage('com.sefr.sofof.command');");
        engine.eval("importPackage('com.sefr.sofof.command.condition');");
        engine.eval("importPackage('com.sefr.sofof.command.sorter');");
        engine.eval("importPackage('com.sefr.sofof.command.permission');");
        engine.eval("importPackage('com.sefr.sofof.command.servlet');");
        engine.eval("importPackage('com.sefr.sofof.command.servlet.jsp');");
        return engine;
    }

    private void help() {
        System.out.println("  ////   /////   /////////   /////   /////////");
        System.out.println("/       /     /  /          /     /  /");
        System.out.println(" ///// /       / //////    /       / //////");
        System.out.println("     /  /     /  /          /     /  /");
        System.out.println("////     /////   /           /////   /");
        System.out.print("create        create a new database\n"
                + "startServer   starts server\n"
                + "startSession  starts a session\n"
                + "exit          close this program\n"
                + "-h        show this content or command pattern\n"
                + "if one parameter have one or more white space surround it with double quotes\n");
    }
}
