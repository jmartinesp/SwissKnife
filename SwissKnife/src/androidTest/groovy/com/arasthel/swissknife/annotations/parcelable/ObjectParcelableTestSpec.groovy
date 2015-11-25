package com.arasthel.swissknife.annotations.parcelable

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import com.arasthel.swissknife.annotations.ParcelableUtil
import com.arasthel.swissknife.annotations.parcelable.models.AndroidParcelableImplementedObject
import com.arasthel.swissknife.annotations.parcelable.models.ObjectParcelables
import com.arasthel.swissknife.annotations.parcelable.models.SerializableObject
import spock.lang.Specification

/**
 * Created by Arasthel on 23/6/15.
 */

public class ObjectParcelableTestSpec extends Specification {

    private static final String RESULT_STRING = "SWISSKNIFE ROCKS"
    private static final Map RESULT_MAP = ["ok": 1, 2: "b", "object": new ArrayList<String>()]
    private static final List RESULT_LIST = [1, 2, "three", 4.0f, new SerializableObject()]
    private static final SparseArray RESULT_SPARSEARRAY = getSparseArrayConstant()
    private static final Parcelable RESULT_PARCELABLE = new AndroidParcelableImplementedObject()
    private static final Bundle RESULT_BUNDLE = getBundleConstant()
    private static final CharSequence RESULT_CHARSEQUENCE = RESULT_STRING as CharSequence
    private static final Serializable RESULT_SERIALIZABLE = new SerializableObject()

    static ObjectParcelables parcelables

    private static final Bundle getBundleConstant() {
        Bundle b = new Bundle()
        b.putString("aString", "a")
        return b
    }

    private static final SparseArray getSparseArrayConstant() {
        SparseArray<Object> sparseArray = new SparseArray<>()
        sparseArray.append(1, RESULT_STRING)
        return sparseArray
    }

    def setupSpec() {
        Parcel parcel = Parcel.obtain()
        ObjectParcelables original = new ObjectParcelables()
        setParcelableValues(original)
        original.writeToParcel(parcel, 0)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        parcelables = new ObjectParcelables(parcel)
    }

    def setParcelableValues(ObjectParcelables objectParcelables) {
        objectParcelables.myString = RESULT_STRING
        objectParcelables.myMap = RESULT_MAP
        objectParcelables.myList = RESULT_LIST
        objectParcelables.mySparseArray = RESULT_SPARSEARRAY
        objectParcelables.myParcelable = RESULT_PARCELABLE
        objectParcelables.myBundle = RESULT_BUNDLE
        objectParcelables.myCharSequence = RESULT_CHARSEQUENCE
        objectParcelables.mySerializable = RESULT_SERIALIZABLE
    }


    def "Parcelling String works"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def result = parcelable.myString

        then:
        result == RESULT_STRING
    }

    def "Parcelling Map works"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def result = parcelable.myMap

        then:
        result == RESULT_MAP
    }

    def "Parcelling List works"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def result = parcelable.myList

        then:
        println(RESULT_LIST)
        println(result)
        result.equals(RESULT_LIST)
    }

    def "Parcelling SparseArray works"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def sparseArray = parcelable.mySparseArray
        def result = sparseArray.get(1)

        then:
        result == RESULT_SPARSEARRAY.get(1)
    }

    def "Parcelling Parcelable works"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def result = parcelable.myParcelable

        then:
        result.equals(RESULT_PARCELABLE)
    }

    def "Parcelling Bundle works"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def bundle = parcelable.myBundle
        def result = bundle.getString("aString")

        then:
        result == RESULT_BUNDLE.getString("aString")
    }

    def "Parcelling CharSequence works"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def result = parcelable.myCharSequence

        then:
        result.equals(RESULT_CHARSEQUENCE)
    }

    def "Parcelling Serializable works"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def result = parcelable.mySerializable

        then:
        result.equals(RESULT_SERIALIZABLE)
    }

    def "A non initialized Object doesn't crash"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def result = parcelable.myUninitializedString

        then:
        result == null
    }

    def "A null Object doesn't crash"() {
        given:
        ObjectParcelables parcelable = parcelables

        when:
        def result = parcelable.myAlwaysNullString

        then:
        result == null
    }
}
