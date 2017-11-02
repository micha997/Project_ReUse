package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by steve on 01.11.17.
 */
public class KlamottenSuchen extends AppCompatActivity {
    private Button btnSuchenLocation;
    private Button btnSuchen;
    private String position;
    private GoogleMap mMap;
    private static int PLACE_PICKER_REQUEST;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_klamotten_suchen);
        btnSuchen = (Button) findViewById(R.id.btnSuchen);

        // search for clothing
        btnSuchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(),map_results.class);
                startActivity(myIntent);
            }
        });

        btnSuchenLocation = (Button) findViewById(R.id.btnSucheLocation);

        btnSuchenLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Fragment to choose a location
                try {
                    Intent intent = new PlacePicker.IntentBuilder().build(KlamottenSuchen.this);
                    startActivityForResult(intent, KlamottenSuchen.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getApplicationContext());
                position = place.getLatLng().toString();
            }
        }
    }
}
