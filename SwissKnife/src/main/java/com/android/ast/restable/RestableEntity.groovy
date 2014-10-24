package com.android.ast.restable

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Restable entity annotation
 *
 * @author Eugene Kamenev @eugenekamenev
 * @since 0.1
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("com.android.ast.restable.RestableTransformation")
@interface RestableEntity {
}