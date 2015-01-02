package com.coupledays.app.utils

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ListView
import com.coupledays.app.adapters.SliderPagerAdapter
import com.coupledays.entity.Apartment

public class SwipeDetector implements View.OnTouchListener {
    static String logTag = 'SWIPE'

    private static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;
    private ListView listView
    private SliderPagerAdapter pagerAdapter

    SwipeDetector(ListView listView, SliderPagerAdapter pagerAdapter) {
        this.listView = listView
        this.pagerAdapter = pagerAdapter
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        downX = event.getX()
        downY = event.getY()
        def item = (Apartment) listView.getItemAtPosition(listView.pointToPosition(Math.round(downX), Math.round(downY)))
        this.pagerAdapter.setApartment(item)
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                Log.i('SWIPE', item?.id?.toString())
                Log.i(logTag, 'Another swipe detected')
                return false;
            case MotionEvent.ACTION_UP:
                upX = event.getX();
                upY = event.getY();
                float deltaX = downX - upX;
                float deltaY = downY - upY;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if (deltaX < 0) {
                        Log.i(logTag, "Swipe Left to Right");
                        return false;
                    }
                    if (deltaX > 0) {
                        Log.i('SWIPE', item?.id?.toString())
                        Log.i(logTag, 'Right To left swipe')
                        return false;
                    }
                } else if (Math.abs(deltaY) > MIN_DISTANCE) {
                    if (deltaY < 0) {
                        Log.i(logTag, "Swipe Top to Bottom");
                        return false;
                    }
                    if (deltaY > 0) {
                        Log.i(logTag, "Swipe Bottom to Top");
                        return false;
                    }
                }
                return false;
        }
        return false;
    }
}