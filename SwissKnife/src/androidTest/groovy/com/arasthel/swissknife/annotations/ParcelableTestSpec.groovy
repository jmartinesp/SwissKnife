package com.arasthel.swissknife.annotations

import android.os.Parcel;

/**
 * Created by Arasthel on 23/6/15.
 */

import spock.lang.Specification;

public class ParcelableTestSpec extends Specification {

    def "parcel String works"() {
        given:
            def string = "this is a test"
            def parcel = new Parcel()

        when:
            parcel.writeValue(string)
            def resultString = parcel.readString()

        then:
            resultString != null
    }

}