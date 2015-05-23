package com.arasthel.swissknife.annotations.resources

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by MrBIMC on 18/05/15.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@GroovyASTTransformationClass(["com.arasthel.swissknife.annotations.resources.ResTransformation"])
public @interface ColorRes {
    int value() default -1;
}