package com.arasthel.swissknife.dsl

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
import groovy.transform.TypeCheckingMode
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

/**
 * DSL methods class
 *
 * @author Eugene Kamenev @eugenekamenev
 * @since 0.1
 */
@CompileStatic
class AndroidDSL {

    static void log(anyObject, message, Throwable throwable = null) {
        Log.d('DEBUG', message.toString(), throwable)
    }

    static View view(context, int id,
                     @DelegatesTo(value = View, strategy = Closure.DELEGATE_FIRST) Closure
                             closure = null) {
        (View) build(context, id, closure)
    }

    static <T extends EditText> T editText(context, int id,
                                           @DelegatesTo(value = T,
                                                   strategy = Closure.DELEGATE_FIRST) Closure
                                                   closure = null) {
        (T) build(context, id, closure)
    }

    static <T extends View> T attach(T view, String propertyName) {
        view?.tag = propertyName
        view
    }

    static <I extends Serializable, T extends View, S> T attach(T view, S object,
                                                                @ClosureParams(value =
                                                                        FromString,
                                                                        options = 'S') Closure<I>
                                                                        closure) {
        attach(view, object.class, closure)
    }

    static <I extends Serializable, T extends View, S> T attach(T view, Class<S> clazz,
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

    static <S, T extends Form<S>> T form(context, int id, S object,
                                         @ClosureParams(value = FromString, options = ['T', 'T,S']) @DelegatesTo(value = T,
                                                 strategy = Closure.DELEGATE_FIRST) Closure
                                                 closure) {
        def form = (T) build(context, id)
        form.build(object, closure)
        form
    }

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
     * @param context
     * @param text
     */
    static void showToast(context, CharSequence text) {
        toast(context, text).show()
    }

    /**
     * Create android {@link Toast} message
     *
     * @since 0.1
     * @param context
     * @param text
     * @return
     */
    static Toast toast(context, CharSequence text) {
        Context ctx = null
        if (context instanceof Context) {
            ctx = (Context) context
        }
        else if (context instanceof View) {
            ctx = ((View) context).context
        }
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT)
    }

    static <T extends View> List<View> getChildren(T view, Boolean nested = false) {
        if (!(view instanceof ViewGroup)) {
            return [view]
        }
        def viewGroup = (ViewGroup) view
        List<View> views = []
        viewGroup.childCount.times { int child ->
            def v = viewGroup.getChildAt(child)
            views << v
            views += nested ? getChildren(v) : [v]
        }
        views
    }

    /**
     * Find {@link Button} by R.id and apply closure on it
     *
     * @since 0.1
     * @param context
     * @param id
     * @param closure
     * @return
     */
    static <T extends Button> T button(context, int id,
                                       @DelegatesTo(value = T, strategy = Closure.DELEGATE_FIRST)
                                       @ClosureParams(value = FromString,
                                               options = 'T') Closure closure = null) {
        (T) build(context, id, closure)
    }

    /**
     * Url String as {@link Bitmap}
     * @param url
     * @return
     */
    static Bitmap asImage(String url) {
        BitmapFactory.decodeStream(url.toURL().openStream())
    }

    /**
     * Find {@link ImageView} by R.id and apply closure on it
     *
     * @param context
     * @param id
     * @param closure
     * @since 0.1
     * @return
     */
    static <T extends ImageView> T image(context, int id,
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
        asListView(context, id, rowLayoutId, iterable, closure)
    }

    /**
     * Create simple {@link ListView} from list with {@link GArrayAdapter}
     *
     * @since 0.1
     * @param context
     * @param id
     * @param rowLayoutId
     * @param items
     * @param closure
     * @return
     */
    static <T extends ListView, A extends ListAdapter> T asListView(context, int id,
                                                                    A listAdapter) {
        def listView = (T) build(context, id)
        listView.setAdapter(listAdapter)
        listView
    }

    /**
     * Create simple {@link ListView} from list with {@link GArrayAdapter}
     *
     * @since 0.1
     * @param context
     * @param id
     * @param rowLayoutId
     * @param items
     * @param closure
     * @return
     */
    static <T extends ListView, S> T asListView(context, int id, int rowLayoutId, Iterable<S> items,
                                                @DelegatesTo(value = View,
                                                        strategy = Closure.DELEGATE_FIRST)
                                                @ClosureParams(value = FromString,
                                                        options = ['S', 'S,android.view.View',
                                                                'S,android.view.View,java.lang.Integer'])
                                                        Closure closure = null) {
        def listView = (ListView) build(context, id)
        onItem(listView, rowLayoutId, items, closure)
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
     * @param context
     * @param id
     * @param closure
     * @return
     */
    static <T extends TextView> T text(context, int id,
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
    @CompileStatic(TypeCheckingMode.SKIP)
    private static <T extends View> Object build(context, int id, Closure closure = null) {
        def object = null
        if (context instanceof View) {
            if (context.tag instanceof SparseArray<T>) {
                object = findComponent(context, id)
            }
        }
        if (context instanceof Fragment) {
            object = context.view.findViewById(id)
        }
        object = object ?: context?.findViewById(id)
        def clone = closure?.rehydrate(object, closure?.owner, closure?.thisObject)
        clone?.resolveStrategy = Closure.DELEGATE_FIRST
        clone?.call(object)
        object
    }
}
