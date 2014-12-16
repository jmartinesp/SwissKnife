package com.coupledays.app.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import com.coupledays.app.R
import com.coupledays.app.adapters.ApartmentImagePagerAdapter
import com.coupledays.entity.Apartment
import groovy.transform.CompileStatic

@CompileStatic
abstract class DefaultFragment extends Fragment {
    static final int mainFragment = R.layout.main_fragment,
                     aListId = R.id.apartmentListView,
                     apartmentRow = R.layout.apartment_row_layout,
                     apartmentImagePager = R.id.apartmentImageViewPager

    ListView apartmentListView
    ImageView favImageView
    ImageView mapImageView
    List<Apartment> apartments
    Apartment apartment

    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(mainFragment, container, false)
    }

    @Override
    void onViewCreated(View view1, Bundle savedInstanceState) {
        super.onViewCreated(view1, savedInstanceState)
        async { fragment, task ->
            task.error { e ->
                e?.printStackTrace()
                view1.showToast(e?.getMessage())
            }
            task.after {
                this.apartments.asListView(this.getActivity(), aListId, apartmentRow) { Apartment apartment ->
                    def viewPager = (ViewPager) view(apartmentImagePager)
                    def imagesAdapter = new ApartmentImagePagerAdapter(this.getActivity(), apartment.images)
                    viewPager.adapter = imagesAdapter
                    viewPager.currentItem = 0
                }
            }
            this.apartments = getApartmentList()
        }
    }

    abstract List<Apartment> getApartmentList()
}
