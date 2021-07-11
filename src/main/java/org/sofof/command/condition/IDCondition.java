/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.condition;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sofof.ID;
import org.sofof.SofofException;

/**
 * select objects based on {@link ID} id.
 * @author Rami Manaf Abdullah
 */
public class IDCondition implements Condition{

    private List<Integer> IDs;
    private Field cachedField;

    private IDCondition() {}
    
    /**
     * create an ID condition which will select items using there integer id
     * @param IDs 
     */
    public IDCondition(Integer... IDs) {
        this.IDs = new ArrayList(Arrays.asList(IDs));
    }
    
    @Override
    public boolean check(Object obj) throws SofofException {
        if(cachedField == null){
            Class clazz = obj.getClass();
            for(Field field : clazz.getDeclaredFields()){
                if(field.getType().equals(ID.class)){
                    cachedField = field;
                    cachedField.setAccessible(true);
                    break;
                }
            }
            if(cachedField==null){
                throw new SofofException("no ID field found in class "+clazz.getCanonicalName());
            }
        }
        try {
            return IDs.contains(((ID)cachedField.get(obj)).getId());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            //strange cases would throw these exceptions
            throw new SofofException(ex);
        }
    }
    
}
