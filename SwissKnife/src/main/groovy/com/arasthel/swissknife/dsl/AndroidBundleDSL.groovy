package com.arasthel.swissknife.dsl

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import groovy.transform.CompileStatic

/**
 * DSL methods for Android Bundle
 * @author Jorge Martín Espinosa
 * @author Eugene Kamenev eugenekamenev
 */
@CompileStatic
public class AndroidBundleDSL {

    /**
     * Create Bundle from Map
     * @param argsMap
     * @return
     */
    static Bundle asBundle(Map<String, ?> argsMap) {
        AndroidStaticBundleDSL.fromMap(null, argsMap)
    }


    /**
     * Fill bundle from map
     * @param bundle
     * @param argsMap
     * @return
     */
    static Bundle putFromMap(Bundle bundle, Map<String, ?> argsMap) {
        for (Map.Entry entry in argsMap.entrySet()) {
            String key = entry.key
            Object value = entry.value
            if (value.class.isArray()) {
                def type = value.class.componentType
                if (type.isPrimitive()) {
                    fromPrimitive(bundle, key, value, true, type)
                }
                else {
                    fromObject(bundle, key, value, true, type)
                }
            }
            else if (value.class.isAssignableFrom(ArrayList)) {
                fromList(bundle, key, value)
            }
            else if (value.class.isAssignableFrom(SparseArray)) {
                fromList(bundle, key, value)
            }
            else {
                // For some reason, primitives when passed as a single variable are treated as
                // objects
                fromObject(bundle, key, value, false, value.class)
            }
        }
        return bundle
    }

    /**
     * Casting and setting value to Bundle
     * @param bundle
     * @param key
     * @param value
     * @param asArray
     * @param valueClass
     * @return
     */
    private
    static Bundle fromObject(Bundle bundle, String key, Object value, boolean asArray,
                             Class valueClass) {
        if (valueClass.isAssignableFrom(Parcelable)) {
            if (asArray) {
                bundle.putParcelableArray(key, (Parcelable[]) value)
            }
            else {
                bundle.putParcelable(key, (Parcelable) value)
            }
            return bundle
        }

        if (valueClass.isAssignableFrom(Serializable)) {
            bundle.putSerializable(key, (Serializable) value)
            return bundle
        }

        if (valueClass.isAssignableFrom(String)) {
            if (asArray) {
                bundle.putStringArray(key, (String[]) value)
            }
            else {
                bundle.putString(key, (String) value)
            }
            return bundle
        }

        if (valueClass.isAssignableFrom(CharSequence)) {
            if (asArray) {
                bundle.putCharSequenceArray(key, (CharSequence[]) value)
            }
            else {
                bundle.putCharSequence(key, (CharSequence) value)
            }
            return bundle
        }

        if (valueClass.isAssignableFrom(Bundle)) {
            bundle.putBundle(key, (Bundle) value)
            return bundle
        }

        if (valueClass.isAssignableFrom(Integer)) {
            if (asArray) {
                bundle.putIntArray(key, (int[]) value)
            }
            else {
                bundle.putInt(key, (int) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(Byte)) {
            if (asArray) {
                bundle.putByteArray(key, (byte[]) value)
            }
            else {
                bundle.putByte(key, (byte) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(Character)) {
            if (asArray) {
                bundle.putCharArray(key, (char[]) value)
            }
            else {
                bundle.putChar(key, (char) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(Long)) {
            if (asArray) {
                bundle.putLongArray(key, (long[]) value)
            }
            else {
                bundle.putLong(key, (long) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(Boolean)) {
            if (asArray) {
                bundle.putBooleanArray(key, (boolean[]) value)
            }
            else {
                bundle.putBoolean(key, (boolean) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(Short)) {
            if (asArray) {
                bundle.putShortArray(key, (short[]) value)
            }
            else {
                bundle.putShort(key, (short) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(Double)) {
            if (asArray) {
                bundle.putDoubleArray(key, (double[]) value)
            }
            else {
                bundle.putDouble(key, (double) value)
            }
            return bundle
        }

        return bundle
    }

    /**
     * Casting primitive value and setting it to Bundle
     * @param bundle
     * @param key
     * @param value
     * @param asArray
     * @param valueClass
     * @return
     */
    private
    static Bundle fromPrimitive(Bundle bundle, String key, Object value, boolean asArray,
                                Class valueClass) {

        if (valueClass.isAssignableFrom(int)) {
            if (asArray) {
                bundle.putIntArray(key, (int[]) value)
            }
            else {
                bundle.putInt(key, (int) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(byte)) {
            if (asArray) {
                bundle.putByteArray(key, (byte[]) value)
            }
            else {
                bundle.putByte(key, (byte) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(char)) {
            if (asArray) {
                bundle.putCharArray(key, (char[]) value)
            }
            else {
                bundle.putChar(key, (char) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(long)) {
            if (asArray) {
                bundle.putLongArray(key, (long[]) value)
            }
            else {
                bundle.putLong(key, (long) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(boolean)) {
            if (asArray) {
                bundle.putBooleanArray(key, (boolean[]) value)
            }
            else {
                bundle.putBoolean(key, (boolean) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(short)) {
            if (asArray) {
                bundle.putShortArray(key, (short[]) value)
            }
            else {
                bundle.putShort(key, (short) value)
            }
            return bundle
        }
        if (valueClass.isAssignableFrom(double)) {
            if (asArray) {
                bundle.putDoubleArray(key, (double[]) value)
            }
            else {
                bundle.putDouble(key, (double) value)
            }
            return bundle
        }

        return bundle
    }

    /**
     * Casting android collection objects and setting value to Bundle
     * @param bundle
     * @param key
     * @param value
     * @return
     */
    private static Bundle fromList(Bundle bundle, String key, Object value) {
        // Ugly hack to find generic type with no performance loss
        try {
            bundle.putSparseParcelableArray(key, (SparseArray<Parcelable>) value)
            return bundle
        } catch (Exception e) {

        }
        try {
            bundle.putParcelableArrayList(key, (ArrayList<Parcelable>) value)
            return bundle
        } catch (Exception e) {

        }

        try {
            bundle.putStringArrayList(key, (ArrayList<String>) value)
            return bundle
        } catch (Exception e) {

        }

        try {
            bundle.putCharSequenceArrayList(key, (ArrayList<CharSequence>) value)
            return bundle
        } catch (Exception e) {

        }

        try {
            bundle.putIntegerArrayList(key, (ArrayList<Integer>) value)
            return bundle
        } catch (Exception e) {

        }

        return bundle
    }
}