package me.champeau.wearapp
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
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
                        image.imageDrawable = resources.getDrawable(R.drawable.broken_heart)
                    }
                }
                task.error { e ->
                    image.imageDrawable = resources.getDrawable(R.drawable.broken_heart)
                    showToast("Loading image error: ${e?.message}")
                }
                this.imagesHolder.findOrCreate(user.avatar) { user.avatar.asImage() }
            }
            text(R.id.userName).setText user.name
            text(R.id.userTelephone).setText user.phone
        }
        def user = new User(name: 'name', phone: 'phone my')
        def map = user.asDefault { Map map, User usr ->
        }
        nextButton.onClick {
            this.showToast(map.toString())
        }

        double[] doubles = [1.2, 2.4]

        SparseArray<Parcelable> sparse = new SparseArray<>()
        sparse.append(1, new User())

        Bundle bundle = new Bundle()
        bundle.putFromMap(["boolean" : true,
                           "sparsearray" : sparse,
                           "doubles": doubles])

        boolean ok = bundle.getBoolean("boolean")
    }

    @Override
    protected void onStart() {
        super.onStart()
    }

    @Override
    protected void onStop() {
        super.onStop()
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.presentation, menu)
        true
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.itemId
        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
