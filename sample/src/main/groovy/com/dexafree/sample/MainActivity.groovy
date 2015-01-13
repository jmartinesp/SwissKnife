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
import groovy.transform.CompileStatic

@CompileStatic
public class MainActivity extends Activity {

    private Context mContext;

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
    public void onTextChanged(CharSequence sequence) {
        writtenTextView.setText(sequence)
    }
    // end::methodAwareAnnotation[]

    @OnEditorAction(R.id.edit_text)
    public boolean onEditorAction(KeyEvent key) {
        Toast.makeText(mContext, "Editor action received", Toast.LENGTH_SHORT).show()
        true
    }

    // tag::onClick[]
    @OnClick(R.id.first_button)
    public void clicked() {
        firstButton.setText("I've been clicked! Click me longer!")
    }
    // end::onClick[]

    @OnLongClick(R.id.first_button)
    public boolean longClicked() {
        firstButton.setText("I've been clicked for a long time!")
        return true
    }

    @OnClick(R.id.second_button)
    public void changeText() {
        firstTextView.setText("You have pressed the second button!")
    }

    @OnClick(R.id.new_activity)
    public void newActivity() {
        startActivity(new Intent(mContext, BackgroundActivity.class))
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(int position) {
        Toast.makeText(mContext, "Pressed item number $position", Toast.LENGTH_SHORT).show()
    }

    @OnItemLongClick(R.id.list_view)
    public boolean onItemLongClick(int position) {
        Toast.makeText(mContext, "Long pressed item number $position", Toast.LENGTH_SHORT).show()
        true
    }

    // tag::multipleOnClick[]
    @OnClick([R.id.third_button, R.id.fourth_button])
    public void onClick() {
        Toast.makeText(
            this, "Button three or four has been clicked", Toast.LENGTH_SHORT).show()
    }
    // end::multipleOnClick[]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        mContext = this;
        setContentView(R.layout.activity_main)

        SwissKnife.inject(this)
        firstTextView.setText("HELLO")

        def items = generateItems()
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items))
    }

    List<String> generateItems() {
        def strings = []
        20.times {
            strings << "Element $it"
        }

        strings
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu)
        true
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId()
        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
