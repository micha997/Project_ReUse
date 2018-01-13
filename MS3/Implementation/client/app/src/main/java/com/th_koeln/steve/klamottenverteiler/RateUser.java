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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Frank on 10.01.2018.
 */

public class RateUser extends AppCompatActivity {

    private EditText txtComment;
    private Spinner spinChooseRating;
    private Button btnSendRating;
    private TextView txtTransactionDetails;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        txtComment = (EditText) findViewById(R.id.txtComment);
        spinChooseRating = (Spinner) findViewById(R.id.spinChooseRating);
        btnSendRating = (Button) findViewById(R.id.btnSendRating);
        txtTransactionDetails = (TextView) findViewById(R.id.txtTransactionDetails);


        ArrayAdapter<String> aArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
        aArrayAdapter.add("Good");
        aArrayAdapter.add("Okay");
        aArrayAdapter.add("Bad");

        spinChooseRating.setAdapter(aArrayAdapter);

        String tId = getIntent().getStringExtra("tId");
        final String request = getIntent().getStringExtra("request");

        txtTransactionDetails.append(tId);
        txtTransactionDetails.append(request);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("RATEUSER"));

        btnSendRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date now = new Date();

                    String strDate = sdfDate.format(now);
                    Date date = sdfDate.parse(strDate);
                    JSONObject rating = new JSONObject(request);
                    rating.put("choice", spinChooseRating.getSelectedItem().toString());
                    rating.put("comment", txtComment.getText().toString());
                    rating.put("time",date.getTime() );
                    String ouId=null;
                    if (uId.equals(rating.getString("ouId"))) {
                       ouId = rating.getString("uId");
                    } else {
                       ouId = rating.getString("ouId");
                    }
                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    myIntent.putExtra("payload",rating.toString());
                    myIntent.putExtra("method","POST");
                    myIntent.putExtra("from","POSTRATING");
                    myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + ouId + "/rating");
                    startService(myIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service

            String from = intent.getStringExtra("from");
            if (from.equals("POSTRATINGFAIL")) {
                showDialog("Error","Could not add rating to Server!");
            }

        }
    };

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(RateUser.this).create();
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
