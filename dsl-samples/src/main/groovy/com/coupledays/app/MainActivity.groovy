package com.coupledays.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.widget.Button
import com.android.ast.InjectViews
import com.android.ast.ViewById
import com.android.components.CacheHolder
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@CompileStatic
@InheritConstructors
@InjectViews(R.layout.my_activity)
class MainActivity extends Activity {
    final CacheHolder imagesHolder = new CacheHolder()

    static String userJson = """{"name":"Gomer","phone":"77034445522",
                                  "avatar":"http://www.codejobs.biz/www/lib/files/images/b312953ac30ff5d.png"},
                                 {"name":"Simpson","phone":"78882223334",
                                 "avatar":"http://groovy.codehaus.org/images/groovy-logo-medium.png"},
                                 {"name":"Sara","phone":"770422233344",
                                 "avatar":"https://raw.githubusercontent.com/Arasthel/SwissKnife/master/SwissKnife.png"},"""
    @ViewById(R.id.next_button)
    Button nextButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        def usrJson = ''
        10.times {
            usrJson += userJson
        }
        usrJson = (String) "[${usrJson[0..-2]}]"
        def userList = usrJson.jsonAsList(User)
        userList.asListView(this, R.id.userList, R.layout.user_row_layout) { user ->
            image(R.id.icon).async { image, task ->
                task.after { bitmap ->
                    if (bitmap) {
                        image.imageBitmap = (Bitmap) bitmap
                    } else {
                        image.imageDrawable = resources.getDrawable(R.drawable.ic_heart_active)
                    }
                }
                task.error { e ->
                    image.imageDrawable = resources.getDrawable(R.drawable.ic_heart_active)
                    showToast("Loading image error: ${e?.message}")
                }
                this.imagesHolder.findOrCreate(user.avatar) { user.avatar.asImage() }
            }
            text(R.id.userName).setText user.name
            text(R.id.userTelephone).setText user.phone
        }
        nextButton.onClick {
            startActivity new Intent(applicationContext, MainActivityNew)
        }

        double[] doubles = [1.2, 2.4]

        SparseArray<Parcelable> sparse = new SparseArray<>()
        sparse.append(1, new User())

        Bundle bundle = new Bundle()
        bundle.putFromMap(["boolean"    : true,
                           "sparsearray": sparse,
                           "doubles"    : doubles])

        boolean ok = bundle.getBoolean("boolean")
    }
}