/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.serializer;

import java.util.Objects;

/**
 *
 * @author Rami Manaf Abdullah
 */
public class ReferenceTest {

    PrimitivesTest test;

    ReferenceTest assignValues() {
        test = new PrimitivesTest();
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
        final ReferenceTest other = (ReferenceTest) obj;
        if (!Objects.equals(this.test, other.test)) {
            return false;
        }
        return true;
    }
}
