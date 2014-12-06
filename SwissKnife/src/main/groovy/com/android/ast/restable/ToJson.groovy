package com.android.ast.restable

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("com.coupledays.ast.ToJsonTransformation")
@interface ToJson {
    String value() default 'default'

    String[] includes() default [];

    String[] excludes() default [];

    String[] rename() default [];
}