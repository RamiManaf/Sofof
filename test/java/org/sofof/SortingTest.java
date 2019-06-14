/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof;

import org.sofof.command.sorter.ObjectSorter;
import org.sofof.command.sorter.Order;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class SortingTest {
    
    public SortingTest() {
    }
    
     @Test
     public void sortNumbers() throws SofofException {
         List l = Arrays.asList(3, 4, 15, 5);
         new ObjectSorter("#", Order.Descending).sort(l);
         Assert.assertEquals(Arrays.asList(15, 5, 4, 3), l);
     }
}
