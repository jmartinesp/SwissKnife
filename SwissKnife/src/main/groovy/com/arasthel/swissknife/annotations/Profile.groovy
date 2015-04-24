package com.arasthel.swissknife.annotations

import android.util.Log
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * This transformation is used for profiling method invocation,
 * you can log method execution time, method parameters and method result.
 * If param value is not null it will execute toString() on it
 * For additional info @see {@link ProfileTransformation}
 *
 * @author Eugene Kamenev @eugenekamenev
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@GroovyASTTransformationClass(['com.arasthel.swissknife.annotations.ProfileTransformation'])
@interface Profile {
    /**
     * Android log tag
     * @see {@link Log#i(java.lang.String, java.lang.String)}
     */
    String tag() default 'PROFILE'
    /*
    * Profile method execution time
    */
    boolean time() default true
    /**
     * Log method parameters
     */
    boolean values() default true
    /**
     * Exclude logging method parameters
     */
    String[] excludes() default []
    /**
     * Include logging method parameters
     */
    String[] includes() default []
    /**
     * Log level @see {@link Log#DEBUG}
     */
    int level() default 3
}