package com.arasthel.swissknife.annotations

import android.os.Parcel;

/**
 * Created by Arasthel on 23/6/15.
 */

import groovy.transform.CompileStatic;

@CompileStatic
public class ParcelableUtil {

    public static void saveAndRestoreParcel(Parcel parcel) {
        def bytes = parcel.marshall()
        parcel.unmarshall(bytes, 0, bytes.length)
        parcel.setDataPosition(0)
    }

}