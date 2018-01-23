package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Frank on 02.01.2018.
 */

public class ShowOutfit extends AppCompatActivity implements View.OnClickListener {

    //private TextView txtShowOutfit;
    private Spinner spinnerHead;
    private Spinner spinnerLayer1;
    private Spinner spinnerLayer2;
    private Spinner spinnerLayer3;
    private Spinner spinnerBottom;
    private Spinner spinnerShoes;
    private Spinner spinnerMissingClothing;

    private ArrayList<String> miss = new ArrayList();
    private ArrayList<String> head = new ArrayList();
    private ArrayList<String> layer1 = new ArrayList();
    private ArrayList<String> layer2 = new ArrayList();
    private ArrayList<String> layer3 = new ArrayList();
    private ArrayList<String> bottom = new ArrayList();
    private ArrayList<String> shoes = new ArrayList();

    private ArrayAdapter<String> missingAdapter;
    private ArrayAdapter<String> headAdapter;
    private ArrayAdapter<String> layer1Adapter;
    private ArrayAdapter<String> layer2Adapter;
    private ArrayAdapter<String> layer3Adapter;
    private ArrayAdapter<String> bottomAdapter;
    private ArrayAdapter<String> shoesAdapter;


    private Button btnSendClothingRequest;
    private Button btnSubscribeMissingClothing;

    private String model;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId = firebaseAuth.getCurrentUser().getUid();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_outfit_2);

        //txtShowOutfit = (TextView) findViewById(R.id.txtShowOutfit);
        spinnerHead = (Spinner) findViewById(R.id.spinnerHead);
        spinnerLayer1 = (Spinner) findViewById(R.id.spinnerLayer1);
        spinnerLayer2 = (Spinner) findViewById(R.id.spinnerLayer2);
        spinnerLayer3 = (Spinner) findViewById(R.id.spinnerLayer3);
        spinnerBottom = (Spinner) findViewById(R.id.spinnerBottom);
        spinnerShoes = (Spinner) findViewById(R.id.spinnerShoes);
        spinnerMissingClothing = (Spinner) findViewById(R.id.spinnerMissingClothing);

        btnSendClothingRequest = (Button) findViewById(R.id.btnSendClothingRequest);
        btnSendClothingRequest.setOnClickListener(this);
        btnSubscribeMissingClothing = (Button) findViewById(R.id.btnSubscribeMissingClothing);
        btnSubscribeMissingClothing.setOnClickListener(this);





        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showdetails"));

        try {
            String outfit= getIntent().getStringExtra("outfit");
            JSONObject outfitsArray = new JSONObject(outfit);
            JSONArray layers = outfitsArray.getJSONArray("layers");
            model = outfitsArray.getString("model");
            String objects;
            for(int ilayer = 0; ilayer < layers.length(); ilayer++)
            {
                objects = outfitsArray.getString((String) layers.get(ilayer));
                if (objects.equals("[]")) {
                    miss.add((String) layers.get(ilayer));
                } else {
                    switch (ilayer){
                        case 0: JSONArray headArray = outfitsArray.getJSONArray("head");
                            for (int j=0;headArray.length()>j;j++){
                                head.add((String) headArray.get(j));
                            }
                            break;
                        case 1: JSONArray layer1Array = outfitsArray.getJSONArray("layer1");
                            for (int j=0;layer1Array.length()>j;j++){
                                layer1.add((String) layer1Array.get(j));
                            }
                            break;
                        case 2: JSONArray layer2Array = outfitsArray.getJSONArray("layer2");
                            for (int j=0;layer2Array.length()>j;j++){
                                layer2.add((String) layer2Array.get(j));
                            }
                            break;
                        case 3: JSONArray layer3Array = outfitsArray.getJSONArray("layer3");
                            for (int j=0;layer3Array.length()>j;j++){
                                layer3.add((String) layer3Array.get(j));
                            }
                            break;
                        case 4: JSONArray bottomArray = outfitsArray.getJSONArray("bottom");
                            for (int j=0;bottomArray.length()>j;j++){
                                bottom.add((String) bottomArray.get(j));
                            }
                            break;
                        case 5: JSONArray shoesArray = outfitsArray.getJSONArray("shoes");
                            for (int j=0;shoesArray.length()>j;j++){
                                shoes.add((String) shoesArray.get(j));
                            }
                            break;
                    }
                    //txtShowOutfit.append(layers.get(ilayer) + ": " + objects + "\n\n");
                }
            }
            missingAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, miss);
            spinnerMissingClothing.setAdapter(missingAdapter);

            headAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, head);
            spinnerHead.setAdapter(headAdapter);

            layer1Adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, layer1);
            spinnerLayer1.setAdapter(layer1Adapter);

            layer2Adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, layer2);
            spinnerLayer2.setAdapter(layer2Adapter);

            layer3Adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, layer3);
            spinnerLayer3.setAdapter(layer3Adapter);

            bottomAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, bottom);
            spinnerBottom.setAdapter(bottomAdapter);

            shoesAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, shoes);
            spinnerShoes.setAdapter(shoesAdapter);

        } catch (JSONException e) {
            showDialog("Error", "Could not process outfit data!");
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btnSendClothingRequest:
                if(spinnerHead.getSelectedItem() !=null)
                    sendRequest(spinnerHead.getSelectedItem().toString());
                if(spinnerLayer1.getSelectedItem() !=null)
                    sendRequest(spinnerLayer1.getSelectedItem().toString());
                if(spinnerLayer2.getSelectedItem() !=null)
                    sendRequest(spinnerLayer2.getSelectedItem().toString());
                if(spinnerLayer3.getSelectedItem() !=null)
                    sendRequest(spinnerLayer3.getSelectedItem().toString());
                if(spinnerBottom.getSelectedItem() !=null)
                    sendRequest(spinnerBottom.getSelectedItem().toString());
                if(spinnerShoes.getSelectedItem() !=null)
                    sendRequest(spinnerShoes.getSelectedItem().toString());
                break;

            case R.id.btnSubscribeMissingClothing:
                if(spinnerMissingClothing.getSelectedItem() !=null) {
                    JSONObject subscribe = new JSONObject();
                    try {
                        // Model vom fehlenden Kleidungsstück fehltfesthalten
                        subscribe.put("model", model);
                        // Layer vom fehlenden Kleidunststück festhalten
                        subscribe.put("missing", spinnerMissingClothing.getSelectedItem().toString());

                        // Sende Suchanfrage an Server
                        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                        myIntent.putExtra("payload", subscribe.toString());
                        myIntent.putExtra("method", "POST");
                        myIntent.putExtra("from", "SUBSCRIBECLOTHING");
                        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + uId + "/search");
                        startService(myIntent);
                    } catch (JSONException e) {
                        // Suche konnte nicht eingetragen werden
                        showDialog("Error", "Could not subscribe for missing clothing!");
                    }
                }
                break;
        }
    }

    public void sendRequest(String clothingID){

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SHOWDETAILS");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothing/" + clothingID);
        startService(myIntent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {



                JSONObject request = new JSONObject();
                try {
                    String clothing = intent.getStringExtra("clothing");
                    JSONObject clothingJson = new JSONObject(clothing);
                    // ID's der Benutzer festhalten
                    request.put("uId", uId);
                    request.put("ouId", clothingJson.getString("uId"));

                    // Sende Suchanfrage zum Server
                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    myIntent.putExtra("payload",request.toString());
                    myIntent.putExtra("method","POST");
                    myIntent.putExtra("from", "NEWREQUEST");
                    myIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothing/"+ clothingJson.getString("id"));
                    startService(myIntent);
                } catch (JSONException e) {
                    showDialog("Error", "Could not process clothing data!");
                }

        }
    };

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ShowOutfit.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (!isFinishing())
        alertDialog.show();
    }
}
