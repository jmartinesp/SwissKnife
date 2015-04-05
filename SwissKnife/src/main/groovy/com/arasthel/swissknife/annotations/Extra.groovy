package com.arasthel.swissknife.annotations

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by Arasthel on 05/04/15.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@GroovyASTTransformationClass(["com.arasthel.swissknife.annotations.ExtraTransformation"])
public @interface Extra {
    String value() default "";
}
