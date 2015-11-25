package com.arasthel.swissknife.annotations.parcelable

import android.os.BadParcelableException
import android.os.Parcel
import com.arasthel.swissknife.annotations.ParcelableUtil
import com.arasthel.swissknife.annotations.parcelable.models.AndroidParcelableImplementedObject
import com.arasthel.swissknife.annotations.parcelable.models.ListParcelables
import com.arasthel.swissknife.annotations.parcelable.models.MultipleListParcelables
import com.arasthel.swissknife.annotations.parcelable.models.NoTypedListParcelables
import com.arasthel.swissknife.annotations.parcelable.models.SerializableObject
import spock.lang.Specification

/**
 * Created by Arasthel on 23/6/15.
 */

public class ListParcelableTestSpec extends Specification {

    private static final List RESULT_LIST_OBJECT = [new AndroidParcelableImplementedObject(), new AndroidParcelableImplementedObject()]
    private static final List RESULT_LIST_PARCELABLE = [new AndroidParcelableImplementedObject(), new AndroidParcelableImplementedObject()]
    private static final List RESULT_LIST_MULTIPLE_TYPES = [new AndroidParcelableImplementedObject(), new SerializableObject()]


    def "Parcelling a non-typed List fails"() {
        given:
        Parcel parcel = Parcel.obtain()
        NoTypedListParcelables parcelable = new NoTypedListParcelables()
        parcelable.myNotTypedList = RESULT_LIST_OBJECT
        parcelable.writeToParcel(parcel, 0)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        NoTypedListParcelables resultParcelable
        try {
            resultParcelable = new NoTypedListParcelables(parcel)
        } catch (Exception e) {
            println(e.message)
        }

        when:
        def result = resultParcelable?.myNotTypedList

        then:
        result == null
    }

    def "Parcelling a Parcelable List works"() {
        given:

        Parcel parcel = Parcel.obtain()
        ListParcelables parcelable = new ListParcelables()
        parcelable.myParcelableList = RESULT_LIST_PARCELABLE
        parcelable.writeToParcel(parcel, 0)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        ListParcelables resultParcelable = null
        try {
            resultParcelable = new ListParcelables(parcel)
        } catch (Exception e) {
            e.printStackTrace()
        }

        when:
        def result = resultParcelable?.myParcelableList

        then:
        result == RESULT_LIST_PARCELABLE
    }

    def "Parcelling a List with several types fails"() {
        given:
        Parcel parcel = Parcel.obtain()
        MultipleListParcelables parcelable = new MultipleListParcelables()
        parcelable.myMultipleParcelableTypeList = RESULT_LIST_MULTIPLE_TYPES
        parcelable.writeToParcel(parcel, 0)
        ParcelableUtil.saveAndRestoreParcel(parcel)
        MultipleListParcelables resultParcelable
        try {
            resultParcelable = new MultipleListParcelables(parcel)
        } catch (Exception e) {
            println(e.message)
        }

        when:
        def result = resultParcelable?.myMultipleParcelableTypeList

        then:
        result == null
    }
}
