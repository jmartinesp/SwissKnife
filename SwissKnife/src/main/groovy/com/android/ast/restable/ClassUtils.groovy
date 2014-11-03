package com.android.ast.restable

import groovy.transform.CompileStatic
import org.springframework.util.ReflectionUtils

import java.lang.reflect.Field

@CompileStatic
class ClassUtils {
    static Object getClassFieldValue(Class clazz, String name) {
        Field field = ReflectionUtils.findField(clazz, name)
        if (field != null) {
            ReflectionUtils.makeAccessible(field)
            try {
                return field.get(null)
            } catch (IllegalAccessException ignored) {
            }
        }
        return null
    }
}
