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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Frank on 30.12.2017.
 */

public class EditProfile extends AppCompatActivity {

    private EditText etGender;
    private Button btnSendProfile;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uId = firebaseAuth.getCurrentUser().getUid();

        etGender = (EditText) findViewById(R.id.editTextGender);
        btnSendProfile = (Button) findViewById(R.id.btnSendProfile);

        btnSendProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject newProfile = new JSONObject();
                try {
                    newProfile.put("gender", etGender.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // define http service call
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                // define parameters for Service-Call
                myIntent.putExtra("payload",newProfile.toString());
                myIntent.putExtra("method","PUT");
                myIntent.putExtra("from","PUTPROFILE");
                myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId);
                //call http service
                startService(myIntent);
            }
        });

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("profile"));
        // get desired vicinity in km
        // define parameters for Http-Service call
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","PROFILE");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId);
        //call http service
        startService(myIntent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String profile = intent.getStringExtra("profile");
            String from = intent.getStringExtra("from");
            try {
                JSONObject profileJson = new JSONObject(profile);
                etGender.setText(profileJson.getString("gender"));
                //... fill user profile interface
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

}
