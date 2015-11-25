package com.arasthel.swissknife.annotations.parcelable

import android.os.Parcel
import com.arasthel.swissknife.annotations.ParcelableUtil
import com.arasthel.swissknife.annotations.parcelable.models.PrimitiveParcelables
import spock.lang.Specification

/**
 * Created by Arasthel on 23/6/15.
 */

public class PrimitiveParcelableTestSpec extends Specification {

    private static final int RESULT_INT = 1234
    private static final long RESULT_LONG = 1234567890123
    private static final char RESULT_CHAR = '.'
    private static final short RESULT_SHORT = 12
    private static final byte RESULT_BYTE = 7
    private static final float RESULT_FLOAT = 123.456f
    private static final double RESULT_DOUBLE = 123.456

    static PrimitiveParcelables parcelables

    def setupSpec() {
        Parcel parcel = Parcel.obtain()
        PrimitiveParcelables original = new PrimitiveParcelables()
        setParcelableValues(original)
        original.writeToParcel(parcel, 0)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        parcelables = new PrimitiveParcelables(parcel)
    }

    def setParcelableValues(PrimitiveParcelables primitiveParcelables) {
        primitiveParcelables.myInt = RESULT_INT
        primitiveParcelables.myLong = RESULT_LONG
        primitiveParcelables.myChar = RESULT_CHAR
        primitiveParcelables.myShort = RESULT_SHORT
        primitiveParcelables.myByte = RESULT_BYTE
        primitiveParcelables.myFloat = RESULT_FLOAT
        primitiveParcelables.myDouble = RESULT_DOUBLE
    }


    def "Parcelling int works"() {
        given:
        PrimitiveParcelables parcelable = parcelables

        when:
        def result = parcelable.myInt

        then:
        result == RESULT_INT
    }

    def "Parcelling long works"() {
        given:
        PrimitiveParcelables parcelable = parcelables

        when:
        def result = parcelable.myLong

        then:
        result == RESULT_LONG
    }


    // char and short are special, since there are not writeChar and writeShort or readChar
    // and readShort methods on Parcel. They are parceled as Character and Short
    def "Parcelling char works"() {
        given:
        PrimitiveParcelables parcelable = parcelables

        when:
        def result = parcelable.myChar

        then:
        result == RESULT_CHAR
    }

    def "Parcelling short works"() {
        given:
        PrimitiveParcelables parcelable = parcelables

        when:
        def result = parcelable.myShort

        then:
        result == RESULT_SHORT
    }

    // ------------------------------------------------------------------------------

    def "Parcelling byte works"() {
        given:
        PrimitiveParcelables parcelable = parcelables

        when:
        def result = parcelable.myByte

        then:
        result == RESULT_BYTE
    }

    def "Parcelling float works"() {
        given:
        PrimitiveParcelables parcelable = parcelables

        when:
        def result = parcelable.myFloat

        then:
        result == RESULT_FLOAT
    }

    def "Parcelling double works"() {
        given:
        PrimitiveParcelables parcelable = parcelables

        when:
        def result = parcelable.myDouble

        then:
        result == RESULT_DOUBLE
    }
}
