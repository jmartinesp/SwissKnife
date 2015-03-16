package com.arasthel.swissknife

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.view.ViewPager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.TextView
import com.arasthel.swissknife.annotations.OnItemSelected
import com.arasthel.swissknife.annotations.OnPageChanged
import com.arasthel.swissknife.annotations.OnTextChanged
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.codehaus.groovy.ast.ClassHelper

import java.lang.reflect.Method

/**
 * Created by Arasthel on 17/08/14.
 */
@CompileStatic
public class SwissKnife {

    public static Handler mHandler = new Handler(Looper.getMainLooper());

    public static final String TAG = "SwissKnife";

    @TypeChecked(TypeCheckingMode.SKIP)
    public static void inject(Object target) {
        target.injectViews(target)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    public static void inject(Object target, View view) {
        target.injectViews(view)
    }
	
    @TypeChecked(TypeCheckingMode.SKIP)
    public static void inject(Object target, Activity activity) {
        target.injectViews(activity)
    }

    public static boolean setOnClick(View v, Object target, String methodName) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            void onClick(View view) {
                Method method = null;
                if ((method = SwissKnife.searchMethod(target, methodName, [View.class])) != null) {
                    method.invoke(target, view);
                }
                else if ((method = SwissKnife.searchMethod(target, methodName, [])) != null) {
                    method.invoke(target);
                }
                else {
                    Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                            "\ta) public void onClick(View v)\n" +
                            "\tb) public void onClick()");
                }
            }
        });
    }

    public static void setOnItemClick(AbsListView v, Object target, String methodName) {
        v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Method method = null;
                if ((method = SwissKnife.searchMethod(target, methodName, [int,
                                                                           AdapterView.class]))
                        != null) {
                    method.invoke(target, [i, adapterView].toArray());
                }
                else if ((method = SwissKnife.searchMethod(target, methodName, [int,
                                                                                AdapterView
                                                                                        .class,
                                                                                View.class])) !=
                        null) {
                    method.invoke(target, [i, adapterView, view].toArray());
                }
                else if ((method = SwissKnife.searchMethod(target, methodName, [int])) != null) {
                    method.invoke(target, i);
                }
                else {
                    Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                            "\ta) public void onItemClick(int position, AdapterView list)\n" +
                            "\tb) public void onItemClick(int position, AdapterView list, " +
                            "View clickedView)\n" +
                            "\tc) public void onItemClick(int position)");
                }
            }
        });
    }

    public
    static void setOnItemSelected(AbsListView v, Object target, String methodName,
                                  String methodStr) {
        OnItemSelected.Method methodEnum = OnItemSelected.Method.valueOf(methodStr);
        v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (OnItemSelected.Method.ITEM_SELECTED == methodEnum) {
                    Method method = null;
                    if ((method = SwissKnife.searchMethod(target, methodName, [int,
                                                                               AdapterView
                                                                                       .class]))
                            != null) {
                        method.invoke(target, [i, adapterView].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName, [int,
                                                                                    AdapterView
                                                                                            .class, View.class])) != null) {
                        method.invoke(target, [i, adapterView, view].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName,
                            [int])) != null) {
                        method.invoke(target, i);
                    }
                    else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onItemSelected(int position, " +
                                "AdapterView list)\n" +
                                "\tb) public void onItemSelected(int position, AdapterView list, " +
                                "View clickedView)\n" +
                                "\tc) public void onItemSelected(int position)");
                    }
                }
            }

            @Override
            void onNothingSelected(AdapterView<?> adapterView) {
                if (OnItemSelected.Method.NOTHING_SELECTED == methodEnum) {
                    Method method = null;
                    if ((method = SwissKnife.searchMethod(target, methodName,
                            [AdapterView.class])) != null) {
                        method.invoke(target, [adapterView].toArray());
                    }
                    else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onNothingSelected(AdapterView list)");
                    }
                }
            }
        });
    }

    public
    static void setOnPageChanged(ViewPager v, Object target, String methodName, String methodStr) {
        OnPageChanged.Method methodEnum = OnPageChanged.Method.valueOf(methodStr);
        v.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            void onPageScrolled(int position, float offset, int offsetInPixels) {
                if (OnPageChanged.Method.PAGE_SCROLLED == methodEnum) {
                    Method method = null
                    if ((method = SwissKnife.searchMethod(target, methodName, [int])) != null) {
                        method.invoke(target, [position].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName, [int, float,
                                                                                    int])) !=
                            null) {
                        method.invoke(target, [position, offset, offsetInPixels].toArray());
                    }
                    else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onPageScrolled(int position)\n" +
                                "\tb) public void onPageScrolled(int position, float offset, " +
                                "int pixelOffset)");
                    }
                }
            }

            @Override
            void onPageSelected(int position) {
                if (OnPageChanged.Method.PAGE_SELECTED == methodEnum) {
                    Method method = null;
                    if ((method = SwissKnife.searchMethod(target, methodName, [int])) != null) {
                        method.invoke(target, [position].toArray());
                    }
                    else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onPageSelected(int position)");
                    }
                }
            }

            @Override
            void onPageScrollStateChanged(int state) {
                if (OnPageChanged.Method.PAGE_SCROLL_STATE_CHANGED == methodEnum) {
                    Method method = null;
                    if ((method = SwissKnife.searchMethod(target, methodName, [int])) != null) {
                        method.invoke(target, [state].toArray());
                    }
                    else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onPageScrollStateChanged(int state)");
                    }
                }
            }
        });
    }


    public
    static void setOnTextChanged(TextView v, Object target, String methodName, String methodStr) {
        OnTextChanged.Method methodEnum = OnTextChanged.Method.valueOf(methodStr);
        v.addTextChangedListener(new TextWatcher() {

            @Override
            void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                if (OnTextChanged.Method.BEFORE_TEXT_CHANGED == methodEnum) {
                    Method method = null;
                    if ((method = SwissKnife.searchMethod(target, methodName,
                            [CharSequence.class])) != null) {
                        method.invoke(target, [charSequence].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName,
                            [TextView.class, CharSequence.class])) != null) {
                        method.invoke(target, [v, charSequence].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName,
                            [CharSequence.class, int, int, int])) != null) {
                        method.invoke(target, [charSequence, start, count, after].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName,
                            [TextView.class, CharSequence.class, int, int, int])) != null) {
                        method.invoke(target, [v, charSequence, start, count, after].toArray());
                    }
                    else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void beforeTextChanged(CharSequence sequence)\n" +
                                "\tb) public void beforeTextChanged(TextView textView, " +
                                "CharSequence sequence)\n" +
                                "\tc) public void beforeTextChanged(CharSequence sequence, " +
                                "int start, int count, int after)\n" +
                                "\td) public void beforeTextChanged(TextView textView, " +
                                "CharSequence sequence, int start, int count, int after)");
                    }
                }
            }

            @Override
            void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (OnTextChanged.Method.ON_TEXT_CHANGED == methodEnum) {
                    Method method = null;
                    if ((method = SwissKnife.searchMethod(target, methodName,
                            [CharSequence.class])) != null) {
                        method.invoke(target, [charSequence].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName, [View.class,
                                                                                    CharSequence
                                                                                            .class])) != null) {
                        method.invoke(target, [v, charSequence].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName,
                            [CharSequence.class, int, int, int])) != null) {
                        method.invoke(target, [charSequence, start, before, count].toArray());
                    }
                    else if ((method = SwissKnife.searchMethod(target, methodName, [View.class,
                                                                                    CharSequence
                                                                                            .class, int, int, int])) != null) {
                        method.invoke(target, [v, charSequence, start, before, count].toArray());
                    }
                    else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onTextChanged(CharSequence sequence)\n" +
                                "\tb) public void onTextChanged(TextView textView, " +
                                "CharSequence sequence)\n" +
                                "\tc) public void onTextChanged(CharSequence sequence, int start," +
                                " int before, int count)\n" +
                                "\td) public void onTextChanged(TextView textView, " +
                                "CharSequence sequence, int start, int before, int count)");
                    }
                }
            }

            @Override
            void afterTextChanged(Editable editable) {
                if (OnTextChanged.Method.ON_TEXT_CHANGED == methodEnum) {
                    Method method = null;
                    if ((method = SwissKnife.searchMethod(target, methodName,
                            [Editable.class])) != null) {
                        method.invoke(target, [editable].toArray());
                    }
                    else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void afterTextChanged(Editable editable)");
                    }
                }
            }
        });
    }

    public static void setOnChecked(CompoundButton v, Object target, String methodName) {
        v.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Method method = null;
                if ((method = SwissKnife.searchMethod(target, methodName, [CompoundButton.class,
                                                                           boolean.class])) !=
                        null) {
                    method.invoke(target, [compoundButton, b].toArray());
                }
                else if ((method = SwissKnife.searchMethod(target, methodName,
                        [boolean.class])) != null) {
                    method.invoke(target, b);
                }
                else {
                    Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                            "\ta) public void onChecked(CompoundButton button, boolean checked)\n" +
                            "\tb) public void onChecked(boolean checked)");
                }
            }
        });
    }


    public static void setOnFocusChanged(View v, Object target, String methodName) {
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            void onFocusChange(View view, boolean b) {
                Method method = null;
                if ((method = SwissKnife.searchMethod(target, methodName, [View,
                                                                           boolean.class])) !=
                        null) {
                    method.invoke(target, [view, b].toArray());
                }
                else if ((method = SwissKnife.searchMethod(target, methodName,
                        [boolean.class])) != null) {
                    method.invoke(target, b);
                }
                else {
                    Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                            "\ta) public void onFocusChanged(View view, boolean hasFocus)\n" +
                            "\tb) public void onFocusChanged(boolean hasFocus)");
                }
            }
        });
    }

    public static void setOnEditorAction(TextView v, Object target, String methodName) {
        v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                Method method = null;
                if ((method = SwissKnife.searchMethod(target, methodName, [TextView.class,
                                                                           KeyEvent.class])) !=
                        null) {
                    return method.invoke(target, [textView, keyEvent].toArray());
                }
                else if ((method = SwissKnife.searchMethod(target, methodName,
                        [KeyEvent.class])) != null) {
                    return method.invoke(target, keyEvent);
                }
                else {
                    Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                            "\ta) public boolean onEditorAction(TextView textview, " +
                            "KeyEvent event)\n" +
                            "\tb) public boolean onEditorAction(KeyEvent event)");
                }
                return false;
            }
        });
    }

    public static void setOnLongClick(View v, Object target, String methodName) {
        v.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            boolean onLongClick(View view) {
                Method method = null;
                if ((method = SwissKnife.searchMethod(target, methodName, [View.class])) != null) {
                    return method.invoke(target, view);
                }
                else if ((method = SwissKnife.searchMethod(target, methodName, [])) != null) {
                    return method.invoke(target, [].toArray());
                }
                else {
                    Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                            "\ta) public boolean onLongClick(View view)\n" +
                            "\tb) public boolean onLongClick()");
                }
                return false
            }
        });
    }

    public static void setOnItemLongClick(AbsListView v, Object target, String methodName) {
        v.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Method method = null;
                if ((method = SwissKnife.searchMethod(target, methodName, [int,
                                                                           AdapterView.class]))
                        != null) {
                    return method.invoke(target, [i, adapterView].toArray());
                }
                else if ((method = SwissKnife.searchMethod(target, methodName, [int,
                                                                                AdapterView
                                                                                        .class,
                                                                                View.class])) !=
                        null) {
                    return method.invoke(target, [i, adapterView, view].toArray());
                }
                else if ((method = SwissKnife.searchMethod(target, methodName, [int])) != null) {
                    return method.invoke(target, i);
                }
                else {
                    Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                            "\ta) public boolean onItemLongClick(int position, " +
                            "AdapterView list)\n" +
                            "\tb) public boolean onItemLongClick(int position, AdapterView list, " +
                            "View clickedView)\n" +
                            "\tc) public boolean onItemLongClick(int position)");
                }
                return false;
            }
        });
    }

    public static void setOnTouch(View v, Object target, String methodName) {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            boolean onTouch(View view, MotionEvent motionEvent) {
                Method method = null;
                if ((method = SwissKnife.searchMethod(target, methodName,
                        [MotionEvent.class])) != null) {
                    return method.invoke(target, [motionEvent].toArray());
                }
                else if ((method = SwissKnife.searchMethod(target, methodName, [View.class,
                                                                                MotionEvent
                                                                                        .class]))
                        != null) {
                    return method.invoke(target, [view, motionEvent].toArray());
                }
                else {
                    Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                            "\ta) public boolean onTouch(MotionEvent event)\n" +
                            "\tb) public boolean onTouch(View view, MotionEvent event)");
                }
                return false;
            }
        });
    }

    public static void runOnBackground(Object target, String methodName, Object... parameters) {
        Method method = null;
        List<Class> parameterClasses = new ArrayList<Class>();
        for (Object o : parameters) {
            parameterClasses << o.class;
        }
        if ((method = SwissKnife.searchMethod(target, methodName, parameterClasses)) != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    void run() {
                        method.invoke(target, parameters);
                    }
                }).start();
            }
            else {
                method.invoke(target, parameters);
            }
        }
    }

    public static void runOnUIThread(Object target, String methodName, Object... parameters) {
        Method method = null;
        List<Class> parameterClasses = new ArrayList<Class>();
        for (Object o : parameters) {
            parameterClasses << o.class;
        }
        if ((method = SwissKnife.searchMethod(target, methodName, parameterClasses)) != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                method.invoke(target, parameters);
            }
            else {
                mHandler.post(new Runnable() {
                    @Override
                    void run() {
                        method.invoke(target, parameters);
                    }
                });
            }
        }
    }


    public
    static Method searchMethod(Object currentObject, String name, List<Class> originalParameters) {
        Class[] parameters = new Class[originalParameters.size()];
        originalParameters.toArray(parameters);

        Method method = null;

        // We use a set so methods can't appear twice in here
        Set<Method> methods = new HashSet<>()
        if (currentObject instanceof Class) {
            methods.addAll(currentObject.getMethods())
            // Search for private methods, too
            methods.addAll(currentObject.getDeclaredMethods())
        }
        else {
            methods.addAll(currentObject.class.getMethods())
            // Search for private methods, too
            methods.addAll(currentObject.class.getDeclaredMethods())
        }

        // As getMethod(...) doesn't search for a compatible but an exact match,
        // we have to search manually
        for (Method m : methods) {
            if (m.getName() == name) {
                if (m.getParameterTypes().length == parameters.length) {
                    boolean found = true
                    for (int i = 0; i < m.getParameterTypes().length; i++) {
                        Class parameter = m.getParameterTypes()[i]

                        // If parameter is a primitive, we have to get its wrapper class so we
                        // can check for inheritance
                        if (parameter.isPrimitive() && !parameters[i].isPrimitive()) {
                            parameter = getWrapperForPrimitive(parameter)
                        }
                        if (!parameter.isAssignableFrom(parameters[i])) {
                            found = false
                        }
                    }
                    if (found == true) {
                        method = m
                        break
                    }
                }
            }
        }

        // If method is private (shouldn't be), we make it accessible
        if (method && !method.isAccessible()) {
            method.setAccessible(true)
        }

        return method;
    }

    private static Class getWrapperForPrimitive(Class primitive) {
        return ClassHelper.getWrapper(ClassHelper.make(primitive)).getTypeClass();
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    public static void restoreState(Object target, Bundle state) {
        if (state) {
            target.restoreSavedState(state)
        }


    }
}