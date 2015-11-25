package com.arasthel.swissknife.annotations.parcelable.models;

/**
 * Created by Arasthel on 16/7/15.
 */

import groovy.transform.CompileStatic;

@CompileStatic
public class SerializableObject implements Serializable {

    int myInt = 1

    @Override
    boolean equals(Object o) {
        if (!(o instanceof SerializableObject)) {
            return false
        } else {
            return myInt == (o as SerializableObject).myInt
        }
    }
}