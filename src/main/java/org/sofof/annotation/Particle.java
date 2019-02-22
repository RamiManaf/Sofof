/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.annotation;

import org.sofof.command.Capture;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *<h3>الجزء</h3>
 * <p>يستخدم لتحديد الحقول التي سيتم التقاطها</p>
 * @author Rami Manaf Abdullah
 * @see Capture
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Particle {
    
}
