/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.sofof.permission.User;
import org.sofof.serializer.JavaSerializer;
import org.sofof.serializer.Serializer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class SessionManager {
    
    private static ArrayList<Serializer> serializers = new ArrayList<Serializer>(){{
        add(new JavaSerializer());
    }};
    private static HashMap<String, Session> sessions = new HashMap<>();
    
    public static void registerSerializer(Serializer serializer){
        serializers.add(Objects.requireNonNull(serializer));
    }
    
    public static boolean removeSerializer(String name){
        for(int i=0;i<serializers.size();i++){
            if(serializers.get(i).getName().equals(name)){
                serializers.remove(i);
                return true;
            }
        }
        return false;
    }

    public static List<Serializer> getSerializers() {
        return Collections.unmodifiableList(serializers);
    }
    
    public static Session startSession(String url, User user, boolean ssl) throws SofofException{
        String[] components = url.split(":");
        if(components.length != 3){
            throw new IllegalArgumentException("the url "+url+" has inappropriate schema. expected serializer:host:port");
        }
        for(Serializer serializer : serializers){
            if(serializer.getName().equals(components[0])){
                return new Session(components[1], Integer.parseInt(components[2]), serializer, user, ssl);
            }
        }
        throw new SofofException("there is no serializer with the name "+components[0]);
    }
    
    public static Session startSession(String url, User user) throws SofofException{
        return startSession(url, user, false);
    }
    
    /**
     * <p>
     * ستقوم بإعداد الجلسات من ملف</p>
     * sofof.xml
     *
     * @throws SofofException في حال حدوث خطأ دخل وخرج
     */
    public static void configure() throws SofofException {
        if (SessionManager.class.getResource("/sofof.xml") != null) {
            try {
                NodeList nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(SessionManager.class.getResourceAsStream("/sofof.xml")).getDocumentElement().getElementsByTagName("sessions");
                if (nodes.getLength() != 0) {
                    Element sessionsEl = (Element) nodes.item(0);
                    for (int i = 0; i < sessionsEl.getElementsByTagName("session").getLength(); i++) {
                        Element session = (Element) sessionsEl.getElementsByTagName("session").item(i);
                        Element user = (Element) session.getElementsByTagName("user").item(0);
                        sessions.put(session.getAttribute("name"), startSession(session.getAttribute("url"), new User(user.getAttribute("name"), user.getAttribute("password")), session.getAttribute("ssl").isEmpty() ? false : Boolean.valueOf(session.getAttribute("ssl"))));
                    }
                }
            } catch (IOException ex) {
                throw new SofofException("unable to read sofof.xml", ex);
            } catch (SAXException ex) {
                throw new SofofException("couldn't parse to sofof.xml", ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * تقوم بجلب الجلسة المقترنة باسمها من ملف sofof.xml
     *
     * @param name اسم الجلسة في ملف sofof.xml
     * @return الجلسة المقترنة بالاسم الممرر أو لا قيمة إذا لم يكن هناك جلسة
     * بذلك الاسم
     */
    public static Session getSession(String name) {
        return sessions.get(name);
    }
    
}
