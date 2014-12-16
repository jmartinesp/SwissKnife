package com.coupledays.app.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coupledays.app.R
import com.coupledays.entity.Apartment
import com.squareup.picasso.Picasso

class ApartmentImagePagerAdapter extends PagerAdapter {

    List<String> images = []
    Context context

    ApartmentImagePagerAdapter(Context context, List<String> images) {
        this.images = images?.collect {
            "${Apartment.restUrl}${it}"
        }
        if (!this.images) {
            this.images = null
        }
        this.context = context
    }

    @Override
    void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View) object)
    }

    @Override
    Object instantiateItem(ViewGroup container, int position) {
        def inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        def view = inflater.inflate(R.layout.apartment_image_pager, container, false)
        def imageView = view.image(R.id.apartmentImageView)
        Picasso.with(context).load(this.images?.getAt(position)).placeholder(R.drawable.ic_img_default).error(R.drawable.ic_img_default).into(imageView)
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
