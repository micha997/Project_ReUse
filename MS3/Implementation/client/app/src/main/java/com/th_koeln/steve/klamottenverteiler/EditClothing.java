package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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

public class EditClothing extends AppCompatActivity {


    private EditText txtFabric;
    private Button btnPutClothing;
    private String cId;
    private TextView txtShowClothing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clothing);
        cId = getIntent().getStringExtra("cId");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uId = firebaseAuth.getCurrentUser().getUid();

        txtFabric = (EditText) findViewById(R.id.txtFabric);
        btnPutClothing = (Button) findViewById(R.id.btnPutClothing);
        txtShowClothing = (TextView) findViewById(R.id.txtShowClothing);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("editclothing"));

        btnPutClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject newProfile = new JSONObject();
                try {
                    newProfile.put("fabric", txtFabric.getText().toString());
                    // define http service call
                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    // define parameters for Service-Call
                    myIntent.putExtra("payload", newProfile.toString());
                    myIntent.putExtra("method", "PUT");
                    myIntent.putExtra("from", "PUTCLOTHING");
                    myIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + cId);
                    //call http service
                    startService(myIntent);
                } catch (JSONException e) {
                    showDialog("Error", "Could not process your entries!");
                }

            }
        });

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
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
            String from = intent.getStringExtra("from");

            if (from.equals("EDITCLOTHINGFAIL")) {
                showDialog("Error", "Could not get clothing!");
            } else if (from.equals("PUTCLOTHINGFAIL")) {
                showDialog("Error", "Could not edit clothing!");
            } else {
                try {
                    String clothing = intent.getStringExtra("clothing");
                    JSONObject clothingJson=new JSONObject(clothing);
                    txtFabric.setText(clothingJson.getString("fabric"));
                    txtShowClothing.setText(clothingJson.toString());
                } catch (JSONException e) {
                    showDialog("Error", "Could not process your entries!");
                }
            }


        }
    };

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(EditClothing.this).create();
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
}
