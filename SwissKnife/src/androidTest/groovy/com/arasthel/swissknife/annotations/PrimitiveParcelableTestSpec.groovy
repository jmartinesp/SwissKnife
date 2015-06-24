package com.arasthel.swissknife.annotations

import android.os.Parcel
import com.andrewreitz.spock.android.AndroidSpecification

/**
 * Created by Arasthel on 23/6/15.
 */

public class PrimitiveParcelableTestSpec extends AndroidSpecification {

    def "Parcelling int works"() {
        given:
        int i = 1234
        Parcel parcel = Parcel.obtain()

        when:
        parcel.writeInt(1234)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        int result = parcel.readInt()

        then:
        result == i
    }

    def "Parcelling long works"() {
        given:
        long l = 123456
        Parcel parcel = Parcel.obtain()

        when:
        parcel.writeLong(l)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        long result = parcel.readLong()

        then:
        result == l
    }

    def "Parcelling float works"() {
        given:
        float f = 1.2
        Parcel parcel = Parcel.obtain()

        when:
        parcel.writeFloat(f)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        float result = parcel.readFloat()

        then:
        result == f
    }

    def "Parcelling double works"() {
        given:
        double d = 1.2
        Parcel parcel = Parcel.obtain()

        when:
        parcel.writeDouble(d)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        double result = parcel.readDouble()

        then:
        result == d
    }

    def "Parcelling byte works"() {
        given:
        byte b = 1
        Parcel parcel = Parcel.obtain()

        when:
        parcel.writeByte(b)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        byte result = parcel.readByte()

        then:
        result == b
    }

}
