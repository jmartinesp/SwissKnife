package com.android.components

import groovy.transform.CompileStatic

@CompileStatic
class ObjectPropertyResolver {

    String notation = ''

    def propertyMissing(String name) {
        notation += "${name}."
        this
    }

    String getNotation() {
        String last
        if (this.notation.length() > 1) {
            last = this.notation[0..-2]
        } else {
            last = this.notation
        }
        this.notation = ''
        last
    }

    def asType(Class clazz) {
        this
    }
}
