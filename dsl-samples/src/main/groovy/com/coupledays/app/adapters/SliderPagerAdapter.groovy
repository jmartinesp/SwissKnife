package com.coupledays.app.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import com.coupledays.app.fragments.ApartmentFragment
import com.coupledays.app.fragments.ByRoomFragment
import com.coupledays.app.fragments.MainFragment
import com.coupledays.entity.Apartment

class SliderPagerAdapter extends FragmentPagerAdapter {
    final int count = 3
    final int fragmentType
    final ViewPager viewPager
    Apartment apartment

    SliderPagerAdapter(FragmentManager fm, int fragmentType, ViewPager pager) {
        super(fm)
        this.fragmentType = fragmentType
        this.viewPager = pager
    }

    @Override
    Fragment getItem(int position) {
        Fragment fragment
        Log.i('SliderPager', "position is: $position")
        switch (position) {
            case 0: fragment = fragmentByType(fragmentType)
                break;
            case 1: fragment = new ApartmentFragment()
                if (this.apartment) {
                    fragment.arguments = [images: this.apartment.images, address: this.apartment.address].asBundle()
                }
                break;
        }
        return fragment
    }


    static Fragment fragmentByType(int type) {
        Fragment fragment
        switch (type) {
            case 0:
                fragment = new MainFragment()
                break;
            default:
                fragment = new ByRoomFragment()
                break;
        }
        if (fragment) {
            fragment.setArguments([rooms: type].asBundle())
        } else {
            Log.e("ERROR", "Error. Fragment is not created");
        }
        fragment
    }
}