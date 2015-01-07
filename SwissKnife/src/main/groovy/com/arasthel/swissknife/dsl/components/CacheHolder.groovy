package com.arasthel.swissknife.dsl.components

import groovy.transform.CompileStatic

/**
 * Simple cache holder based on Map of any elements
 * @param < K >
 * @param < V >
 *
 * @author Eugene Kamenev eugenekamenev
 */
@CompileStatic
class CacheHolder<K, V> {
    final Map<K, V> elements = new LinkedHashMap<K, V>()

    synchronized V findOrCreate(K key, Closure<V> closure = null) {
        if (elements.containsKey(key)) {
            return elements[key]
        } else {
            V element
            try {
                element = closure?.call()
            } catch (Exception e) {
                elements.put(key, null)
                throw e
            }
            return elements.put(key, element)
        }
    }

    synchronized V findOrCreate(K key, V object) {
        V element = elements[key]
        if (!elements.containsKey(key)) {
            element = elements.put(key, object)
        }
        element
    }

    V remove(K key) {
        elements.remove(key)
    }
}
