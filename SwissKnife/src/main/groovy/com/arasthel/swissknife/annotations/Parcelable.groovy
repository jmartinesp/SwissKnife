package com.arasthel.swissknife.annotations;

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Transforming entity to Parcelable
 *
 * @author Jorge Martín Espinosa
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("com.arasthel.swissknife.annotations.ParcelableTransformation")
@interface Parcelable {
    /**
     * Entity properties that should be excluded from Parcelable
     * @return
     */
    Class exclude() default Closure
}
