package com.arasthel.swissknife.dsl.components

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.view.animation.Animation
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder;

/**
 * Created by Arasthel on 12/03/15.
 */

@CompileStatic
@Builder
public class FragmentTransactionBuilder {

    FragmentActivity activity
    int replacedViewId
    Fragment fragment
    String fragmentTag
    List<Integer> customAnimations
    boolean addToBackStack

    public FragmentTransaction buildReplace() {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

        setAnimations(transaction)

        if(fragmentTag) {
            transaction.replace(replacedViewId, fragment, fragmentTag)
            addToBacksTackIfNeeded(transaction)
        } else {
            transaction.replace(replacedViewId, fragment)
        }

        return transaction;
    }

    public FragmentTransaction buildAdd() {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

        setAnimations(transaction)

        if(fragmentTag) {
            transaction.add(replacedViewId, fragment, fragmentTag)
            addToBacksTackIfNeeded(transaction)
        } else {
            transaction.add(replacedViewId, fragment)
        }

        return transaction;
    }

    private void setAnimations(FragmentTransaction transaction) {
        if(customAnimations) {
            if(customAnimations.size() == 2) {
                transaction.setCustomAnimations(customAnimations[0], customAnimations[1])
            } else {
                transaction.setCustomAnimations(customAnimations[0], customAnimations[1],
                        customAnimations[2], customAnimations[3])
            }
        }
    }

    private void addToBacksTackIfNeeded(FragmentTransaction transaction) {
        if(addToBackStack) {
            transaction.addToBackStack(fragmentTag)
        }
    }
}
