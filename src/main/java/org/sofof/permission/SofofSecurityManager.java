/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.permission;

import org.sofof.command.Executable;
import org.sofof.command.Query;

/**
 * Objects of classes that implements this interface could be passed to the
 * server so they could receive users request for executing commands in the
 * database to decide if they can execute them or not
 *
 * @author Rami Manaf Abdullah
 */
public interface SofofSecurityManager {

    /**
     * This method called every time a user is trying to query objects
     *
     * @param user
     * @param query
     * @throws SecurityException if the query request is denied
     */
    public void checkQuery(User user, Query query) throws SecurityException;

    /**
     * This method called every time a user is trying to query objects
     *
     * @param user
     * @param executable
     * @throws SecurityException if the executing request is denied
     */
    public void checkExecutable(User user, Executable executable) throws SecurityException;

}
