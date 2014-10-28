package com.dexafree.sample

import android.app.Activity
import android.widget.Button
import android.widget.TextView
import com.arasthel.swissknife.annotations.InjectView;/**
 * Created by Arasthel on 27/10/14.
 */

import groovy.transform.CompileStatic;

@CompileStatic
public class BaseActivity extends Activity {

    @InjectView(R.id.written_text)
    TextView writtenTextView
}
