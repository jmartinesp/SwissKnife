package com.android.ast.restable

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/**
 * @see Restable entity validation builder
 * @author Eugene Kamenev @eugenekamenev
 * @since 0.1
 */
@Singleton
@CompileStatic
class RestableValidationBuilder {

    /**
     * Validation options holder
     */
    static final Map<Class, Map<String, Map>> entityConstraints = new HashMap()

    protected Class scanningClass
    /**
     * Method that scan entity class for constraints
     *
     * @param clazz
     */
    static synchronized void create(Class clazz) {
        RestableValidationBuilder.instance.scanningClass = clazz
        if (!entityConstraints.containsKey(clazz)) {
            def closure = (Closure) ClassUtils.getClassFieldValue(clazz, 'constraints')
            def clone = closure?.rehydrate(RestableValidationBuilder.instance, null, null)
            clone?.resolveStrategy = Closure.DELEGATE_ONLY
            clone?.call()
        }
    }

    def methodMissing(String name, def args) {
        saveProperty(scanningClass, name, (Map) ((Object[]) args)[0])
        this
    }
/**
 * Object validation method
 *
 * @param object
 * @return
 */
    boolean validate(object) {
        boolean validated = true
        ((Map) object['errors']).clear()
        def errors = [:]
        entityConstraints[object.class]?.each { k, v ->
            def propertyValue = object[k]
            v.nullable ? this.nullable(propertyValue, (boolean) v.nullable, k, errors) : void
            v.blank ? this.blank(propertyValue, (boolean) v.blank, k, errors) : void
            v.size ? this.size(propertyValue, (IntRange) v.size, k, errors) : void
            v.min ? this.min(propertyValue, v.min, k, errors) : void
            v.max ? this.max(propertyValue, v.max, k, errors) : void
            v.pattern ? this.pattern(propertyValue, v.pattern, k, errors) : void
            v.length ? this.length(propertyValue, (int) v.length, k, errors) : void
            v.range ? this.range(propertyValue, (IntRange) v.range, k, errors) : void
        }
        if (errors.size() > 0) {
            object['errors'] = errors
            validated = false
        }
        validated
    }

    private void range(value, IntRange range, String property, Map<String, Map> errors) {
        ((Number) value) in range ?: putError(errors, property, 'range', "$property should be in range $range.from..$range.to")
    }

    /**
     * Validating by size property
     *
     * @param value
     * @param range
     * @param property
     * @param errors
     */
    private void size(value, IntRange range, String property, Map<String, Map> errors) {
        if (value instanceof String) {
            value.length() in range ?: putError(errors, property, 'size', "Size should be in $range.from..$range.to")
        } else if (value instanceof Number) {
            (value as Number) in range ?: putError(errors, property, 'size', "Size should be in range $range.from..$range.to")
        }
    }

    /**
     * Validating by blank property
     *
     * @param value
     * @param condition
     * @param property
     * @param errors
     */
    private void blank(value, boolean condition, String property, Map<String, Map> errors) {
        ((String) value).trim().length() > 1 != condition ?: putError(errors, property, 'blank', "Value of $property should not be blank")
    }

    /**
     * Validating by nullable property
     *
     * @param value
     * @param condition
     * @param property
     * @param errors
     */
    private void nullable(value, boolean condition, String property, Map<String, Map> errors) {
        (value == null) == condition ?: putError(errors, property, 'nullable', "Value of $property should not be null")
    }

    /**
     * Validating by max property
     *
     * @param value
     * @param max
     * @param property
     * @param errors
     */
    private void max(value, max, String property, Map<String, Map> errors) {
        if (value instanceof String) {
            value.length() <= (Number) max ?: putError(errors, property, 'max', "Value of $property should not be greather than $max")
        } else if (value instanceof Number) {
            ((Number) value) <= (Number) max ?: putError(errors, property, 'max', "Value of $property should not be greather than $max")
        }
    }

    /**
     * Validating by min property
     *
     * @param value
     * @param min
     * @param property
     * @param errors
     */
    private void min(value, min, String property, Map<String, Map> errors) {
        if (value instanceof String) {
            value.length() >= (Number) min ?: putError(errors, property, 'min', "Value of $property should not be less than $min")
        } else if (value instanceof Number) {
            ((Number) value) >= (Number) min ?: putError(errors, property, 'min', "Value of $property should not be less than $min")
        }
    }

    /**
     * Validating by length property
     *
     * @param value
     * @param length
     * @param property
     * @param errors
     */
    private void length(value, int length, String property, Map<String, Map> errors) {
        ((String) value).length() <= length ?: putError(errors, property, 'length', "Length of $property should be less than $length")
    }

    /**
     * Validating by pattern
     *
     * @param value
     * @param pattern
     * @param property
     * @param errors
     */
    private void pattern(value, pattern, String property, Map<String, Map> errors) {
        ((String) value) ==~ ((Pattern) pattern) ?: putError(errors, property, 'pattern', "Value of $property is invalid")
    }

    /**
     * Collect error method
     *
     * @param errors
     * @param property
     * @param errorType
     * @param errorMessage
     */
    private void putError(Map<String, Map> errors, String property, String errorType, String errorMessage) {
        if (!errors[property]) {
            errors[property] = [:]
        }
        errors[property][errorType] = errorMessage
    }

    /**
     * Save property constraint rule
     * @param clazz
     * @param property
     * @param map
     */
    private static void saveProperty(Class clazz, String property, Map map) {
        def properties = entityConstraints[clazz]
        if (!properties) {
            properties = new HashMap()
        }
        Map paramerters = (Map) properties[property]
        if (!paramerters) {
            paramerters = new HashMap()
        }
        properties[property] = paramerters + map
        entityConstraints.put(clazz, properties)
    }
}
