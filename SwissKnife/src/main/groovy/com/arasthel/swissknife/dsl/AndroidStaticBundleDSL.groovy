package com.arasthel.swissknife.dsl

import android.os.Bundle;

/**
 * Created by Arasthel on 04/04/15.
 */

import groovy.transform.CompileStatic;

@CompileStatic
public class AndroidStaticBundleDSL {

    /**
     * Create Bundle from Map
     * @param c
     * @param argsMap
     * @return
     */
    static Bundle fromMap(Bundle b, Map<String, ?> argsMap) {
        Bundle bundle = new Bundle()
        bundle = AndroidBundleDSL.putFromMap(bundle, argsMap)
        return bundle
    }

}
