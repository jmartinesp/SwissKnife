package com.dexafree.sample

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.arasthel.swissknife.annotations.OnClick;

/**
 * Created by Arasthel on 21/6/15.
 */

import groovy.transform.CompileStatic;

@CompileStatic
public class BaseActivity extends AppCompatActivity {

    @InjectView(R.id.first_textview)
    TextView firstTextView

    @OnClick(R.id.second_button)
    public void changeText() {
        firstTextView.text = 'You have pressed the second button!'
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SwissKnife.inject(this)

        firstTextView.text = "this thing was injected from strings.xml"
    }
}