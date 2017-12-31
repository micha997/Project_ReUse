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
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Frank on 30.12.2017.
 */

public class EditClothing extends AppCompatActivity {


    private EditText txtFabric;
    private Button btnPutClothing;
    private String cId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clothing);
        cId = getIntent().getStringExtra("cId");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uId = firebaseAuth.getCurrentUser().getUid();

        txtFabric = (EditText) findViewById(R.id.txtFabric);
        btnPutClothing = (Button) findViewById(R.id.btnPutClothing);

        btnPutClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject newProfile = new JSONObject();
                try {
                    newProfile.put("fabric", txtFabric.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // define http service call
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                // define parameters for Service-Call
                myIntent.putExtra("payload", newProfile.toString());
                myIntent.putExtra("method", "PUT");
                myIntent.putExtra("from", "PUTCLOTHING");
                myIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + cId);
                //call http service
                startService(myIntent);
            }
        });


        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("editclothing"));

        myIntent.putExtra("payload", "");
        myIntent.putExtra("method", "GET");
        myIntent.putExtra("from", "EDITCLOTHING");
        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + cId );
        //call http service
        startService(myIntent);



    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String clothing = intent.getStringExtra("clothing");

            try {
                JSONObject clothingJson=new JSONObject(clothing);
                txtFabric.setText(clothingJson.getString("fabric"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
