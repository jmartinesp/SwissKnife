package com.android.ast.restable

import groovy.transform.CompileStatic

/**
 * This trait class adds methods for {@link RestableEntity}
 * While AST-transformation not written
 * we can use trait
 */
@CompileStatic
trait Restable {
    Map errors = new HashMap()

    Object save(Map params) {
        RestableBuilder.instance.onSave(this, params)
        this
    }

    Object delete(Map params) {
        RestableBuilder.instance.onDelete(this, params)
        this
    }

    Object update(Map params) {
        RestableBuilder.instance.onUpdate(this, params)
        this
    }

    boolean validate() {
        RestableValidationBuilder.instance.validate(this)
    }
}