package com.dexafree.sample

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import com.arasthel.swissknife.SwissKnife
import com.arasthel.swissknife.annotations.InjectView
import com.arasthel.swissknife.annotations.OnBackground
import com.arasthel.swissknife.annotations.OnClick
import com.arasthel.swissknife.annotations.OnUIThread
import com.arasthel.swissknife.annotations.SaveInstance
import groovy.transform.CompileStatic

@CompileStatic
public class BackgroundActivity extends Activity {

    private Context mContext;

    @SaveInstance
    public Person[] myPersons

    @SaveInstance
    public Person aPerson

    @SaveInstance
    public ArrayList<Person> aParcelableList

    @SaveInstance
    public String myString

    @SaveInstance
    public int myInt


    @InjectView(R.id.edit_text)
    @SaveInstance
    public EditText textView;


    @InjectView(R.id.image_view) ImageView image

    @OnClick(R.id.load_button)
    public void onClick() {
        startLoading()
    }

    @OnBackground()
    public void startLoading() {
        String url = "https://camo.githubusercontent.com/216b3510229c2a6a77ccef60f258ac760a86fc79/687474703a2f2f692e696d6775722e636f6d2f4c53396f4859562e706e67";
        Bitmap imageBitmap;

        InputStream inputStream = new URL(url).openStream()
        imageBitmap = BitmapFactory.decodeStream(inputStream)

        setImage(imageBitmap)
    }

    @OnUIThread()
    public void setImage(Bitmap bitmap){
        image.setImageBitmap(bitmap)
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        mContext = this;
        setContentView(R.layout.activity_background)

        SwissKnife.inject(this)
        SwissKnife.restoreState(this, savedInstanceState)

        if(savedInstanceState == null){
            Log.d("SIS", "WAS NULL")

            myPersons = new Person[1]
            aParcelableList = new ArrayList<Person>()
            aPerson = new Person("MyName", 55)
            myInt = 150
            myString = "I am a String"

            myPersons[0] = aPerson
            aParcelableList.add(aPerson)

        }


        assert myPersons[0].name== "MyName"
        assert myPersons[0].age == 55

        assert aPerson.name == "MyName"
        assert aPerson.age == 55

        assert aParcelableList.get(0).name == "MyName"
        assert aParcelableList.get(0).age == 55

        assert myInt == 150
        assert myString == "I am a String"
    }


    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState)
    }




}