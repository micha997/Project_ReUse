package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Frank on 09.11.2017.
 */

public class ShowPrefer extends AppCompatActivity {
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private TextView tvPreferUserName;
    private TextView tvPreferUserColor;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitivy_showprefer);

        tvPreferUserColor = (TextView) findViewById(R.id.tvPreferUserColor);
        tvPreferUserName = (TextView) findViewById(R.id.tvPreferUserName);

        String uId=firebaseAuth.getCurrentUser().getUid().toString();
        String name = firebaseAuth.getCurrentUser().getEmail().toString();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("prefer"));

        tvPreferUserName.setText("Vorlieben: " + name);

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        // define parameters for Http-Service call
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SEARCHPREFER");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/users/" + uId + "/prefer");
        //call http service
        startService(myIntent);


    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String from = intent.getStringExtra("from");
            if (from.equals("SEARCHPREFERFAIL")) {
                showDialog("Error","Could not add prefer!");
            } else {
                tvPreferUserColor = (TextView) findViewById(R.id.tvPreferUserColor);
                JSONObject prefer = null;
                try {
                    prefer = new JSONObject(intent.getStringExtra("prefer"));
                    tvPreferUserColor.setText("Blau: " + prefer.get("blue").toString() + "\n" + "Gelb: " + prefer.get("yellow").toString() + "\n"
                            + "Rot: " + prefer.get("red").toString() + "\n" + "Grau: " + prefer.get("gray").toString() + "\n"
                            + "Schwarz: " + prefer.get("black").toString() + "\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ShowPrefer.this).create();
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
