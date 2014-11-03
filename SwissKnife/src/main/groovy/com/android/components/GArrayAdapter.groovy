package com.android.components

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import groovy.transform.CompileStatic

/**
 * Simple array adapter
 *
 * @param < T >
 * @since 0.1
 */
@CompileStatic
class GArrayAdapter<T> extends ArrayAdapter<T> {
    final List<T> values
    final int resource
    Closure onItemClosure

    GArrayAdapter(Context context, int resource, List<T> values, Closure buildClosure) {
        super(context, resource, values)
        this.resource = resource
        this.values = values
        this.onItemClosure = buildClosure
    }

    /**
     * View objects will be automatically holded in convertView.getTag()
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(this.resource, parent, false)
        }
        if (!convertView.getTag()) {
            convertView.setTag(new SparseArray<View>())
        }
        this.onItemClosure?.delegate = convertView
        onItemClosure?.resolveStrategy = Closure.DELEGATE_FIRST
        switch (onItemClosure?.maximumNumberOfParameters) {
            default: onItemClosure?.call(values[position]); break;
            case 2: onItemClosure?.call(values[position], convertView); break;
            case 3: onItemClosure?.call(values[position], convertView, position); break;
        }
        convertView
    }
}
