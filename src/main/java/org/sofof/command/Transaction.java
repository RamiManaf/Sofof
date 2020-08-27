/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.SofofException;
import java.util.Arrays;
import java.util.List;
import org.sofof.ListOutputStream;
import org.sofof.ListInputStream;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class Transaction implements Executable {

    private List<Executable> executables;

    /**
     * execute all executables as a transaction
     * @param executables 
     */
    public Transaction(Executable... executables) {
        this.executables = Arrays.asList(executables);
    }

    @Override
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException {
        if (executables != null) {
            try {
                out.startTransaction();
                for (Executable executable : executables) {
                    executable.execute(in, out);
                }
                out.commit();
            } catch (SofofException ex) {
                out.rollback();
                throw new SofofException(ex);
            }
            return executables.size();
        } else {
            return 0;
        }
    }

}
