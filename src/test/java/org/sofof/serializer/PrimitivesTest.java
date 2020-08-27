/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class PrimitivesTest {

    int a;
    char b;
    short c;
    byte d;
    long e;
    float f;
    double g;
    boolean h;

    PrimitivesTest assignValues() {
        a = 1;
        b = 'b';
        c = 3;
        d = 4;
        e = 5000000000L;
        f = 6.4f;
        g = 7.1;
        h = true;
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
        final PrimitivesTest other = (PrimitivesTest) obj;
        if (this.a != other.a) {
            return false;
        }
        if (this.b != other.b) {
            return false;
        }
        if (this.c != other.c) {
            return false;
        }
        if (this.d != other.d) {
            return false;
        }
        if (this.e != other.e) {
            return false;
        }
        if (Float.floatToIntBits(this.f) != Float.floatToIntBits(other.f)) {
            return false;
        }
        if (Double.doubleToLongBits(this.g) != Double.doubleToLongBits(other.g)) {
            return false;
        }
        if (this.h != other.h) {
            return false;
        }
        return true;
    }
}
