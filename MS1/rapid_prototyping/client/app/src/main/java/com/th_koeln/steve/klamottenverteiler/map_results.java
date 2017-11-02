package com.th_koeln.steve.klamottenverteiler;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class map_results extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_results);
        // define map fragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // initialize map system + view
        mapFragment.getMapAsync(this);
    }


    //add markers on map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // define + place some Marker
        LatLng dortmund = new LatLng(51.5, 7.45);
        mMap.addMarker(new MarkerOptions().position(dortmund).title("Marker in Dortmund"));
    }
}
