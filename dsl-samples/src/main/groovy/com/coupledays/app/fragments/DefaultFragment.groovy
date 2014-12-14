package com.coupledays.app.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import com.coupledays.app.R
import com.coupledays.entity.Apartment
import groovy.transform.CompileStatic

@CompileStatic
abstract class DefaultFragment extends Fragment {

    ListView apartmentListView
    ImageView favImageView
    ImageView mapImageView
    List<Apartment> apartments

    DefaultFragment() {

    }

    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(R.layout.main_fragment, container, false)
    }

    @Override
    void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)
        async { fragment, task ->
            task.error { e ->
                view.showToast(e.getMessage())
            }
            task.after {
                Log.i('INFO', this.apartments.toString())
            }
            this.apartments = Apartment.getApartmentList()
        }

    }
}
