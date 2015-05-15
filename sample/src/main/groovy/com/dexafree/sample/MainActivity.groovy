package com.dexafree.sample
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.*
import com.arasthel.swissknife.dsl.components.GAsyncTask

import groovy.transform.CompileStatic

@CompileStatic
public class MainActivity extends Activity {

    private Context mContext

    @InjectView(R.id.first_textview)
    TextView firstTextView
    // tag::injectViewWithId[]
    @InjectView(R.id.first_button)
    Button firstButton
    // end::injectViewWithId[]
    @InjectView(R.id.list_view)
    ListView listView
    @InjectView(R.id.written_text)
    TextView writtenTextView

    // tag::methodAwareAnnotation[]
    @OnTextChanged(value = R.id.edit_text, method = OnTextChanged.Method.ON_TEXT_CHANGED)
    @Profile
    public void onTextChanged(CharSequence sequence) {
        writtenTextView.text = sequence
    }
    // end::methodAwareAnnotation[]

    @OnEditorAction(R.id.edit_text)
    @Profile
    public boolean onEditorAction(KeyEvent key) {
        toast 'Editor action received' show()
        true
    }

    @OnClick(R.id.first_button)
    def testEmbeddedParcelable(){
        Team aInstance = new Team()
        Person p1 = new Person("Jing",38)
        aInstance.guys.add(p1)
        Person p2 = new Person("MrX",25)
        aInstance.guys.add(p2)

        Intent intent = new Intent(this,ParcelableReceiveActivity.class)
        intent.putExtra("team",aInstance as android.os.Parcelable)
        startActivity(intent)
    }

/*
    // tag::onClick[]
    @OnClick(R.id.first_button)
    @Profile
    public void clicked() {
        firstButton.text = 'I\'ve been clicked! Click me longer!'
        profileMethod('param1Value', 'param2Value')
    }
    // end::onClick[]

    @OnLongClick(R.id.first_button)
    public boolean longClicked() {
        firstButton.text = 'I\'ve been clicked for a long time!'
        profileMethod('anotherValue1', 'anotherValue2', 5)
        return true
    }
*/


    @OnClick(R.id.second_button)
    public void changeText() {
        firstTextView.text = 'You have pressed the second button!'
    }

    @OnClick(R.id.new_activity)
    @Profile
    public void newActivity() {
        startActivity new Intent(mContext, BackgroundActivity.class)
    }

    @OnItemClick(R.id.list_view)
    @Profile
    public void onItemClick(int position) {
        toast "Pressed item number $position" show()
    }

    @OnItemLongClick(R.id.list_view)
    @Profile
    public boolean onItemLongClick(int position) {
        toast "Long pressed item number $position" show()
        true
    }

    // tag::multipleOnClick[]
    @OnClick([R.id.third_button, R.id.fourth_button])
    public void onClick() {
        toast 'Button three or four has been clicked' show()
    }
    // end::multipleOnClick[]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        mContext = this
        contentView = R.layout.activity_main
        SwissKnife.inject this
        firstTextView.text = 'HELLO'
        def items = generateItems()
        listView.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
    }

    @Profile
    List<String> generateItems() {
        def strings = []
        20.times {
            strings << "Element $it"
        }
        return strings
    }

    @Profile
    def profileMethod(String param1, String param2, int f = 4) {
        async { Activity context, GAsyncTask task ->
            task.after {
                context.toast('Hey! Async task just finished') show()
            }
            task.error { e ->
                context.toast('WTF. Error raised.') show()
            }
            Thread.sleep(10000)
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu)
        true
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true
        }
        super.onOptionsItemSelected(item)
    }
}
