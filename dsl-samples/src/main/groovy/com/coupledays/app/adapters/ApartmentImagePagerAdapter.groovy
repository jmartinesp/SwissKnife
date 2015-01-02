package com.coupledays.app.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coupledays.app.R
import com.coupledays.app.fragments.ApartmentFragment
import com.coupledays.entity.Apartment
import com.nostra13.universalimageloader.core.ImageLoader
import groovy.transform.CompileStatic

@CompileStatic
class ApartmentImagePagerAdapter extends PagerAdapter {

    List<String> images = []
    Context context

    ApartmentImagePagerAdapter(Context context, List<String> images) {
        this.images = images?.collect {
            "${Apartment.restUrl}${it}".toString()
        }
        if (!this.images) {
            this.images = null
        }
        this.context = context
    }

    @Override
    void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object)
    }

    @Override
    Object instantiateItem(ViewGroup container, int position) {
        def inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        def view = inflater.inflate(R.layout.apartment_image_pager, container, false)
        def imageView = view.image(R.id.apartmentImageView)
        ImageLoader.instance.displayImage(this.images?.getAt(position), imageView, ApartmentFragment.options)
        container.addView(view)
        view
    }

    @Override
    int getCount() {
        if (!this.images) return 0
        this.images.size()
    }

    @Override
    boolean isViewFromObject(View view, Object o) {
        view == o
    }
}
