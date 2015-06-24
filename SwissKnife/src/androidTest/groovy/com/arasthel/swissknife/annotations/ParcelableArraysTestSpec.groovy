package com.arasthel.swissknife.annotations

import android.os.Parcel
import com.andrewreitz.spock.android.AndroidSpecification

/**
 * Created by Arasthel on 23/6/15.
 */

public class ParcelableArraysTestSpec extends AndroidSpecification {

    def "Parcelling String[] works"() {
        given:
            String string = "this is a test"
            String[] strings = [string, string, string]
            Parcel parcel = Parcel.obtain()

        when:
            parcel.writeValue(strings)
            ParcelableUtil.saveAndRestoreParcel(parcel)
            String[] resultStrings = parcel.readValue(null)

        then:
            resultStrings == strings
    }

}
