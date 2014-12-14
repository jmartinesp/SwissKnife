package com.coupledays.app.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.coupledays.app.R
import com.coupledays.entity.Apartment

class MainFragment extends Fragment {
    ListView apartmentListView

    static final List<Apartment> apartments = new ArrayList<>()

    MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(R.layout.main_fragment, container, false)
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }
}
