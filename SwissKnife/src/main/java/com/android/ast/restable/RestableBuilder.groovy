package com.android.ast.restable

import groovy.transform.CompileStatic

/**
 * {@link RestableEntity} rest methods call builder
 */
@Singleton
@CompileStatic
class RestableBuilder {

    /**
     * Rest methods parameters holder
     */
    private static Map<Class, Map> entityProperties = new HashMap()

    /**
     * Method that scan entity class for building rest calls
     * @param clazz
     */
    static void create(Class clazz) {
        if (!entityProperties.containsKey(clazz)) {
            def closure = (Closure) ClassUtils.getClassFieldValue(clazz, 'api')
            def clone = closure?.rehydrate(RestableBuilder.instance, null, null)
            clone?.resolveStrategy = Closure.DELEGATE_ONLY
            try {
                clone?.call()
            } catch (MissingMethodException e) {
                saveProperty(clazz, e.method, (Map) e.arguments[0])
            }
        }
    }

    void onSave(object, Map params = null) {
    }

    void onDelete(object, Map params = null) {
    }

    void onUpdate(object, Map params = null) {
    }

    /**
     * Save property constraint rule
     * @param clazz
     * @param property
     * @param map
     */
    private static void saveProperty(Class clazz, String property, Map map) {
        def properties = entityProperties.get(clazz)
        if (!properties) {
            properties = new HashMap()
        }
        Map paramerters = (Map) properties[property]
        if (!paramerters) {
            paramerters = new HashMap()
        }
        properties[property] = paramerters + map
        entityProperties.put(clazz, properties)
    }
}
