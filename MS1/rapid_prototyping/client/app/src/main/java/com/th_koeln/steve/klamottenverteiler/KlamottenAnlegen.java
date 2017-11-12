package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by steve on 31.10.17.
 */

public class KlamottenAnlegen extends AppCompatActivity {

    private static int PLACE_PICKER_REQUEST;
    private EditText etName;
    private EditText etGroesse;
    private EditText etPosition;
    private Button kleidungAnlegen;
    private Button btnChooseLocation;
    private double latitude;
    private double longitude;
    private String city;
    private Geocoder geocoder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_klamotten_anlegen);
        etName = (EditText) findViewById(R.id.etName);
        etGroesse = (EditText) findViewById(R.id.etGroesse);
        etPosition = (EditText) findViewById(R.id.etPosition);
        btnChooseLocation = (Button) findViewById(R.id.btnChooseLocation);
        geocoder = new Geocoder(this, Locale.getDefault());

        btnChooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Place Picker
                try {
                    Intent intent = new PlacePicker.IntentBuilder().build(KlamottenAnlegen.this);
                    startActivityForResult(intent, KlamottenAnlegen.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        kleidungAnlegen = (Button) findViewById(R.id.btnKlamottenEinstellen);

        kleidungAnlegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String groesse = etGroesse.getText().toString();

                // build JSON object for clothing
                JSONObject kleidung = new JSONObject();
                try {
                    kleidung.put("groesse",groesse);
                    kleidung.put("name",name);
                    kleidung.put("longitude", longitude);
                    kleidung.put("latitude",latitude);
                    kleidung.put("city",city);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // define http service call
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                // define parameters for Service-Call
                myIntent.putExtra("payload",kleidung.toString());
                myIntent.putExtra("method","POST");
                myIntent.putExtra("url","https://192.168.0.80:3000/klamotten");
                //call http service
                startService(myIntent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                // get Place Picker location
                Place place = PlacePicker.getPlace(getApplicationContext(), data );
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                // get city name
                try {
                    city = getNameOfCity(latitude,longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private String getNameOfCity(double lat, double lon) throws IOException {
        //search for address with geo coder
        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
        //extract city name
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }

}




