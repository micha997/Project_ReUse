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
import android.text.method.ScrollingMovementMethod;
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
    private String text;
    private String rId;
    private String message_to;
    private String message_from;


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
        txtChat.setMovementMethod(new ScrollingMovementMethod());

        rId = getIntent().getStringExtra("rId");
        message_to = getIntent().getStringExtra("to");
        message_from = getIntent().getStringExtra("from");

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
                text = txtMessage.getText().toString();
                txtChat.append("You: " + text + "\n");
                JSONObject message = new JSONObject();
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date now = new Date();

                String strDate = sdfDate.format(now);
                try {
                    Date date = sdfDate.parse(strDate);
                    message.put("time",date.getTime());
                    message.put("from", message_from);
                    message.put("to", message_to);
                    if (message_from.equals(uId)) {
                        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + message_from + "/messages");
                    } else {
                        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + message_to + "/messages");
                    }
                    message.put("message",text);
                    message.put("attach","attach");

                    message.put("rId",rId);
                    myIntent.putExtra("payload", message.toString());
                    myIntent.putExtra("method","POST");
                    myIntent.putExtra("from","POSTMESSAGE");
                    startService(myIntent);
                } catch (JSONException e) {
                    showDialog("Error", "Can not proceed your entries ");
                } catch (ParseException e) {
                    showDialog("Error", "Can not proceed your entries ");
                }
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

            String from = intent.getStringExtra("from");

            if (from.equals("GETCONVERSATIONFAIL")) {
                showDialog("Error!", "Could not get Conversation!");
            } else if (from.equals("POSTMESSAGEFAIL")){
                showDialog("Error!", "Could not add message!");
            } else {
                String params = intent.getStringExtra("params");
                try {

                    if (from.equals("newMessage")) {

                        JSONObject message = new JSONObject(params);
                        txtChat.append("Partner: " + message.getString("message") + "\n");

                    } else if (from.equals("GETCONVERSATION")) {

                        JSONArray messageArray = new JSONArray(params);

                        for (int i = 0; i < messageArray.length(); i++) {

                            if (messageArray.getJSONObject(i).getString("from").equals(uId) && messageArray.getJSONObject(i).getString("to").equals(message_to)) {
                                txtChat.append("You: " + messageArray.getJSONObject(i).getString("message") + "\n");
                            } else if (messageArray.getJSONObject(i).getString("from").equals(message_to) && messageArray.getJSONObject(i).getString("to").equals(uId)) {
                                txtChat.append("Partner: " + messageArray.getJSONObject(i).getString("message") + "\n");
                            }

                        }
                    }
                } catch (JSONException e) {

                    showDialog("Error", "Can not proceed your messages");

                }
            }

        }
    };

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(Chat.this).create();
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
