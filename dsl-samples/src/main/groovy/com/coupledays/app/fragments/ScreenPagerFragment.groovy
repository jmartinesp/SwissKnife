package com.coupledays.app.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coupledays.app.R
import com.coupledays.app.adapters.SliderPagerAdapter

class ScreenPagerFragment extends Fragment {
    int fragmentType = 0

    ScreenPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        if (arguments) {
            fragmentType = arguments.getInt('fragmentType')
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        def view = inflater.inflate(R.layout.fragment_screen_pager, container, false)
        def viewPager = (ViewPager) view.view(R.id.pager)
        viewPager.setCurrentItem(0)
        viewPager.adapter = new SliderPagerAdapter(fragmentManager, fragmentType, viewPager)
        view
    }
}
