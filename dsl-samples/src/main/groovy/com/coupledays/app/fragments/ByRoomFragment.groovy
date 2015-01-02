package com.coupledays.app.fragments

import com.coupledays.entity.Apartment
import groovy.transform.CompileStatic

@CompileStatic
class ByRoomFragment extends DefaultFragment {
    @Override
    List<Apartment> getApartmentList(int offset) {
        (List<Apartment>) Apartment.apartmentByRooms(rooms: arguments.getInt('rooms'), offset: offset * 30)
    }
}
