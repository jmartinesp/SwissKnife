package com.arasthel.swissknife.annotations

import android.os.Parcel
import com.andrewreitz.spock.android.AndroidSpecification

/**
 * Created by Arasthel on 23/6/15.
 */

public class ParcelableTestSpec extends AndroidSpecification {

    def "Parcelling String works"() {
        given:
            String string = "this is a test"
            Parcel parcel = Parcel.obtain()

        when:
            parcel.writeValue(string)
            ParcelableUtil.saveAndRestoreParcel(parcel)
            String resultString = parcel.readValue(null)

        then:
            resultString == string
    }

    def "Parcelling List of parcelable objects works"() {
        given:
            String string = "this is a test"
            List<String> list = [string, string, string]
            Parcel parcel = Parcel.obtain()

        when:
            parcel.writeValue(list)
            ParcelableUtil.saveAndRestoreParcel(parcel)
            List<String> resultList = parcel.readValue(null)

        then:
            resultList == list
    }

    def "Parcelling an @Parcelable annotated object works"() {
        given:
        Person
        Parcel parcel = Parcel.obtain()

        when:
        parcel.writeValue(string)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        String resultString = parcel.readValue(null)

        then:
        resultString == string
    }

    def "Parcelling List of non parcelable objects throws RuntimeException"() {
        given:
            List<ParcelableUtil> list = [new ParcelableUtil()]
            Parcel parcel = Parcel.obtain()

        when:
            parcel.writeValue(list)
            ParcelableUtil.saveAndRestoreParcel(parcel)
            List<ParcelableUtil> resultList = parcel.readValue(null)

        then:
            RuntimeException e = thrown()
            e != null
    }

}
