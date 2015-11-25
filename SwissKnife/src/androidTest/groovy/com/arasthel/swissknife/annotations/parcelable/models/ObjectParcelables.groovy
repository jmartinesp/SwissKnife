package com.arasthel.swissknife.annotations.parcelable.models

import android.os.Bundle
import android.util.SparseArray
import com.arasthel.swissknife.annotations.Parcelable
import groovy.transform.CompileStatic

/**
 * Created by Arasthel on 16/7/15.
 */
@CompileStatic
@Parcelable
public class ObjectParcelables {

    String myString = ""
    Map myMap = [:]
    List<Object> myList = []
    AndroidParcelableImplementedObject myParcelable
    SparseArray mySparseArray
    Bundle myBundle
    CharSequence myCharSequence
    SerializableObject mySerializable
    String myUninitializedString
    String myAlwaysNullString

}