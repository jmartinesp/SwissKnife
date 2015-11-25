package com.arasthel.swissknife.annotations.parcelable.models

import com.arasthel.swissknife.annotations.Parcelable
import groovy.transform.CompileStatic

/**
 * Created by Arasthel on 16/7/15.
 */
@CompileStatic
@Parcelable
public class PrimitiveArrayParcelables {

    int[] myInts = []
    char[] myChars = []
    float[] myFloats = []
    double[] myDoubles = []
    long[] myLongs = []
    short[] myShorts = []
    byte[] myBytes = []

}