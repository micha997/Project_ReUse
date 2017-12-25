package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by steve on 31.10.17.
 */

public class KlamottenAnlegen extends AppCompatActivity {

    private static int PLACE_PICKER_REQUEST;

    private EditText etSize;
    private EditText etArt;
    private EditText etStyle;
    private EditText etGender;
    private EditText etAge;
    private EditText etColour;

    private Button kleidungAnlegen;
    private Button btnChooseLocation;
    private double latitude;
    private double longitude;
    private String city = null;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_klamotten_anlegen);
        etSize = (EditText) findViewById(R.id.etSize);
        etArt = (EditText) findViewById(R.id.etArt);
        etStyle = (EditText) findViewById(R.id.etStyle);
        etGender = (EditText) findViewById(R.id.etGender);
        etAge = (EditText) findViewById(R.id.etAge);
        etColour = (EditText) findViewById(R.id.etColour);

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
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uiD= firebaseAuth.getCurrentUser().getUid();
        kleidungAnlegen = (Button) findViewById(R.id.btnKlamottenEinstellen);

        kleidungAnlegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String size = etSize.getText().toString();
                String art = etArt.getText().toString();
                String style = etStyle.getText().toString();
                String gender = etGender.getText().toString();
                String age = etAge.getText().toString();
                String colour = etColour.getText().toString();

                size=calcSize(size,art);

                // build JSON object for clothing post
                JSONObject kleidung = new JSONObject();
                try {
                    kleidung.put("size",size);
                    kleidung.put("art",art);
                    kleidung.put("style",style);
                    kleidung.put("gender",gender);
                    kleidung.put("age",age);
                    kleidung.put("colour", colour);
                    kleidung.put("longitude", longitude);
                    kleidung.put("latitude",latitude);
                    kleidung.put("city",city);
                    kleidung.put("uId", uiD);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // define http service call
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                // define parameters for Service-Call
                myIntent.putExtra("payload",kleidung.toString());
                myIntent.putExtra("method","POST");
                myIntent.putExtra("from","POSTKLAMOTTEN");
                myIntent.putExtra("url",getString(R.string.DOMAIN) + "/klamotten");
                //call http service
                startService(myIntent);
            }
        });
    }
    private String calcSize(String size, String art) {
        ArrayList<String> sizes = new ArrayList<>();
        sizes.add("XXS");
        sizes.add("XS");
        sizes.add("S");
        sizes.add("M");
        sizes.add("L");
        sizes.add("XL");
        sizes.add("XXL");
        sizes.add("XXXL");
        sizes.add("XXXXL");

        if (sizes.contains(size)) {
            return size;
        }
        int iSize=Integer.parseInt(size);
        if (art.equals("trousers") ) {
            if ( iSize <= 38) {
                size= "XXS";
            } else if (38 <= iSize && iSize <= 42) {
                size= "XS";
            } else if (42 < iSize && iSize <= 46) {
                size= "S";
            } else if (46 < iSize && iSize <= 50) {
                size= "M";
            } else if (50 < iSize && iSize <= 54) {
                size= "L";
            } else if (54 < iSize && iSize <= 58) {
                return "XX";
            } else if (58 < iSize && iSize <= 62) {
                size= "XXL";
            } else if (62 < iSize && iSize <= 66) {
                size= "XXXL";
            } else if (66 < iSize) {
                size= "XXXXL";
            } else {
                size="Falsche Groesse";
            }
        }
        return size;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                // get Place Picker location
                Place place = PlacePicker.getPlace(getApplicationContext(), data );
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                /*// get city name
                try {
                    city = getNameOfCity(latitude,longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/


            }
        }
    }

/*    private String getNameOfCity(double lat, double lon) throws IOException {
        //search for address with geo coder
        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
        //extract city name
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }*/

}




