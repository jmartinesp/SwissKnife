package com.coupledays.app.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coupledays.app.R
import com.coupledays.app.adapters.ApartmentImagePagerAdapter
import com.coupledays.entity.Apartment
import com.nostra13.universalimageloader.core.DisplayImageOptions

class ApartmentFragment extends Fragment {
    Apartment apartment = new Apartment(address: 'SimpleAddress')

    static final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_img_default)
            .showImageForEmptyUri(R.drawable.ic_empty)
            .showImageOnFail(R.drawable.ic_img_default)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        if (arguments) {
            apartment.images = arguments.getStringArrayList('images')
            apartment.address = arguments.getString('address')
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        def view = inflater.inflate(R.layout.fragment_apartment, container, false)
        view.text(R.id.apartmentAddress).setText apartment.address
        def viewPager = (ViewPager) view.view(R.id.apartmentImageViewPager)
        viewPager.adapter = new ApartmentImagePagerAdapter(this.activity, apartment.images)
        viewPager.setCurrentItem(0)
        view
    }
}
