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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Frank on 25.12.2017.
 */

public class ShowClothing extends AppCompatActivity {

    private ImageView imgClothingDetails;
    private TextView txtClothing;
    private Button btnGetClothing;
    private String clothing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showclothing);
        txtClothing = (TextView) findViewById(R.id.txtClothing);
        imgClothingDetails = (ImageView) findViewById(R.id.imgCLothingDetail);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String ouId= firebaseAuth.getCurrentUser().getUid();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showdetails"));

        btnGetClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject request = new JSONObject();
                try {
                    JSONObject clothingJson = new JSONObject(clothing);

                    request.put("uId", clothingJson.getString("uId)"));
                    request.put("ouId", clothingJson.getString("ouId"));
                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    myIntent.putExtra("payload",request.toString());
                    myIntent.putExtra("method","POST");
                    myIntent.putExtra("from", "NEWREQUEST");
                    myIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothing/"+ clothingJson.getString("id)"));
                    startService(myIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //call http service

            }
        });

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            clothing = intent.getStringExtra("clothing");
            txtClothing.setText(clothing);
        }
    };
}

