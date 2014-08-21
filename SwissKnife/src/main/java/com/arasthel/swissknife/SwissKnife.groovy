package com.arasthel.swissknife

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
import com.arasthel.swissknife.utils.AnnotationUtils
import com.arasthel.swissknife.annotations.OnPageChanged
import com.arasthel.swissknife.annotations.OnTextChanged
import groovy.transform.CompileStatic

import java.lang.reflect.Method

/**
 * Created by Arasthel on 17/08/14.
 */
@CompileStatic
public class SwissKnife {

    public static final String TAG = "SwissKnife";

    public static void inject(Object target) {
        if (target.metaClass.respondsTo(target, "injectViews")) {
            target.invokeMethod("injectViews", null);
        } else {
            Log.e(TAG, "Could not inject class " + target.class.name);
        }
    }

    public static void inject(Object target, View view) {
        if (target.metaClass.respondsTo(target, "injectViews")) {
            target.invokeMethod("injectViews", [view].toArray());
        } else {
            Log.e(TAG, "Could not inject class " + target.class.name);
        }
    }

    public static boolean setOnClick(View v, Object target, String methodName) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            void onClick(View view) {
                if (target.metaClass.respondsTo(target, methodName)) {
                    if(SwissKnife.searchMethod(target, methodName, [View.class])) {
                        target.invokeMethod(methodName, view);
                    } else if(SwissKnife.searchMethod(target, methodName, [])) {
                        target.invokeMethod(methodName, null);
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onClick(View v)\n" +
                                "\tb) public void onClick()");
                    }
                } else {
                    SwissKnife.printMethodNotFound(methodName, target.class.name);
                }
            }
        });
    }

    public static void setOnItemClick(AbsListView v, Object target, String methodName) {
        v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (target.metaClass.respondsTo(target, methodName)) {
                    if (SwissKnife.searchMethod(target, methodName, [int, AdapterView.class])) {
                        target.invokeMethod(methodName, [i, adapterView].toArray());
                    } else if (SwissKnife.searchMethod(target, methodName, [int, AdapterView.class, View.class])) {
                        target.invokeMethod(methodName, [i, adapterView, view].toArray());
                    } else if (SwissKnife.searchMethod(target, methodName, [int])) {
                        target.invokeMethod(methodName, i);
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onItemClick(int position, AdapterView list)\n" +
                                "\tb) public void onItemClick(int position, AdapterView list, View clickedView)\n" +
                                "\tc) public void onItemClick(int position)");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
            }
        });
    }

    public static void setOnItemSelected(AbsListView v, Object target, String methodName, String methodStr) {
        OnItemSelected.Method method = OnItemSelected.Method.valueOf(methodStr);
        v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(OnItemSelected.Method.ITEM_SELECTED == methodStr) {
                    if (target.metaClass.respondsTo(target, methodName)) {
                        if (SwissKnife.searchMethod(target, methodName, [int, AdapterView.class])) {
                            target.invokeMethod(methodName, [i, adapterView].toArray());
                        } else if (SwissKnife.searchMethod(target, methodName, [int, AdapterView.class, View.class])) {
                            target.invokeMethod(methodName, [i, adapterView, view].toArray());
                        } else if (SwissKnife.searchMethod(target, methodName, [int])) {
                            target.invokeMethod(methodName, i);
                        } else {
                            Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                    "\ta) public void onItemSelected(int position, AdapterView list)\n" +
                                    "\tb) public void onItemSelected(int position, AdapterView list, View clickedView)\n" +
                                    "\tc) public void onItemSelected(int position)");
                        }
                    } else {
                        printMethodNotFound(methodName, target.class.name);
                    }
                }
            }

            @Override
            void onNothingSelected(AdapterView<?> adapterView) {
                if(OnItemSelected.Method.NOTHING_SELECTED == methodStr) {
                    if (target.metaClass.respondsTo(target, methodName)) {
                        if (SwissKnife.searchMethod(target, methodName, [AdapterView.class])) {
                            target.invokeMethod(methodName, [adapterView].toArray());
                        } else {
                            Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                    "\ta) public void onNothingSelected(AdapterView list)");
                        }
                    } else {
                        printMethodNotFound(methodName, target.class.name);
                    }
                }
            }
        });
    }

    public static void setOnPageChanged(ViewPager v, Object target, String methodName, String methodStr) {
        OnPageChanged.Method method = OnPageChanged.Method.valueOf(methodStr);
        v.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            void onPageScrolled(int position, float offset, int offsetInPixels) {
                if(method == OnPageChanged.Method.PAGE_SCROLLED) {
                    if (target.metaClass.respondsTo(target, methodName)) {
                        if (SwissKnife.searchMethod(target, methodName, [int])) {
                            target.invokeMethod(methodName, [position].toArray());
                        } else if(SwissKnife.searchMethod(target, methodName, [int, float, int])) {
                            target.invokeMethod(methodName, [position, offset, offsetInPixels].toArray());
                        } else {
                            Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                    "\ta) public void onPageScrolled(int position)\n" +
                                    "\tb) public void onPageScrolled(int position, float offset, int pixelOffset)");
                        }
                    } else {
                        printMethodNotFound(methodName, target.class.name);
                    }
                }
            }

            @Override
            void onPageSelected(int position) {
                if(method == OnPageChanged.Method.PAGE_SELECTED) {
                    if (target.metaClass.respondsTo(target, methodName)) {
                        if (SwissKnife.searchMethod(target, methodName, [int])) {
                            target.invokeMethod(methodName, [position].toArray());
                        } else {
                            Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                    "\ta) public void onPageSelected(int position)");
                        }
                    } else {
                        printMethodNotFound(methodName, target.class.name);
                    }
                }
            }

            @Override
            void onPageScrollStateChanged(int state) {
                if(method == OnPageChanged.Method.PAGE_SCROLL_STATE_CHANGED) {
                    if (target.metaClass.respondsTo(target, methodName)) {
                        if (SwissKnife.searchMethod(target, methodName, [int])) {
                            target.invokeMethod(methodName, [state].toArray());
                        }
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onPageScrollStateChanged(int state)");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
            }
        });
    }




    public static void setOnTextChanged(TextView v, Object target, String methodName, String methodStr) {
        OnTextChanged.Method method = OnTextChanged.Method.valueOf(methodStr);
        v.addTextChangedListener(new TextWatcher() {
            @Override
            void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                if(method == OnTextChanged.Method.BEFORE_TEXT_CHANGED) {
                    if (target.metaClass.respondsTo(target, methodName)) {
                        if (SwissKnife.searchMethod(target, methodName, [CharSequence.class])) {
                            target.invokeMethod(methodName, [charSequence].toArray());
                        } else if (SwissKnife.searchMethod(target, methodName, [TextView.class, CharSequence.class])) {
                            target.invokeMethod(methodName, [v, charSequence].toArray());
                        } else if (SwissKnife.searchMethod(target, methodName, [CharSequence.class, int, int, int])) {
                            target.invokeMethod(methodName, [charSequence, start, count, after].toArray());
                        } else if (SwissKnife.searchMethod(target, methodName, [TextView.class, CharSequence.class, int, int, int])) {
                            target.invokeMethod(methodName, [v, charSequence, start, count, after].toArray());
                        } else {
                            Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                    "\ta) public void beforeTextChanged(CharSequence sequence)\n" +
                                    "\tb) public void beforeTextChanged(TextView textView, CharSequence sequence)\n" +
                                    "\tc) public void beforeTextChanged(CharSequence sequence, int start, int count, int after)\n" +
                                    "\td) public void beforeTextChanged(TextView textView, CharSequence sequence, int start, int count, int after)");
                        }
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
            }

            @Override
            void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(method == OnTextChanged.Method.ON_TEXT_CHANGED) {
                    if (target.metaClass.respondsTo(target, methodName)) {
                        if (SwissKnife.searchMethod(target, methodName, [CharSequence.class])) {
                            target.invokeMethod(methodName, [charSequence].toArray());
                        } else if (SwissKnife.searchMethod(target, methodName, [View.class, CharSequence.class])) {
                            target.invokeMethod(methodName, [v, charSequence].toArray());
                        } else if (SwissKnife.searchMethod(target, methodName, [CharSequence.class, int, int, int])) {
                            target.invokeMethod(methodName, [charSequence, start, before, count].toArray());
                        } else if (SwissKnife.searchMethod(target, methodName, [View.class, CharSequence.class, int, int, int])) {
                            target.invokeMethod(methodName, [v, charSequence, start, before, count].toArray());
                        } else {
                            Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                    "\ta) public void onTextChanged(CharSequence sequence)\n" +
                                    "\tb) public void onTextChanged(TextView textView, CharSequence sequence)\n" +
                                    "\tc) public void onTextChanged(CharSequence sequence, int start, int before, int count)\n" +
                                    "\td) public void onTextChanged(TextView textView, CharSequence sequence, int start, int before, int count)");
                        }
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
            }

            @Override
            void afterTextChanged(Editable editable) {
                if(method == OnTextChanged.Method.ON_TEXT_CHANGED) {
                    if (target.metaClass.respondsTo(target, methodName)) {
                        if (SwissKnife.searchMethod(target, methodName, [Editable.class])) {
                            target.invokeMethod(methodName, [editable].toArray());
                        }
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void afterTextChanged(Editable editable)");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
            }
        });
    }

    public static void setOnChecked(CompoundButton v, Object target, String methodName) {
        v.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (target.metaClass.respondsTo(target, methodName)) {
                    if (SwissKnife.searchMethod(target, methodName, [CompoundButton.class, boolean.class])) {
                        target.invokeMethod(methodName, [compoundButton, b].toArray());
                    } else if(SwissKnife.searchMethod(target, methodName, [boolean.class])) {
                        target.invokeMethod(methodName, b);
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onChecked(CompoundButton button, boolean checked)\n" +
                                "\tb) public void onChecked(boolean checked)");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
            }
        });
    }



    public static void setOnFocusChanged(View v, Object target, String methodName) {
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            void onFocusChange(View view, boolean b) {
                if (target.metaClass.respondsTo(target, methodName)) {
                    if (SwissKnife.searchMethod(target, methodName, [View, boolean.class])) {
                        target.invokeMethod(methodName, [view, b].toArray());
                    } else if (SwissKnife.searchMethod(target, methodName, [boolean.class])) {
                        target.invokeMethod(methodName, b);
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public void onFocusChanged(View view, boolean hasFocus)\n" +
                                "\tb) public void onFocusChanged(boolean hasFocus)");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
            }
        });
    }

    public static void setOnEditorAction(TextView v, Object target, String methodName) {
        v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (target.metaClass.respondsTo(target, methodName)) {
                    if (SwissKnife.searchMethod(target, methodName, [TextView.class, KeyEvent.class])) {
                        return target.invokeMethod(methodName, [textView, keyEvent].toArray());
                    } else if(SwissKnife.searchMethod(target, methodName, [KeyEvent.class])) {
                        return target.invokeMethod(methodName, keyEvent);
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public boolean onEditorAction(TextView textview, KeyEvent event)\n" +
                                "\tb) public boolean onEditorAction(KeyEvent event)");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
                return false;
            }
        });
    }

    public static void setOnLongClick(View v, Object target, String methodName) {
        v.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            boolean onLongClick(View view) {
                if (target.metaClass.respondsTo(target, methodName)) {
                    if(SwissKnife.searchMethod(target, methodName, [View.class])) {
                        return target.invokeMethod(methodName, view);
                    } else if (SwissKnife.searchMethod(target, methodName, null)) {
                        return target.invokeMethod(methodName, null);
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public boolean onLongClick(View view)\n" +
                                "\tb) public boolean onLongClick()");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
                return false
            }
        });
    }

    public static void setOnItemLongClick(AbsListView v, Object target, String methodName) {
        v.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (target.metaClass.respondsTo(target, methodName)) {
                    if (SwissKnife.searchMethod(target, methodName, [int, AdapterView.class])) {
                        return target.invokeMethod(methodName, [i, adapterView].toArray());
                    } else if (SwissKnife.searchMethod(target, methodName, [int, AdapterView.class, View.class])) {
                        return target.invokeMethod(methodName, [i, adapterView, view].toArray());
                    } else if (SwissKnife.searchMethod(target, methodName, [int])) {
                        return target.invokeMethod(methodName, i);
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public boolean onItemLongClick(int position, AdapterView list)\n" +
                                "\tb) public boolean onItemLongClick(int position, AdapterView list, View clickedView)\n" +
                                "\tc) public boolean onItemLongClick(int position)");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
                return false;
            }
        });
    }

    public static void setOnTouch(View v, Object target, String methodName) {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            boolean onTouch(View view, MotionEvent motionEvent) {
                if (target.metaClass.respondsTo(target, methodName)) {
                    if (SwissKnife.searchMethod(target, methodName, [MotionEvent.class])) {
                        return target.invokeMethod(methodName, [motionEvent].toArray());
                    } else if (SwissKnife.searchMethod(target, methodName, [View.class, MotionEvent.class])) {
                        return target.invokeMethod(methodName, [view, motionEvent].toArray());
                    } else {
                        Log.e(TAG, "Could not use annotated method. Method should be like:\n" +
                                "\ta) public boolean onTouch(MotionEvent event)\n" +
                                "\tb) public boolean onTouch(View view, MotionEvent event)");
                    }
                } else {
                    printMethodNotFound(methodName, target.class.name);
                }
                return false;
            }
        })
    }

    public static void runOnBackground(Closure closure) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(new Runnable() {
                @Override
                void run() {
                    closure();
                }
            }).start();
        } else {
            closure();
        }
    }

    public static void runOnUIThread(Closure closure) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            closure();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                void run() {
                    closure();
                }
            });
        }
    }


    public static boolean searchMethod(Object currentObject, String name, List<Class> parameters) {
        for(Method method in currentObject.class.getMethods()) {
            if(name.equals(method.getName())) {
                Class[] parameterClasses = method.getParameterTypes();
                if(parameters.size() != parameterClasses.size()) {
                    return false;
                }
                if(parameters.size() == 0 && parameterClasses.size() == 0) {
                    return true;
                }

                for(i in 0..parameterClasses.size()-1) {
                    if(!AnnotationUtils.isSubtype((Class) parameters[i], (Class) parameterClasses[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static void printMethodNotFound(String methodName, String className) {
        Log.e(TAG, "Could not find method " + methodName + " in "+ className);
    }
}