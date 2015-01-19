package com.arasthel.swissknife.dsl

import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

/**
 * DSL methods for attaching events to components
 *
 * @author Eugene Kamenev eugenekamenev
 */
@CompileStatic
class AndroidEventDSL {

    /**
     * Apply click closure on {@link android.widget.ListView} component
     *
     * @param listView
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends ListView, S> T onClick(T listView,
                                             @ClosureParams(value = FromString, options = ['S',
                                                     'S,android.view.View', 'S,android.view.View,java.lang.Integer']) Closure closure) {
        def clone = closure?.rehydrate(listView, closure?.owner, closure?.thisObject)
        clone?.resolveStrategy = Closure.DELEGATE_FIRST
        listView.onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            void onItemClick(AdapterView parent, View view, int position, long id) {
                switch (clone?.maximumNumberOfParameters) {
                    default: clone?.call(); break;
                    case 1: clone?.call(listView.getItemAtPosition(position)); break;
                    case 2: clone?.call(listView.getItemAtPosition(position), view); break;
                    case 3: clone?.call(listView.getItemAtPosition(position), view,
                            position); break;
                }
            }
        }
        listView
    }

    /**
     * Apply long click closure on {@link ListView} component
     *
     * @param listView
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends ListView, S> T onLongClick(T listView,
                                                 @DelegatesTo(value = T,
                                                         strategy = Closure.DELEGATE_FIRST)
                                                 @ClosureParams(value = FromString,
                                                         options = ['S', 'S,android.view.View',
                                                                 'S,android.view.View,java.lang.Integer'])
                                                         Closure<Boolean> closure) {
        def clone = closure?.rehydrate(listView, closure?.owner, closure?.thisObject)
        clone?.resolveStrategy = Closure.DELEGATE_FIRST
        listView.onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                switch (clone?.maximumNumberOfParameters) {
                    default: clone?.call(); break;
                    case 1: clone?.call(listView.getItemAtPosition(position)); break;
                    case 2: clone?.call(listView.getItemAtPosition(position), view); break;
                    case 3: clone?.call(listView.getItemAtPosition(position), view,
                            position); break;
                }
            }
        }
        listView
    }

    /**
     * Apply click closure on {@link View} component
     *
     * @param view
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends View> T onClick(T view,
                                      @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                      @ClosureParams(value = FromString,
                                              options = 'T') Closure closure) {
        def clone = closure?.rehydrate(view, closure?.owner, closure?.thisObject)
        clone?.resolveStrategy = Closure.DELEGATE_FIRST
        view?.onClickListener = new View.OnClickListener() {
            @Override
            void onClick(View v) {
                clone?.call(v)
            }
        }
        view
    }

    /**
     * Apply longClick closure on {@link View} component
     *
     * @param view
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends View> T onLongClick(T view,
                                          @DelegatesTo(value = T,
                                                  strategy = Closure.DELEGATE_FIRST)
                                          @ClosureParams(value = FromString,
                                                  options = 'T') Closure<Boolean> closure) {
        def clone = closure?.rehydrate(view, closure?.owner, closure?.thisObject)
        clone?.resolveStrategy = Closure.DELEGATE_FIRST
        view.onLongClickListener = new View.OnLongClickListener() {
            @Override
            boolean onLongClick(View v) {
                clone?.call(v)
            }
        }
        view
    }
}
