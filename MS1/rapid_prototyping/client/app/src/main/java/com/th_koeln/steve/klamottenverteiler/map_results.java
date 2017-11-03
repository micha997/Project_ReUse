package com.th_koeln.steve.klamottenverteiler;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class map_results extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private String clothing_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_results);
        // define map fragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // initialize map system + view
        mapFragment.getMapAsync(this);
        // get Elements to show in map
        clothing_list = getIntent().getStringExtra("clothing_list");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // define map-element
        mMap = googleMap;
        try {
            JSONArray jsonArray = new JSONArray(clothing_list);
            // iterate result-JSONArray and Place Marker with extra information
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                mMap.addMarker(new MarkerOptions().position(new LatLng(row.getDouble("latitude"),
                        row.getDouble("longitude"))).title(row.getString("name") + " in "+
                        row.getString("city")).snippet("Größe: " + row.getString("groesse") + "\n"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
