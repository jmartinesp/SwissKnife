package me.champeau.speakertime
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
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
                                  "avatar":"http://localhost/2.jpg"},
                                 {"name":"Simpson","phone":"78882223334",
                                 "avatar":"http://localhost/1.jpg"},
                                 {"name":"Sara","phone":"770422233344",
                                 "avatar":"http://localhost/no-image-"},"""
    @ViewById(R.id.next_button)
    Button nextButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        def usrJson = ''
        10.times {
            usrJson += userJson
        }

        double[] doubles = new double[3]

        SparseArray<Parcelable> strings = new SparseArray<>()
        strings.append(1, new User())

        Map args = ["adios" : false, "hola" : strings, "doubles": doubles]

        long inicio = System.currentTimeMillis()
        Bundle bundle = Bundle.fromMap(args)
        long end = System.currentTimeMillis()

        Log.d("DOUBLES", "${bundle.getSparseParcelableArray("hola")}")

        Log.d("TIME", "${end - inicio}ms")
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
