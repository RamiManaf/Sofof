/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.util.Arrays;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class ArrayTest {

    int[] i;
    int[][] i2;
    String[] s;

    ArrayTest assignValues() {
        i = new int[]{1, 2, 3};
        i2 = new int[][]{{1, 2, 3}, {4, 5, 6}};
        s = new String[]{"hi", "hello"};
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArrayTest other = (ArrayTest) obj;
        if (!Arrays.equals(this.i, other.i)) {
            return false;
        }
        if (!Arrays.deepEquals(this.i2, other.i2)) {
            return false;
        }
        if (!Arrays.deepEquals(this.s, other.s)) {
            return false;
        }
        return true;
    }
}
