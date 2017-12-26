package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class map_results extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private JSONArray jsonArray;
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
        mMap.setOnMarkerClickListener(this);
/*        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

            }
        });*/
        try {
            jsonArray = new JSONArray(clothing_list);
            // iterate result-JSONArray and Place Marker with extra information
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject row = jsonArray.getJSONObject(i);
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(row.getDouble("latitude"),
                        row.getDouble("longitude"))).title(row.getString("art")).snippet("Größe: " + row.getString("size") + "\n" + "Distance: " + row.getString("distance")));
                marker.setTag(row.getString("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object cId = marker.getTag();
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SHOWDETAILS");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothing/" + cId);
        startService(myIntent);

        myIntent = new Intent(getApplicationContext(),ShowClothing.class);
        startActivity(myIntent);
        finish();
        /*String uId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        JSONObject prefer = new JSONObject();
        try {
            prefer.put("prefer", tag.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        // define parameters for Service-Call
        /*for(int i = 0; jsonArray.length() > i; i++)
        {
            try {
                JSONObject objects = jsonArray.getJSONObject(i);
                if (objects.getString("id").equals(prefer.getString("prefer"))) {
                    prefer.put("color",objects.getString("color"));
                    prefer.put("size",objects.getString("size"));
                    prefer.put("art",objects.getString("art"));
                    prefer.put("style",objects.getString("style"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/

        /*myIntent.putExtra("payload",prefer.toString());
        myIntent.putExtra("method","POST");
        myIntent.putExtra("from","POSTPREFER");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/users/" + uId + "/prefer");
        //call http service
        startService(myIntent);*/
        return false;
    }
}
