package com.coupledays.app

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.android.ast.InjectViews
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import groovy.transform.CompileStatic

@CompileStatic
@InjectViews(R.layout.fragment_map)
class ShowOnMapActivity extends FragmentActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle)
        def fragment = ((SupportMapFragment) supportFragmentManager.findFragmentById(R.id.map))
        fragment.getMapAsync(this)
    }

    @Override
    void onMapReady(GoogleMap googleMap) {
        def b = intent.extras
        def location = coordinateFromString(b.getString('lat'), b.getString('lon'))
        googleMap.setMyLocationEnabled(true)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13))
        googleMap.addMarker(new MarkerOptions().title(b.getInt('price').toString()).snippet(b.getString(b.getString('phone'))).position(location))
    }

    @Override
    protected void onResume() {
        super.onResume()
    }

    static LatLng coordinateFromString(String lat, String lon) {
        new LatLng(lat.toBigDecimal().setScale(16).doubleValue(), lon.toBigDecimal().setScale(16).doubleValue())
    }
}