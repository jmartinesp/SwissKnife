package com.arasthel.swissknife.annotations.parcelable

import android.os.Parcel
import com.arasthel.swissknife.annotations.ParcelableUtil
import com.arasthel.swissknife.annotations.parcelable.models.PrimitiveArrayParcelables
import spock.lang.Specification

/**
 * Created by Arasthel on 23/6/15.
 */

public class PrimitiveArrayParcelableTestSpec extends Specification {

    private static final int[] RESULT_INT = [1234]
    private static final long[] RESULT_LONG = [1234567890123]
    private static final char[] RESULT_CHAR = ['.']
    private static final short[] RESULT_SHORT = [12]
    private static final byte[] RESULT_BYTE = [7]
    private static final float[] RESULT_FLOAT = [123.456f]
    private static final double[] RESULT_DOUBLE = [123.456]

    static PrimitiveArrayParcelables parcelables

    def setupSpec() {
        Parcel parcel = Parcel.obtain()
        PrimitiveArrayParcelables original = new PrimitiveArrayParcelables()
        setParcelableValues(original)
        original.writeToParcel(parcel, 0)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        parcelables = new PrimitiveArrayParcelables(parcel)
    }

    def setParcelableValues(PrimitiveArrayParcelables PrimitiveArrayParcelables) {
        PrimitiveArrayParcelables.myInts = RESULT_INT
        PrimitiveArrayParcelables.myLongs = RESULT_LONG
        PrimitiveArrayParcelables.myChars = RESULT_CHAR
        PrimitiveArrayParcelables.myShorts = RESULT_SHORT
        PrimitiveArrayParcelables.myBytes = RESULT_BYTE
        PrimitiveArrayParcelables.myFloats = RESULT_FLOAT
        PrimitiveArrayParcelables.myDoubles = RESULT_DOUBLE
    }


    def "Parcelling int[] works"() {
        given:
        PrimitiveArrayParcelables parcelable = parcelables

        when:
        def result = parcelable.myInts

        then:
        result == RESULT_INT
    }

    def "Parcelling long[] works"() {
        given:
        PrimitiveArrayParcelables parcelable = parcelables

        when:
        def result = parcelable.myLongs

        then:
        result == RESULT_LONG
    }


    // char and short are special, since there are not writeChar and writeShort or readChar
    // and readShort methods on Parcel. They are parceled as Character and Short
    def "Parcelling char[] works"() {
        given:
        PrimitiveArrayParcelables parcelable = parcelables

        when:
        def result = parcelable.myChars

        then:
        result == RESULT_CHAR
    }

    def "Parcelling short[] works"() {
        given:
        PrimitiveArrayParcelables parcelable = parcelables

        when:
        def result = parcelable.myShorts

        then:
        result == RESULT_SHORT
    }

    // ------------------------------------------------------------------------------

    def "Parcelling byte[] works"() {
        given:
        PrimitiveArrayParcelables parcelable = parcelables

        when:
        def result = parcelable.myBytes

        then:
        result == RESULT_BYTE
    }

    def "Parcelling float[] works"() {
        given:
        PrimitiveArrayParcelables parcelable = parcelables

        when:
        def result = parcelable.myFloats

        then:
        result == RESULT_FLOAT
    }

    def "Parcelling double[] works"() {
        given:
        PrimitiveArrayParcelables parcelable = parcelables

        when:
        def result = parcelable.myDoubles

        then:
        result == RESULT_DOUBLE
    }
}
