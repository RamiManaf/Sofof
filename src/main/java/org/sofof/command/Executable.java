/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import java.io.Serializable;
import org.sofof.ListOutputStream;
import org.sofof.ListInputStream;

/**
 *command that can edit data in the database
 *@author Rami Manaf Abdullah
 */
public interface Executable extends Command, Serializable{
    
    /**
     * execute on the server and has the ability to edit stored data in the database using passed objects
     * @param in lists reader from the database
     * @param out lists writer to the database
     * @return usually affected objects by the command
     * @throws SofofException 
     */
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException;
    
}
