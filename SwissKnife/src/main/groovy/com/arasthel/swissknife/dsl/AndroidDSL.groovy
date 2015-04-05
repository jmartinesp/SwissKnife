package com.arasthel.swissknife.dsl

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.Fragment
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.arasthel.swissknife.dsl.components.Form
import com.arasthel.swissknife.dsl.components.GArrayAdapter
import com.arasthel.swissknife.dsl.components.GAsyncTask
import com.arasthel.swissknife.dsl.components.ObjectPropertyResolver
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

/**
 * DSL methods class
 *
 * @author Eugene Kamenev @eugenekamenev
 * @author Jorge Martín Espinosa
 * @since 0.1
 */
@CompileStatic
class AndroidDSL {

    /**
     * Seng a DEBUG log message with the provided message
     * @param message String
     * @param throwable (Optional) Exception to show on logcat
     * @return
     */
    static void log(anyObject, message, Throwable throwable = null) {
        Log.d('DEBUG', message.toString(), throwable)
    }

    /**
     * Short way of writing 'findViewById' and applying a closure to that view
     * @param context The container View
     * @param id Id of the view we want to find
     * @param closure (Optional) Closure to apply to the found view
     * @return View
     */
    static View view(View context, int id,
                     @DelegatesTo(value = View, strategy = Closure.DELEGATE_FIRST) Closure
                             closure = null) {
        (View) build(context, id, closure)
    }

    /**
     * Short way of writing 'findViewById' and applying a closure to that view
     * @param context The container Activity
     * @param id Id of the view we want to find
     * @param closure (Optional) Closure to apply to the found view
     * @return View
     */
    static View view(Activity context, int id,
                     @DelegatesTo(value = View, strategy = Closure.DELEGATE_FIRST) Closure
                             closure = null) {
        (View) build(context, id, closure)
    }

    /**
     * Short way of writing 'findViewById' and applying a closure to that view
     * @param context The container Fragment
     * @param id Id of the view we want to find
     * @param closure (Optional) Closure to apply to the found view
     * @return View
     */
    static View view(Fragment context, int id,
                     @DelegatesTo(value = View, strategy = Closure.DELEGATE_FIRST) Closure
                             closure = null) {
        (View) build(context, id, closure)
    }

    /**
     * Find and cast a View to {@link EditText} and optionally apply a closure to it
     * @param context The container View
     * @param id Id of the EditText we want to find
     * @param closure (Optional) Closure to apply to the found view
     * @return EditText
     */
    static <T extends EditText> T editText(View context, int id,
                                           @DelegatesTo(value = T,
                                                   strategy = Closure.DELEGATE_FIRST) Closure
                                                   closure = null) {
        (T) build(context, id, closure)
    }

    /**
     * Find and cast a View to {@link EditText} and optionally apply a closure to it
     * @param context The container Activity
     * @param id Id of the EditText we want to find
     * @param closure (Optional) Closure to apply to the found view
     * @return EditText
     */
    static <T extends EditText> T editText(Activity context, int id,
                                           @DelegatesTo(value = T,
                                                   strategy = Closure.DELEGATE_FIRST) Closure
                                                   closure = null) {
        (T) build(context, id, closure)
    }

    /**
     * Find and cast a View to {@link EditText} and optionally apply a closure to it
     * @param context The container Fragment
     * @param id Id of the EditText we want to find
     * @param closure (Optional) Closure to apply to the found view
     * @return EditText
     */
    static <T extends EditText> T editText(Fragment context, int id,
                                           @DelegatesTo(value = T,
                                                   strategy = Closure.DELEGATE_FIRST) Closure
                                                   closure = null) {
        (T) build(context, id, closure)
    }

    /**
     * Add a String tag to a View
     * @param view The View
     * @param propertyName The String tag
     * @return View
     */
    static <T extends View> T attach(T view, String propertyName) {
        view?.tag = propertyName
        view
    }

    /**
     * Add an object's property as a tag to a View using a closure
     * @param view The View
     * @param object The object that holds the property
     * @param closure Closure to apply
     * @return View
     */
    static <I extends Serializable, T extends View, S> T attach(T view, S object,
                                                                @ClosureParams(value =
                                                                        FromString,
                                                                        options = 'S') Closure<I>
                                                                        closure) {
        internalAttach(view, object.class, closure)
    }

    private static <I extends Serializable, T extends View, S> T internalAttach(T view, Class<S> clazz,
                                                                @ClosureParams(value =
                                                                        FromString,
                                                                        options = 'S') Closure<I>
                                                                        closure) {
        view?.tag = attachViewTag(closure.dehydrate())
        view
    }

    private static String attachViewTag(Closure closure) {
        def resolver = new ObjectPropertyResolver()
        closure?.call(resolver)
        resolver.notation
    }

    /**
     * Create a {@link Form} using a closure
     * @param context The container View
     * @param id The id of the form-holding view
     * @param object The object whose properties will be used on the closure
     * @param closure Closure with form configuration and callbacks
     * @return Form
     */
    static <S, T extends Form<S>> T form(View context, int id, S object,
                                         @ClosureParams(value = FromString, options = ['T', 'T,S']) @DelegatesTo(value = T,
                                                 strategy = Closure.DELEGATE_FIRST) Closure
                                                 closure) {
        internalForm(context, id, object, closure)
    }

    /**
     * Create a {@link Form} using a closure
     * @param context The container Fragment
     * @param id The id of the form-holding view
     * @param object The object whose properties will be used on the closure
     * @param closure Closure with form configuration and callbacks
     * @return Form
     */
    static <S, T extends Form<S>> T form(Fragment context, int id, S object,
                                         @ClosureParams(value = FromString, options = ['T', 'T,S']) @DelegatesTo(value = T,
                                                 strategy = Closure.DELEGATE_FIRST) Closure
                                                 closure) {
        internalForm(context, id, object, closure)
    }

    /**
     * Create a {@link Form} using a closure
     * @param context The container Activity
     * @param id The id of the form-holding view
     * @param object The object whose properties will be used on the closure
     * @param closure Closure with form configuration and callbacks
     * @return Form
     */
    static <S, T extends Form<S>> T form(Activity context, int id, S object,
                                         @ClosureParams(value = FromString, options = ['T', 'T,S']) @DelegatesTo(value = T,
                                                 strategy = Closure.DELEGATE_FIRST) Closure
                                                 closure) {
        internalForm(context, id, object, closure)
    }

    private static <S, T extends Form<S>> T internalForm (context, int id, S object,
                                                         @ClosureParams(value = FromString, options = ['T', 'T,S']) @DelegatesTo(value = T,
                                                                 strategy = Closure.DELEGATE_FIRST) Closure
                                                                 closure) {
        def form = (T) build(context, id)
        form.build(object, closure)
        form
    }

    /**
     * Find a component in a {@link SparseArray} View tag
     * @param view The view which holds the tag
     * @param id The id of the view we want to retrieve
     * @return View
     */
    static <T extends View> T findComponent(View view, int id) {
        def viewHolder = (SparseArray<View>) view.tag
        if (!viewHolder) {
            viewHolder = new SparseArray<View>();
            view.tag = viewHolder
        }
        View res = viewHolder.get(id);
        if (!res) {
            res = view.findViewById(id)
            viewHolder.put(id, res)
        }
        return (T) res;
    }

    /**
     * Show android {@link Toast} message
     *
     * @since 0.1
     * @param view A View object
     * @param text
     */
    static void showToast(View view, CharSequence text) {
        toast(view, text).show()
    }

    /**
     * Create android {@link Toast} message with short length
     *
     * @since 0.1
     * @param context A Context object
     * @param text
     * @return {@link Toast}
     */
    static Toast toast(Context context, CharSequence text) {
        internalToast(context, text)
    }

    /**
     * Create android {@link Toast} message with short length
     *
     * @since 0.1
     * @param view A View Object
     * @param text
     * @return {@link Toast}
     */
    static Toast toast(View view, CharSequence text) {
        internalToast(view, text)
    }

    private static Toast internalToast(context, CharSequence text) {
        Context ctx = null
        if (context instanceof Context) {
            ctx = (Context) context
        }
        else if (context instanceof View) {
            ctx = ((View) context).context
        }
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT)
    }

    /**
     * Get child views of a ViewGroup
     *
     * @since 0.1
     * @param view A ViewGroup Object
     * @param text
     * @return {@link Toast}
     */
    static <T extends ViewGroup> List<View> getChildren(T view, Boolean nested = false) {
        List<View> views = []
        view.childCount.times { int child ->
            def v = view.getChildAt(child)
            views << v
            views += nested && v instanceof ViewGroup ? getChildren(v as ViewGroup) : [v]
        }
        views
    }

    /**
     * Find {@link Button} by R.id and apply closure on it
     *
     * @since 0.1
     * @param view
     * @param id
     * @param closure
     * @return
     */
    static <T extends Button> T button(View view, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        internalButton(view, id, closure)
    }

    /**
     * Find {@link Button} by R.id and apply closure on it
     *
     * @since 0.1
     * @param fragment
     * @param id
     * @param closure
     * @return
     */
    static <T extends Button> T button(Fragment fragment, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        internalButton(fragment, id, closure)
    }

    /**
     * Find {@link Button} by R.id and apply closure on it
     *
     * @since 0.1
     * @param activity
     * @param id
     * @param closure
     * @return
     */
    static <T extends Button> T button(Activity activity, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        internalButton(activity, id, closure)
    }

    static <T extends Button> T internalButton(context, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        (T) build(context, id, closure)
    }

    /**
     * Download image from a String url and convert it to {@link Bitmap}
     * @param url
     * @return
     */
    static Bitmap asImage(String url) {
        BitmapFactory.decodeStream(url.toURL().openStream())
    }

    /**
     * Find {@link ImageView} by R.id and apply closure on it
     *
     * @param view
     * @param id
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends ImageView> T image(View view, int id,
                                         @DelegatesTo(value = T,
                                                 strategy = Closure.DELEGATE_FIRST)
                                         @ClosureParams(value = FromString,
                                                 options = 'T') Closure closure = null) {
        internalImage(view, id, closure)
    }

    /**
     * Find {@link ImageView} by R.id and apply closure on it
     *
     * @param fragment
     * @param id
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends ImageView> T image(Fragment fragment, int id,
                                         @DelegatesTo(value = T,
                                                 strategy = Closure.DELEGATE_FIRST)
                                         @ClosureParams(value = FromString,
                                                 options = 'T') Closure closure = null) {
        internalImage(fragment, id, closure)
    }

    /**
     * Find {@link ImageView} by R.id and apply closure on it
     *
     * @param activity
     * @param id
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends ImageView> T image(Activity activity, int id,
                                         @DelegatesTo(value = T,
                                                 strategy = Closure.DELEGATE_FIRST)
                                         @ClosureParams(value = FromString,
                                                 options = 'T') Closure closure = null) {
        internalImage(activity, id, closure)
    }

    static <T extends ImageView> T internalImage(context, int id,
                                         @DelegatesTo(value = T,
                                                 strategy = Closure.DELEGATE_FIRST)
                                         @ClosureParams(value = FromString,
                                                 options = 'T') Closure closure = null) {
        (T) build(context, id, closure)
    }

    /**
     * Start simple {@link GAsyncTask}
     *
     * @param clazz
     * @param closure
     * @since 0.1
     * @return
     */
    static <T, S> S async(S context,
                          @ClosureParams(value = FromString, options = ['S', 'S,com.arasthel.swissknife.dsl.components.GAsyncTask'])
                                  Closure<T> task) {
        new GAsyncTask<T>(task).execute(context)
        context
    }

    /**
     * Create simple ListView with default {@link GArrayAdapter}
     *
     * @since 0.1
     * @param iterable
     * @param context
     * @param id
     * @param rowLayoutId
     * @param closure
     * @return
     */
    static <T extends ListView, S> T asListView(Iterable<S> iterable, context, int id,
                                                int rowLayoutId,
                                                @DelegatesTo(value = View,
                                                        strategy = Closure.DELEGATE_FIRST)
                                                @ClosureParams(value = FromString,
                                                        options = ['S', 'S,android.view.View',
                                                                'S,android.view.View,java.lang.Integer'])
                                                        Closure closure = null) {
        (T) internalAsListViewWithoutAdapter(context, id, rowLayoutId, iterable, closure)
    }

    /**
     * Create simple {@link ListView} from list with {@link GArrayAdapter}
     *
     * @since 0.1
     * @param view
     * @param id
     * @param closure
     * @return
     */
    static <T extends ListView, A extends ListAdapter> T asListView(View view, int id,
                                                                    A listAdapter) {
        internalAsListViewWithAdapter(view, id, listAdapter)
    }

    /**
     * Create simple {@link ListView} from list with {@link GArrayAdapter}
     *
     * @since 0.1
     * @param fragment
     * @param id
     * @param closure
     * @return
     */
    static <T extends ListView, A extends ListAdapter> T asListView(Fragment fragment, int id,
                                                                    A listAdapter) {
        internalAsListViewWithAdapter(fragment, id, listAdapter)
    }

    /**
     * Create simple {@link ListView} from list with {@link GArrayAdapter}
     *
     * @since 0.1
     * @param activity
     * @param id
     * @param closure
     * @return
     */
    static <T extends ListView, A extends ListAdapter> T asListView(Activity activity, int id,
                                                                    A listAdapter) {
        internalAsListViewWithAdapter(activity, id, listAdapter)
    }

    private static <T extends ListView, A extends ListAdapter> T internalAsListViewWithAdapter(context, int id,
                                                                    A listAdapter) {
        def listView = (T) build(context, id)
        listView.setAdapter(listAdapter)
        listView
    }

    /**
     * Create simple {@link ListView} from list with {@link GArrayAdapter}
     *
     * @since 0.1
     * @param view
     * @param id
     * @param rowLayoutId
     * @param items
     * @param closure
     * @return
     */
    static <T extends ListView, S> T asListView(View view, int id, int rowLayoutId, Iterable<S> items,
                                                @DelegatesTo(value = View,
                                                        strategy = Closure.DELEGATE_FIRST)
                                                @ClosureParams(value = FromString,
                                                        options = ['S', 'S,android.view.View',
                                                                'S,android.view.View,java.lang.Integer'])
                                                        Closure closure = null) {
        internalAsListViewWithoutAdapter(view, id, rowLayoutId, items, closure)
    }

    /**
     * Create simple {@link ListView} from list with {@link GArrayAdapter}
     *
     * @since 0.1
     * @param fragment
     * @param id
     * @param rowLayoutId
     * @param items
     * @param closure
     * @return
     */
    static <T extends ListView, S> T asListView(Fragment fragment, int id, int rowLayoutId, Iterable<S> items,
                                                @DelegatesTo(value = View,
                                                        strategy = Closure.DELEGATE_FIRST)
                                                @ClosureParams(value = FromString,
                                                        options = ['S', 'S,android.view.View',
                                                                'S,android.view.View,java.lang.Integer'])
                                                        Closure closure = null) {
        internalAsListViewWithoutAdapter(fragment, id, rowLayoutId, items, closure)
    }

    /**
     * Create simple {@link ListView} from list with {@link GArrayAdapter}
     *
     * @since 0.1
     * @param activity
     * @param id
     * @param rowLayoutId
     * @param items
     * @param closure
     * @return
     */
    static <T extends ListView, S> T asListView(Activity activity, int id, int rowLayoutId, Iterable<S> items,
                                                @DelegatesTo(value = View,
                                                        strategy = Closure.DELEGATE_FIRST)
                                                @ClosureParams(value = FromString,
                                                        options = ['S', 'S,android.view.View',
                                                                'S,android.view.View,java.lang.Integer'])
                                                        Closure closure = null) {
        internalAsListViewWithoutAdapter(activity, id, rowLayoutId, items, closure)
    }

    private static <T extends ListView, S> T internalAsListViewWithoutAdapter(context, int id, int rowLayoutId, Iterable<S> items,
                                                @DelegatesTo(value = View,
                                                        strategy = Closure.DELEGATE_FIRST)
                                                @ClosureParams(value = FromString,
                                                        options = ['S', 'S,android.view.View',
                                                                'S,android.view.View,java.lang.Integer'])
                                                        Closure closure = null) {
        def listView = (T) build(context, id)
        onItem(listView, rowLayoutId, items, closure)
        listView
    }

    /**
     * Creating {@link GArrayAdapter} and apply onItem closure on ListView
     *
     * @since 0.1
     * @param listView
     * @param rowLayoutId
     * @param items
     * @param closure
     * @return
     */
    static <T extends ListView, S> T onItem(T listView, int rowLayoutId, Iterable<S> items,
                                            @DelegatesTo(value = View,
                                                    strategy = Closure.DELEGATE_FIRST)
                                            @ClosureParams(value = FromString, options = ['S',
                                                    'S,android.view.View', 'S,android.view.View,java.lang.Integer']) Closure closure) {
        listView.adapter = new GArrayAdapter(listView.context, rowLayoutId, items.toList(), closure)
        listView
    }

    /**
     * Find {@link TextView} by R.id and apply closure on it
     *
     * @since 0.1
     * @param view
     * @param id
     * @param closure
     * @return
     */
    static <T extends TextView> T text(View view, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        (T) internalText(view, id, closure)
    }

    /**
     * Find {@link TextView} by R.id and apply closure on it
     *
     * @since 0.1
     * @param fragment
     * @param id
     * @param closure
     * @return
     */
    static <T extends TextView> T text(Fragment fragment, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        (T) internalText(fragment, id, closure)
    }

    /**
     * Find {@link TextView} by R.id and apply closure on it
     *
     * @since 0.1
     * @param activity
     * @param id
     * @param closure
     * @return
     */
    static <T extends TextView> T text(Activity activity, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        (T) internalText(activity, id, closure)
    }

    /**
     * Find {@link TextView} by R.id and apply closure on it
     *
     * @since 0.1
     * @param context
     * @param id
     * @param closure
     * @return
     */
    private static <T extends TextView> T internalText(context, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        (T) build(context, id, closure)
    }

    /**
     * Hide {@link View} component
     *
     * @since 0.1
     * @param view
     */
    static <T extends View> void hide(T view) {
        visible view, false
    }

    /**
     * Show {@link View} component
     *
     * @since 0.1
     * @param view
     */
    static <T extends View> void show(T view) {
        visible view, true
    }

    /**
     * Apply visibility on {@link View} component
     *
     * @param view
     * @param visible
     * @since 0.1
     * @return
     */
    static <T extends View> T visible(T view, boolean visible) {
        view.setVisibility visible ? View.VISIBLE : View.GONE
        view
    }

    /**
     * Apply visibility closure on {@link View} component
     *
     * @param view
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends View> T visible(T view,
                                      @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                              Closure<Boolean> closure) {
        if (closure.call()) {
            visible(view, true)
        }
        else {
            visible(view, false)
        }
        view
    }

    /**
     * Component build method
     *
     * @comment cant make it {@link CompileStatic} because of context.findViewById dynamic nature
     * @param context
     * @param id
     * @param closure
     * @since 0.1
     * @return
     */
    private static <T extends View> Object build(context, int id, Closure closure = null) {
        def object = null
        if (context instanceof View) {
            if (context.tag instanceof SparseArray<T>) {
                object = findComponent(context, id)
            }
        } else if (context instanceof Fragment) {
            object = context.view.findViewById(id)
        } else if (context instanceof Activity) {
            object = context.findViewById(id)
        }
        def clone = closure?.rehydrate(object, closure?.owner, closure?.thisObject)
        clone?.resolveStrategy = Closure.DELEGATE_FIRST
        clone?.call(object)
        object
    }

    /**
     * Returns the root view of the {@link Activity}
     *
     * @param activity
     * @return View
     */
    static View getRootView(Activity activity) {
        def root = activity.getWindow().getDecorView().findViewById(android.R.id.content) as ViewGroup
        root.getChildAt(0)
    }
}
