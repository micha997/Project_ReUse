package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.adapter.ClothingOfferAdapter;
import com.th_koeln.steve.klamottenverteiler.adapter.ClothingOptionsAdapter;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;
import com.th_koeln.steve.klamottenverteiler.services.ListViewHelper;
import com.th_koeln.steve.klamottenverteiler.services.RecyclerListener;
import com.th_koeln.steve.klamottenverteiler.structures.ClothingOffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
    Created by steve on 01.11.17.
 */
public class SearchClothing extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uId = firebaseAuth.getCurrentUser().getUid();

    private ArrayList<ClothingOffer> ListForAdapter;
    private RecyclerView searchRecyclerView;
    private Toolbar searchClothingToolbar;

    private double latitude = 50.908620299999995;
    private double longitude = 6.9563028;
    private long vicinity = 9999;
    private static int PLACE_PICKER_REQUEST;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_clothing_2);

        searchRecyclerView = (RecyclerView) findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setHasFixedSize(true);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchRecyclerView.addOnItemTouchListener(
                new RecyclerListener(this, searchRecyclerView, new RecyclerListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent showIntent = new Intent(getApplicationContext(), ShowClothing.class);
                        showIntent.putExtra("clothingID",ListForAdapter.get(position).getId());
                        startActivity(showIntent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        //Weitere moeglichkeiten
                    }
                }));

        ListForAdapter = new ArrayList<>();

        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Trying to get clothing..\n");
        progressDialog.show();

        searchClothingToolbar = (Toolbar) findViewById(R.id.searchClothingToolbar);
        setSupportActionBar(searchClothingToolbar);

        // broadcast for getting clothing elements from HTTP Service
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("clothing"));

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        // define parameters for Http-Service call
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SEARCH");
        myIntent.putExtra("url",getString(R.string.DOMAIN) +"/klamotten/"+ latitude + "/" + longitude + "/" + vicinity + "/" + uId);
        //call http service
        startService(myIntent);


    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String from = intent.getStringExtra("from");
            if (from.equals("SEARCHFAIL")) {
                showDialog("Error", "Could not get clothing!");
                progressDialog.dismiss();
            } else if (from.equals("SEARCHPREFCLOTHINGFAIL")) {
                showDialog("Error", "Could not get clothing!");
                progressDialog.dismiss();
            } else {
                try {
                    // get clothing results from HTTP-Service
                    String clothinglist = intent.getStringExtra("clothing");
                    JSONArray clothinglistJsonArray = new JSONArray(clothinglist);

                    for (int i = 0; i < clothinglistJsonArray.length(); i++) {

                        JSONObject tmpObj = clothinglistJsonArray.getJSONObject(i);
                        String absPath = "";

                        if (!tmpObj.isNull("image")) {
                            String filename = "img" + i;
                            String string = clothinglistJsonArray.getJSONObject(i).getString("image");
                            FileOutputStream outputStream;
                            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                            byte[] decodedBytes = Base64.decode(string, 0);
                            outputStream.write(decodedBytes);
                            outputStream.close();
                            clothinglistJsonArray.getJSONObject(i).put("imagepath", filename);
                            clothinglistJsonArray.getJSONObject(i).remove("image");

                            absPath = SearchClothing.this.getFilesDir().getAbsolutePath() + "/" + filename;
                        }

                        ListForAdapter.add(new ClothingOffer(tmpObj.getString("id"),tmpObj.getString("uId"),
                                tmpObj.getString("art"),tmpObj.getString("size"),tmpObj.getString("style"),
                                tmpObj.getString("gender"),"",tmpObj.getString("fabric"),
                                tmpObj.getString("notes"),tmpObj.getString("brand"),absPath,tmpObj.getDouble("distance")));
                    }

                    fillView(ListForAdapter);

                } catch (JSONException e) {
                    showDialog("Error", "Could not process clothing data!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                // send clothing results to Google Maps activity
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

    private void fillView(ArrayList<ClothingOffer> options) {
        ClothingOfferAdapter optAdapter;
        optAdapter = new ClothingOfferAdapter(this, options);
        searchRecyclerView.setAdapter(optAdapter);
    }


    /*
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
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.search_clothing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int actionID = item.getItemId();

        if(actionID == R.id.action_filter){

        }

        if(actionID == R.id.action_search){

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        /*
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
        */
    }

}