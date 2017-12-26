package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by steve on 31.10.17.
 */

public class AddClothing extends AppCompatActivity {

    private static int PLACE_PICKER_REQUEST;

    private EditText etSize;
    private EditText etArt;
    private EditText etStyle;
    private EditText etGender;
    private EditText etAge;
    private EditText etColour;
    private EditText etFabric;
    private EditText etNotes;
    private EditText etBrand;
    private ImageView imgImage;
    private byte[] byteArray;
    private Button kleidungAnlegen;
    private Button btnChooseLocation;
    private Button btnChoouseImage;
    private double latitude;
    private double longitude;
    private String city = null;
    private Geocoder geocoder;
    private String result;
    public static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_klamotten_anlegen);
        etSize = (EditText) findViewById(R.id.etSize);
        etArt = (EditText) findViewById(R.id.etArt);
        etStyle = (EditText) findViewById(R.id.etStyle);
        etGender = (EditText) findViewById(R.id.etGender);
        etColour = (EditText) findViewById(R.id.etColour);
        imgImage = (ImageView) findViewById(R.id.imgImage);
        etNotes = (EditText) findViewById(R.id.etNotes);
        etFabric = (EditText) findViewById(R.id.etFabric);
        etBrand = (EditText) findViewById(R.id.etBrand);
        btnChooseLocation = (Button) findViewById(R.id.btnChooseLocation);
        btnChoouseImage = (Button) findViewById(R.id.btnChooseImage);

        geocoder = new Geocoder(this, Locale.getDefault());

        btnChooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Place Picker
                try {
                    Intent intent = new PlacePicker.IntentBuilder().build(AddClothing.this);
                    startActivityForResult(intent, AddClothing.PLACE_PICKER_REQUEST);
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

        btnChoouseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        kleidungAnlegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String size = etSize.getText().toString();
                String art = etArt.getText().toString();
                String style = etStyle.getText().toString();
                String gender = etGender.getText().toString();
                String colour = etColour.getText().toString();
                String fabric = etFabric.getText().toString();
                String notes = etNotes.getText().toString();
                String brand = etBrand.getText().toString();

                // build JSON object for clothing post
                JSONObject kleidung = new JSONObject();
                try {
                    kleidung.put("size",size);
                    kleidung.put("art",art);
                    kleidung.put("style",style);
                    kleidung.put("gender",gender);
                    kleidung.put("colours", colour);
                    kleidung.put("brand", brand);
                    kleidung.put("fabric", fabric);
                    kleidung.put("notes", notes);
                    kleidung.put("longitude", longitude);
                    kleidung.put("latitude",latitude);
                    kleidung.put("city",city);
                    kleidung.put("image",result);
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


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                // get Place Picker location
                Place place = PlacePicker.getPlace(getApplicationContext(), data);
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;


            }
        }
        if (requestCode == PICK_IMAGE) {
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] byteData = new byte[16384];

                while ((nRead = inputStream.read(byteData, 0, byteData.length)) != -1) {
                    buffer.write(byteData, 0, nRead);
                }
                buffer.flush();

                byte imageData[] = buffer.toByteArray();
                inputStream.read(imageData);
                result = Base64.encodeToString(imageData, Base64.DEFAULT);
                imgImage.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}




