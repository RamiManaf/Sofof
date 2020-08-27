/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import java.io.Serializable;
import java.util.List;
import org.sofof.ListInputStream;

/**
 * Query for objects from the database
 * @author Rami Manaf Abdullah
 */
public interface Query extends Command, Serializable{
    
    /**
     * execute the query
     * @param in List reader from the database
     * @return list of queried objects
     * @throws SofofException 
     */
    public List query(ListInputStream in) throws SofofException;
    
}
