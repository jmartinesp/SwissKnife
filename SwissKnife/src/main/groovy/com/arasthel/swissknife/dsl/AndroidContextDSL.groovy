package com.arasthel.swissknife.dsl
import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import groovy.transform.CompileStatic

/**
 * DSL methods for Android Context components
 * @author Cédric Champeau
 */
@CompileStatic
public class AndroidContextDSL {

    /**
     * Get notification manager for context
     * @see android.app.NotificationManager
     *
     * @param self
     * @return
     */
    static NotificationManagerCompat getCompatNotificationManager(Context self) {
        NotificationManagerCompat.from(self)
    }

    /**
     * Shows notification
     * @see android.app.NotificationManager
     *
     * @param self
     * @param notificationId
     * @param notification
     */
    static void showNotification(Context self, int notificationId, Notification notification) {
        getCompatNotificationManager(self).notify(notificationId, notification)
    }

    /**
     * Build notification by closure spec
     * @see android.app.NotificationManager
     *
     * @param self
     * @param notificationSpec
     * @return
     */
    static Notification notification(Context self, @DelegatesTo(NotificationCompat.Builder) Closure notificationSpec = null) {
        def builder = new NotificationCompat.Builder(self)
        if (notificationSpec) {
            builder.with(notificationSpec)
        }
        builder.build()
    }

    /**
     * Show notification method
     * @see android.app.Notification
     *
     * @param self
     * @param notificationId
     * @param notificationSpec
     */
    static void showNotification(Context self, int notificationId, @DelegatesTo(NotificationCompat.Builder) Closure notificationSpec) {
        showNotification(self, notificationId, notification(self, notificationSpec))
    }

    /**
     * Create pending intent
     * @see PendingIntent
     *
     * @param self
     * @param requestCode
     * @param intent
     * @param flags
     * @return
     */
    static PendingIntent pendingActivityIntent(Context self, int requestCode, Intent intent, int flags) {
        PendingIntent.getActivity(self, requestCode, intent, flags)
    }

    /**
     * Create notification with bigtext style
     * @see NotificationCompat.BigTextStyle
     *
     * @param self
     * @param styleSpec
     * @return
     */
    static NotificationCompat.BigTextStyle bigTextStyle(Context self, @DelegatesTo(NotificationCompat.BigTextStyle) Closure styleSpec = null) {
        def bigStyle = new NotificationCompat.BigTextStyle()
        if (styleSpec) {
            bigStyle.with(styleSpec)
        }
        bigStyle
    }

    /**
     * Create new intent
     * @see android.content.Intent
     *
     * @param self
     * @param clazz
     * @return
     */
    static Intent intent(Context self, Class<?> clazz) {
        new Intent(self, clazz)
    }

    /**
     * Starts activity
     * @see android.content.Intent
     * @see android.app.Activity
     *
     * @param self
     * @param activity
     * @param intentSpec
     */
    static void startActivity(Context self,
                              Class<? extends Activity> activity,
                              @DelegatesTo(value=Intent, strategy = Closure.DELEGATE_FIRST) Closure intentSpec = null) {
        def intent = new Intent(self, activity)
        if(intentSpec != null){
            def clone = (Closure) intentSpec.clone()
            clone.resolveStrategy = Closure.DELEGATE_FIRST
            clone.delegate = intent
            clone()
        }
        self.startActivity(intent)
    }
}
