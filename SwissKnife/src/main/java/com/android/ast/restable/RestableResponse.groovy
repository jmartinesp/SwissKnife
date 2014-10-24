package com.android.ast.restable
import groovy.transform.CompileStatic

@CompileStatic
class RestableResponse {
    Closure onSuccess
    Closure onFailure
    Closure modifyRequest

    Map restableProperties

    RestableResponse(Map restableProperties) {
        this.restableProperties = restableProperties
    }

    void sendRequest() {
        try {

        } catch (Exception e) {

        }
    }
}
