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
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Frank on 09.01.2018.
 */

public class Chat extends AppCompatActivity {
    private Button btnSendMessages;
    private EditText txtMessage;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();
    private TextView txtChat;
    public static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        btnSendMessages = (Button) findViewById(R.id.btnSendMessages);
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        txtChat = (TextView) findViewById(R.id.txtChat);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("chat"));

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","GETCONVERSATION");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/messages/" + getIntent().getStringExtra("to"));
        startService(myIntent);

        btnSendMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = txtMessage.getText().toString();
                String rId = getIntent().getStringExtra("rId");
                String to = getIntent().getStringExtra("to");
                String from = getIntent().getStringExtra("from");
                txtChat.append("You: " + text + "\n");
                JSONObject message = new JSONObject();
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date now = new Date();

                String strDate = sdfDate.format(now);
                try {
                    Date date = sdfDate.parse(strDate);
                    message.put("time",date.getTime());
                    if (from.equals(uId)) {
                        message.put("from", from);
                        message.put("to", to);
                        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + from + "/messages");
                    } else {
                        message.put("from", to);
                        message.put("to", from);
                        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + to + "/messages");
                    }
                    message.put("message",text);
                    message.put("attach","attach");

                    message.put("rId",rId);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                myIntent.putExtra("payload", message.toString());
                myIntent.putExtra("method","POST");
                myIntent.putExtra("from","POSTMESSAGE");
                startService(myIntent);


            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String params = intent.getStringExtra("params");
            String from = intent.getStringExtra("from");

            if (from.equals("newMessage")) {

                try {
                    JSONObject message= null;
                    message = new JSONObject(params);
                    txtChat.append("Partner: " + message.getString("message") + "\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (from.equals("GETCONVERSATION")) {
                try {
                    JSONArray messageArray = new JSONArray(params);
                    for (int i = 0; i < messageArray.length(); i++) {
                        if (messageArray.getJSONObject(i).getString("from").equals(uId)) {
                            txtChat.append("You: " + messageArray.getJSONObject(i).getString("message") + "\n");
                    } else {
                            txtChat.append("Partner: " + messageArray.getJSONObject(i).getString("message") + "\n");

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }
    };
}
