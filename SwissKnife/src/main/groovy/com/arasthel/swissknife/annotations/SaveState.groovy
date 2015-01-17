package com.arasthel.swissknife.annotations

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Transforming entity to Parcelable
 *
 * @author Eugene Kamenev
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("com.arasthel.swissknife.annotations.SaveStateForTransformation")
@interface SaveState {
    /**
     * Entity properties that should be excluded from Parcelable
     * @return
     */
    Class value() default Closure
}