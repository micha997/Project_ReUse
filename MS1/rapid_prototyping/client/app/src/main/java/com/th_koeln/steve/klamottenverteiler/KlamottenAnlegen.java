package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
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
    String position = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_klamotten_anlegen);
        etName = (EditText) findViewById(R.id.etName);
        etGroesse = (EditText) findViewById(R.id.etGroesse);
        etPosition = (EditText) findViewById(R.id.etPosition);
        btnChooseLocation = (Button) findViewById(R.id.btnChooseLocation);

        btnChooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    kleidung.put("position", position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // start http service
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                // define parameters for Service-Call
                myIntent.putExtra("payload",kleidung.toString());
                myIntent.putExtra("url","https://192.168.0.80:3000/klamotten");
                startService(myIntent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getApplicationContext(), data );
                position = place.getLatLng().toString();
            }
        }
    }

}




