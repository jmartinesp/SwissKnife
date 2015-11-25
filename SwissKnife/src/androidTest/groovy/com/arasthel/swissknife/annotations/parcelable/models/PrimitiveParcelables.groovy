package com.arasthel.swissknife.annotations.parcelable.models

import com.arasthel.swissknife.annotations.Parcelable

/**
 * Created by Arasthel on 16/7/15.
 */

import groovy.transform.CompileStatic;

@CompileStatic
@Parcelable
public class PrimitiveParcelables {

    int myInt = 5
    char myChar = 'c'
    float myFloat = 1.2f
    double myDouble = 1.22
    long myLong = 1
    short myShort = 1
    byte myByte = 1

}