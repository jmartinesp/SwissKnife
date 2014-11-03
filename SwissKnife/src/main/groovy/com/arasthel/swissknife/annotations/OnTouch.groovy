package com.arasthel.swissknife.annotations

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
/**
 * Created by Arasthel on 16/08/14.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)

@GroovyASTTransformationClass(["com.arasthel.swissknife.annotations.OnTouchTransformation"])
public @interface OnTouch {
    int[] value();
}
