package com.arasthel.swissknife.annotations.parcelable.models

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator;

/**
 * Created by Arasthel on 16/7/15.
 */

import groovy.transform.CompileStatic;

@CompileStatic
public class AndroidParcelableImplementedObject implements Parcelable {

    int myInt = 1

    @Override
    int describeContents() {
        return 0
    }

    @Override
    void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(myInt)
    }

    public static Creator CREATOR = new Creator<AndroidParcelableImplementedObject>() {
        @Override
        AndroidParcelableImplementedObject createFromParcel(Parcel source) {
            AndroidParcelableImplementedObject object = new AndroidParcelableImplementedObject()
            object.myInt = source.readInt()
            return object
        }

        @Override
        AndroidParcelableImplementedObject[] newArray(int size) {
            return new AndroidParcelableImplementedObject[size]
        }
    }

    @Override
    boolean equals(Object o) {
        if (!(o instanceof AndroidParcelableImplementedObject)) {
            return false
        } else {
            return myInt == (o as AndroidParcelableImplementedObject).myInt
        }
    }
}