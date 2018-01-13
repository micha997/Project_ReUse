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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Frank on 07.01.2018.
 */

public class ShowRequest extends AppCompatActivity implements View.OnClickListener {
    private TextView txtShowMyRequests;
    private Spinner spinOpenRequests;
    private ArrayList<String> ids = new ArrayList();
    private ArrayList<String> activeRequests = new ArrayList();
    private ArrayList<String> requestsWaiting = new ArrayList();
    private ArrayAdapter<String> requestAdapter;
    private JSONArray requestJsonArray;
    private TextView txtShowForeignRequests;
    private Button btnTransSuccess;
    private Button btnStartChat;
    private Button btnConfirmTransaction;
    private Button btnAcceptRequest;

    private Spinner spinActiveRequests;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();
    private Spinner spinRequestsWaiting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_requests);

        txtShowMyRequests = (TextView) findViewById(R.id.txtShowMyRequests);
        txtShowForeignRequests = (TextView) findViewById(R.id.txtShowForeignRequests);
        spinOpenRequests = (Spinner) findViewById(R.id.spinOpenRequests);
        spinActiveRequests = (Spinner) findViewById(R.id.spinActiveRequests);
        spinRequestsWaiting = (Spinner) findViewById(R.id.spinRequestsWaiting);

        btnStartChat = (Button) findViewById(R.id.btnStartChat);
        btnStartChat.setOnClickListener(this);

        btnConfirmTransaction = (Button) findViewById(R.id.btnConfirmTransaction);
        btnConfirmTransaction.setOnClickListener(this);

        btnAcceptRequest = (Button) findViewById(R.id.btnAcceptRequest);
        btnAcceptRequest.setOnClickListener(this);

        btnTransSuccess = (Button) findViewById(R.id.btnTransSuccess);
        btnTransSuccess.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showrequests"));

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SHOWREQUESTS");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests");
        startService(myIntent);

    }

    private JSONObject setStatus(String status, String spin) {
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);

        try {
            for (int i = 0; i < requestJsonArray.length(); i++) {

                if (requestJsonArray.getJSONObject(i).getString("id").equals(spin)) {
                    JSONObject putRequest = requestJsonArray.getJSONObject(i);
                    putRequest.put("status", status);

                    if(status.equals("waiting")) {
                        putRequest.put("confirmed", uId);
                    } else {
                        putRequest.put("confirmed", 0);
                    }
                    myIntent.putExtra("payload",putRequest.toString());
                }
            }
            myIntent.putExtra("method","PUT");
            myIntent.putExtra("from","PUTREQUEST");
            myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests/" + spin);
            startService(myIntent);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return null;
        }


        private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            if (from.equals("SHOWREQUESTSFAIL")) {
                showDialog("Error","Could not get request!");
            } else {

                String requests = intent.getStringExtra("clothing");

                try {
                    requestJsonArray = new JSONArray(requests);
                    for (int i = 0; i < requestJsonArray.length(); i++) {
                        JSONObject requestJsonObject = requestJsonArray.getJSONObject(i);
                        if (requestJsonObject.getString("from").equals("own")) {
                            txtShowMyRequests.append(requestJsonObject.toString());

                        } else if (requestJsonObject.getString("from").equals("foreign")) {
                            txtShowForeignRequests.append(requestJsonObject.toString());
                            if (requestJsonObject.getString("status").equals("open"))
                                ids.add(requestJsonObject.getString("id").toString());
                        }
                        if (requestJsonObject.getString("status").equals("accepted"))
                            activeRequests.add(requestJsonObject.getString("id").toString());
                        if (requestJsonObject.getString("status").equals("waiting") && (!requestJsonObject.getString("confirmed").equals(uId)))
                            requestsWaiting.add(requestJsonObject.getString("id").toString());
                    }

                    requestAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, activeRequests);
                    spinActiveRequests.setAdapter(requestAdapter);

                    requestAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, ids);
                    spinOpenRequests.setAdapter(requestAdapter);

                    requestAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, requestsWaiting);
                    spinRequestsWaiting.setAdapter(requestAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    };


    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ShowRequest.this).create();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnStartChat:
                Intent myIntent = new Intent(getApplicationContext(),Chat.class);


                myIntent.putExtra("rId", spinActiveRequests.getSelectedItem().toString());
                try {
                    for (int i = 0; i < requestJsonArray.length(); i++) {
                            if (requestJsonArray.getJSONObject(i).getString("id").equals(spinActiveRequests.getSelectedItem().toString())) {
                                if (uId.equals(requestJsonArray.getJSONObject(i).getString("uId"))) {
                                    myIntent.putExtra("to", requestJsonArray.getJSONObject(i).getString("ouId"));
                                } else {
                                    myIntent.putExtra("to", requestJsonArray.getJSONObject(i).getString("uId"));
                                }
                            }
                    }
                    myIntent.putExtra("from", uId);
                    startActivity(myIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnConfirmTransaction:
                setStatus("confirmed", spinRequestsWaiting.getSelectedItem().toString());
                myIntent = new Intent(getApplicationContext(), RateUser.class);
                myIntent.putExtra("tId", spinRequestsWaiting.getSelectedItem().toString());
                try {
                    for (int i = 0; i < requestJsonArray.length(); i++) {

                        if (requestJsonArray.getJSONObject(i).getString("id").equals(spinRequestsWaiting.getSelectedItem().toString())) {
                            myIntent.putExtra("request",requestJsonArray.getJSONObject(i).toString());
                        }
                    }
                    startActivity(myIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnAcceptRequest:
                setStatus("accepted", spinOpenRequests.getSelectedItem().toString());

                break;

            case R.id.btnTransSuccess:
                setStatus("waiting", spinActiveRequests.getSelectedItem().toString());
                break;
        }
    }
}
