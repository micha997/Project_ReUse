package com.th_koeln.steve.klamottenverteiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Frank on 30.12.2017.
 */

public class MyClothing extends AppCompatActivity {

    private Spinner spinChooseClothing;
    private Button btnEditClothing;
    private String names[] = {"Red", "Blue", "Green"};
    private ArrayList<String> ids = new ArrayList();
    private String cId;

    private ArrayAdapter<String> clothingAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_clothing);
        btnEditClothing = (Button) findViewById(R.id.btnEditClothing);
        spinChooseClothing = (Spinner) findViewById(R.id.spinChooseClothing);
        btnEditClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("myclothing"));

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uId = firebaseAuth.getCurrentUser().getUid();
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("payload", "");
        myIntent.putExtra("method", "GET");
        myIntent.putExtra("from", "MYCLOTHING");
        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + uId + "/clothing");
        //call http service
        startService(myIntent);

        btnEditClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editIntent = new Intent(getApplicationContext(), EditClothing.class);
                editIntent.putExtra("cId",cId);
                startActivity(editIntent);
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String clothing = intent.getStringExtra("clothing");

            try {
                JSONArray clothingJsonArray = new JSONArray(clothing);
                for (int i = 0; i < clothingJsonArray.length(); i++) {
                    JSONObject clothingJsonObject = clothingJsonArray.getJSONObject(i);
                    ids.add(clothingJsonObject.getString("id").toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            clothingAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, ids);
            spinChooseClothing.setAdapter(clothingAdapter);

        spinChooseClothing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cId = spinChooseClothing.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        }
    };
}


