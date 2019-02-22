/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command;

import org.sofof.command.Executable;
import org.sofof.ListInputStream;
import org.sofof.ListOutputStream;
import org.sofof.SofofException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class AtomicProcess implements Executable{
    
    private List<Executable> executables;

    public AtomicProcess(){
    }
    
    public AtomicProcess(Executable... executables){
        this.executables = Arrays.asList(executables);
    }
    
    @Override
    public int execute(ListInputStream in, ListOutputStream out) throws SofofException {
        if(executables != null){
            for(Executable executable : executables){
                executable.execute(in, out);
            }
            return executables.size();
        }else return 0;
    }
    
}
