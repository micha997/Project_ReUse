package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
import java.util.Iterator;


/**
 * Created by Frank on 02.01.2018.
 */

public class ShowOutfit extends AppCompatActivity {

    private TextView txtShowOutfit;
    private Spinner spinMissingClothing;
    private ArrayList<String> miss = new ArrayList();
    private ArrayAdapter<String> missingAdapter;
    private Button btnSubscribeMissingClothing;
    private String model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_outfit);
        txtShowOutfit = (TextView) findViewById(R.id.txtShowOutfit);
        txtShowOutfit.setText("");
        spinMissingClothing = (Spinner) findViewById(R.id.spinMissingClothing);
        btnSubscribeMissingClothing = (Button) findViewById(R.id.btnSubscribeMissingClothing);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uId = firebaseAuth.getCurrentUser().getUid();

        btnSubscribeMissingClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject subscribe = new JSONObject();
                try {
                    subscribe.put("model", model );
                    subscribe.put("missing",spinMissingClothing.getSelectedItem().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                myIntent.putExtra("payload", subscribe.toString());
                myIntent.putExtra("method","POST");
                myIntent.putExtra("from","SUBSCRIBECLOTHING");
                myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/search");
                startService(myIntent);
            }
        });

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
                    txtShowOutfit.append(layers.get(ilayer) + ": " + objects + "\n\n");
                }
            }
            missingAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, miss);
            spinMissingClothing.setAdapter(missingAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
