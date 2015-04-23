package com.arasthel.swissknife.dsl.components

import android.os.AsyncTask
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

/**
 * Simple android Async Task
 *
 * @author Eugene Kamenev @eugenekamenev
 * @since 0.1
 * @param < T >
 */
@CompileStatic
class GAsyncTask<T> extends AsyncTask<Object, Object, T> {
    /**
     * Closure which will be executed as background task and return T
     */
    private final Closure<T> task

    /**
     * Closure which will be executed after successful task execution
     * Is optional
     */
    private Closure after

    /**
     * Closure which will be executed if error occurs while executing task
     * Is optional
     */
    private Closure error

    /**
     * Exception for error closure
     */
    private Exception exception

    GAsyncTask(Closure<T> task) {
        this.task = task
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected T doInBackground(Object... params) {
        try {
            return task?.call(params ? params[0] : null, this)
        } catch (Exception ignore) {
            this.exception = ignore
            null
        }
    }

    /**
     * After task execution method
     * @param object
     */
    @Override
    protected void onPostExecute(T object) {
        if (exception || !object) {
            this.error?.call(exception)
        }
        else {
            this.after?.call(object)
        }
    }

    void before(Closure closure) {
        this.before = closure
    }

    /**
     * after closure setter
     * @param closure
     */
    void after(@ClosureParams(value = FromString, options = 'T') Closure closure) {
        this.after = closure
    }

    /**
     * error closure setter
     * @param closure
     */
    void error(
            @ClosureParams(value = FromString, options = 'java.lang.Exception') Closure closure) {
        this.error = closure
    }
}
