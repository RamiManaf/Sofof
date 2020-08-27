/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.command.sorter;

import java.io.Serializable;

/**
 * Order of sorting ascending or descending.
 * 
 *@author Rami Manaf Abdullah
 */
public enum Order implements Serializable{
    
        Descending,
        Ascending;
        private static final long serialVersionUID = 87234702347l;
    }
