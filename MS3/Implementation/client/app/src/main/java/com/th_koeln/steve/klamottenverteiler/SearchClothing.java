package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
public class SearchClothing extends AppCompatActivity implements View.OnClickListener {
    private EditText etVicinity;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uId = firebaseAuth.getCurrentUser().getUid();
    private Button btnSearchPrefer;
    private Button btnSearch;
    private Button btnSearchLocation;

    private double latitude;
    private double longitude;
    private String vicinity;
    private static int PLACE_PICKER_REQUEST;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_search_clothing);

        btnSearchLocation = (Button) findViewById(R.id.btnSearchLocation);
        btnSearchLocation.setOnClickListener(this);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);

        btnSearchPrefer = (Button) findViewById(R.id.btnSearchPrefer);
        btnSearchPrefer.setOnClickListener(this);

        etVicinity = (EditText) findViewById(R.id.eTvicinity);

        // broadcast for getting clothing elements from HTTP Service
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("clothing"));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String from = intent.getStringExtra("from");
            if (from.equals("SEARCHFAIL")) {
                showDialog("Error","Could not get clothing!");
            } else if (from.equals("SEARCHPREFCLOTHINGFAIL")) {
                showDialog("Error","Could not get clothing!");
            } else {
                // get clothing results from HTTP-Service
                String clothinglist = intent.getStringExtra("clothing");

                // send clothing results to Google Maps activity
                Intent myIntent = new Intent(getApplicationContext(),map_results.class);
                myIntent.putExtra("clothing_list", clothinglist);
                startActivity(myIntent);
            }

        }
    };


    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(SearchClothing.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

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

    @Override
    public void onClick(View view) {
        Intent myIntent;

        switch (view.getId()) {

            case R.id.btnSearchLocation:
                try {
                    myIntent = new PlacePicker.IntentBuilder().build(SearchClothing.this);
                    startActivityForResult(myIntent, SearchClothing.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    showDialog("Error", "Could not open Place Picker Service!");
                } catch (GooglePlayServicesNotAvailableException e) {
                    showDialog("Error", "Could not open Place Picker Service!");
                } catch (Exception e) {
                    showDialog("Error", "Could not open Place Picker Service!");
                }
                break;

            case R.id.btnSearch:
                myIntent = new Intent(getApplicationContext(), HttpsService.class);
                // get desired vicinity in km
                vicinity = etVicinity.getText().toString();
                // define parameters for Http-Service call
                myIntent.putExtra("payload","");
                myIntent.putExtra("method","GET");
                myIntent.putExtra("from","SEARCH");
                myIntent.putExtra("url",getString(R.string.DOMAIN) +"/klamotten/"+ latitude + "/" + longitude + "/" + vicinity + "/" + uId);
                //call http service
                startService(myIntent);
                break;

            case R.id.btnSearchPrefer:
                myIntent = new Intent(getApplicationContext(), HttpsService.class);
                // get desired vicinity in km
                vicinity = etVicinity.getText().toString();
                // define parameters for Http-Service call
                myIntent.putExtra("payload","");
                myIntent.putExtra("method","GET");
                myIntent.putExtra("from","SEARCHPREFCLOTHING");
                myIntent.putExtra("url",getString(R.string.DOMAIN) +"/users/"+ uId + "/prefer/klamotten/"+ latitude + "/" + longitude + "/" + vicinity);
                //call http service
                startService(myIntent);
                break;

            default:
                break;
        }
    }
}
