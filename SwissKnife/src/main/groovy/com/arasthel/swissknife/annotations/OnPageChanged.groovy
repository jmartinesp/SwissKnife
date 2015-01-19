package com.arasthel.swissknife.annotations

import groovy.transform.CompileStatic
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
@GroovyASTTransformationClass(["com.arasthel.swissknife.annotations.OnPageChangedTransformation"])
public @interface OnPageChanged {
    int[] value();

    Method method();

    @CompileStatic
    public static final enum Method {
        PAGE_SCROLLED,
        PAGE_SELECTED,
        PAGE_SCROLL_STATE_CHANGED
    }
}
