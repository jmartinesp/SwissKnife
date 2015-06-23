package com.arasthel.swissknife.annotations

import android.os.Parcel

/**
 * Created by Arasthel on 23/6/15.
 */

import spock.lang.Specification

public class ParcelableTestSpec extends Specification {

    def "parcel String works"() {
        given:
            def string = "this is a test"
            def parcel = Parcel.obtain()

        when:
            parcel.writeValue(string)
            def resultString = parcel.readString()

        then:
            resultString != null
    }

}
