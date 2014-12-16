package com.android.ast.restable

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate

import java.nio.charset.Charset

@CompileStatic
class RestServiceCaller {

    static Object createRestCall(Map methodParams, Map urlParams, Closure transformClosure) {
        if (methodParams.method == HttpMethod.GET) {
            if (methodParams.type == List) {
                return getList((String) methodParams.url, urlParams, transformClosure)
            } else {
                return getSingle((String) methodParams.url, urlParams, transformClosure)
            }
        }
        return null
    }

    static Object getSingle(String url, Map parameters, Closure transformClosure) {
        def template = createRestTemplate()
        def jsonSlurper = new JsonSlurper()
        return transformClosure.call((Map) jsonSlurper.parseText((String) template.getForEntity(url, String, (Map<String, ?>) parameters).getBody()))
    }

    static List getList(String url, Map parameters, Closure transformClosure) {
        def template = createRestTemplate()
        def jsonSlurper = new JsonSlurper()
        if (!parameters) {
            parameters = new HashMap()
        }
        def parsed = (List<Map>) jsonSlurper.parseText((String) template.getForEntity(url, String, (Map<String, ?>) parameters).getBody())
        return parsed.collect(transformClosure)
    }

    static RestTemplate createRestTemplate() {
        def template = new RestTemplate()
        def stringConverter = new StringHttpMessageConverter(Charset.forName('UTF-8'))
        stringConverter.supportedMediaTypes = [MediaType.APPLICATION_JSON]
        template.messageConverters.add(stringConverter)
        template
    }
}
