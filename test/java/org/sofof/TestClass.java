/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import java.io.Serializable;

/**
 *
 * @author LENOVO PC
 */
public class TestClass implements Serializable{
    public int x = 3234;
    
    public int methodReturn7(){
        return 7;
    }
    
    public String methodReturnHello(){
        return "Hello";
    }
    
    public int methodReturnParam(int y){
        return y;
    }
    
    public int textLength(String text){
        return text.length();
    }
    
    public int multiParams(String first, int sec){
        return sec;
    }
}
