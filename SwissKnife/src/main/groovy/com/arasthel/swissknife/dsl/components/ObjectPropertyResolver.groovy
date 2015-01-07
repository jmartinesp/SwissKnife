package com.arasthel.swissknife.dsl.components

import groovy.transform.CompileStatic

/**
 * Used in notation builder for dynamic code
 * @author Eugene Kamenev eugenekamenev
 */
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
