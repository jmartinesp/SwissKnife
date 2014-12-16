package com.coupledays.app.fragments

import com.coupledays.entity.Apartment

class MainFragment extends DefaultFragment {
    MainFragment() {
    }

    @Override
    List<Apartment> getApartmentList() {
        (List<Apartment>) Apartment.apartmentList(null)
    }
}
