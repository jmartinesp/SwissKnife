package com.android.ast.restable

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/**
 * @see Restable entity validation builder
 * @author Eugene Kamenev @eugenekamenev
 * @since 0.1
 */
@CompileStatic
class RestableValidation {
    public static void range(value, IntRange range, String property, Map<String, Map> errors) {
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
    public static void size(value, IntRange range, String property, Map<String, Map> errors) {
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
    public static void blank(value, boolean condition, String property, Map<String, Map> errors) {
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
    public static void nullable(value, boolean condition, String property, Map<String, Map> errors) {
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
    public static void max(value, max, String property, Map<String, Map> errors) {
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
    public static void min(value, min, String property, Map<String, Map> errors) {
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
    public static void length(value, int length, String property, Map<String, Map> errors) {
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
    public static void pattern(value, pattern, String property, Map<String, Map> errors) {
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
    public static void putError(Map<String, Map> errors, String property, String errorType, String errorMessage) {
        if (!errors[property]) {
            errors[property] = [:]
        }
        errors[property][errorType] = errorMessage
    }
}
