package com.th_koeln.steve.klamottenverteiler;

import android.content.BroadcastReceiver;
import android.content.Context;
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

public class ShowRequest extends AppCompatActivity {
    private TextView txtShowMyRequests;
    private Spinner spinAcceptRequest;
    private ArrayList<String> ids = new ArrayList();
    private ArrayList<String> activeRequests = new ArrayList();
    private ArrayList<String> requestsWaiting = new ArrayList();
    private ArrayAdapter<String> requestAdapter;
    private Button btnAcceptRequest;
    private Button btnStartChat;
    private String requests;
    private JSONArray requestJsonArray;
    private TextView txtShowForeignRequests;
    private Spinner spinActiveRequests;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();
    private Button btnTransSuccess;
    private Spinner spinRequestsWaiting;
    private Button btnConfirmTransaction;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_requests);
        btnAcceptRequest = (Button) findViewById(R.id.btnAcceptRequest);
        btnStartChat = (Button) findViewById(R.id.btnStartChat);
        txtShowMyRequests = (TextView) findViewById(R.id.txtShowMyRequests);
        txtShowForeignRequests = (TextView) findViewById(R.id.txtShowForeignRequests);
        spinAcceptRequest = (Spinner) findViewById(R.id.spinAcceptRequest);
        spinActiveRequests = (Spinner) findViewById(R.id.spinActiveRequests);
        btnTransSuccess = (Button) findViewById(R.id.btnTransSuccess);
        spinRequestsWaiting = (Spinner) findViewById(R.id.spinRequestsWaiting);
        btnConfirmTransaction = (Button) findViewById(R.id.btnConfirmTransaction);


        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showrequests"));

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SHOWREQUESTS");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests");
        startService(myIntent);

        btnTransSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStatus("waiting", spinAcceptRequest.getSelectedItem().toString());
            }
        });

        btnAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStatus("accepted", spinActiveRequests.getSelectedItem().toString());
            }
        });

        btnConfirmTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStatus("confirmed", spinRequestsWaiting.getSelectedItem().toString());
                Intent myIntent = new Intent(getApplicationContext(), RateUser.class);
                startActivity(myIntent);

            }
        });

        btnStartChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(),Chat.class);


                myIntent.putExtra("rId", spinActiveRequests.getSelectedItem().toString());
                for (int i = 0; i < requestJsonArray.length(); i++) {

                    try {
                        if (requestJsonArray.getJSONObject(i).getString("id").equals(spinActiveRequests.getSelectedItem().toString())) {
                            if (uId.equals(requestJsonArray.getJSONObject(i).getString("uId"))) {
                                myIntent.putExtra("to", requestJsonArray.getJSONObject(i).getString("ouId"));
                            } else {
                                myIntent.putExtra("to", requestJsonArray.getJSONObject(i).getString("uId"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                myIntent.putExtra("from", uId);
                startActivity(myIntent);

            }
        });

    }

    private JSONObject setStatus(String status, String spin) {
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);

        try {
            for (int i = 0; i < requestJsonArray.length(); i++) {

                if (requestJsonArray.getJSONObject(i).getString("id").equals(spin)) {
                    JSONObject putRequest = requestJsonArray.getJSONObject(i);
                    putRequest.put("status", status);
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
            String requests = intent.getStringExtra("clothing");
            try {
                requestJsonArray = new JSONArray(requests);
                for (int i = 0; i < requestJsonArray.length(); i++) {
                    JSONObject requestJsonObject = requestJsonArray.getJSONObject(i);
                    if ( requestJsonObject.getString("from").equals("own")) {
                        txtShowMyRequests.append(requestJsonObject.toString());

                    } else if (requestJsonObject.getString("from").equals("foreign")) {
                        txtShowForeignRequests.append(requestJsonObject.toString());
                        if (requestJsonObject.getString("status").equals("open"))
                        ids.add(requestJsonObject.getString("id").toString());
                    }
                    if (requestJsonObject.getString("status").equals("accepted"))
                        activeRequests.add(requestJsonObject.getString("id").toString());
                    if (requestJsonObject.getString("status").equals("waiting"))
                        requestsWaiting.add(requestJsonObject.getString("id").toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            requestAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, activeRequests);
            spinActiveRequests.setAdapter(requestAdapter);

            requestAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, ids);
            spinAcceptRequest.setAdapter(requestAdapter);

            requestAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, requestsWaiting);
            spinRequestsWaiting.setAdapter(requestAdapter);
        }
    };
}
