package com.th_koeln.steve.klamottenverteiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

/**
 * Created by steve on 01.11.17.
 */
public class KlamottenSuchen extends AppCompatActivity {
    private Button btnSuchenLocation;
    private Button btnSuchen;
    private Button btnSearchPref;
    private EditText etVicinity;

    private double latitude;
    private double longitude;
    private String vicinity;
    private static int PLACE_PICKER_REQUEST;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_klamotten_suchen);

        btnSearchPref = (Button) findViewById(R.id.btnSearchPrefer);
        btnSuchen = (Button) findViewById(R.id.btnSuchen);
        btnSuchenLocation = (Button) findViewById(R.id.btnSucheLocation);
        etVicinity = (EditText) findViewById(R.id.eTvicinity);

        // broadcast for getting clothing elements from HTTP Service
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("clothing"));

        // search for clothing

        btnSearchPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                String uId = firebaseAuth.getCurrentUser().getUid();
                // get desired vicinity in km
                vicinity = etVicinity.getText().toString();
                // define parameters for Http-Service call
                myIntent.putExtra("payload","");
                myIntent.putExtra("method","GET");
                myIntent.putExtra("from","SEARCHPREFCLOTHING");
                myIntent.putExtra("url",getString(R.string.DOMAIN) +"/users/"+ uId + "/prefer/klamotten/"+ latitude + "/" + longitude + "/" + vicinity);
                //call http service
                startService(myIntent);

            }
        });

        btnSuchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                String uId = firebaseAuth.getCurrentUser().getUid();
                // get desired vicinity in km
                vicinity = etVicinity.getText().toString();
                // define parameters for Http-Service call
                myIntent.putExtra("payload","");
                myIntent.putExtra("method","GET");
                myIntent.putExtra("from","SEARCH");
                myIntent.putExtra("url",getString(R.string.DOMAIN) +"/klamotten/"+ latitude + "/" + longitude + "/" + vicinity + "/" + uId);
                //call http service
                startService(myIntent);
            }
        });


        // Choose Search-Location Button
        btnSuchenLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Place Picker Fragment to choose a location
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String clothinglist = intent.getStringExtra("clothing");
            String from = intent.getStringExtra("from");
            // send clothing results to Google Maps activity
            Intent myIntent = new Intent(getApplicationContext(),map_results.class);
            myIntent.putExtra("clothing_list", clothinglist);
            startActivity(myIntent);

        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                // get SearchLocation from Place Picker and safe latitude and longitude
                Place place = PlacePicker.getPlace(getApplicationContext(),data);
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
            }
        }
    }
}
