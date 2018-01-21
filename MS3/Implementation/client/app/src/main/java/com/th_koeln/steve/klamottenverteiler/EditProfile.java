package com.th_koeln.steve.klamottenverteiler;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Frank on 30.12.2017.
 */

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    private EditText etGender;
    private TextView txtShowUserProfile;

    private Button btnSendProfile;
    private Button btnTimeSend;

    private Button btnTimeFromWeekday;
    private Button btnTimeToWeekday;
    private Button btnTimeFromWeekend;
    private Button btnTimeToWeekend;

    private String txtWeekTimeBegin = "00:00";
    private String txtWeekTimeEnd = "00:00";
    private String txtWeekendTimeBegin = "00:00";
    private String txtWeekendTimeEnd = "00:00";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId = firebaseAuth.getCurrentUser().getUid();

    //Variablen fuer TimePicker
    private int DIALOG_ID = -1;
    private int hourPick;
    private int minutePick;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_2);

        etGender = (EditText) findViewById(R.id.editTextGender);
        txtShowUserProfile = (TextView) findViewById(R.id.txtShowUserProfile);

        btnSendProfile = (Button) findViewById(R.id.btnSendProfile);
        btnSendProfile.setOnClickListener(this);

        btnTimeSend = (Button) findViewById(R.id.btnTimeSend);
        btnTimeSend.setOnClickListener(this);

        btnTimeFromWeekend = (Button) findViewById(R.id.btnTimeFromWeekend);
        btnTimeFromWeekend.setOnClickListener(this);

        btnTimeToWeekend = (Button) findViewById(R.id.btnTimeToWeekend);
        btnTimeToWeekend.setOnClickListener(this);

        btnTimeFromWeekday = (Button) findViewById(R.id.btnTimeFromWeekday);
        btnTimeFromWeekday.setOnClickListener(this);

        btnTimeToWeekday = (Button) findViewById(R.id.btnTimeToWeekday);
        btnTimeToWeekday.setOnClickListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("profile"));

        // get desired vicinity in km
        // define parameters for Http-Service call
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","PROFILE");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId);
        //call http service
        startService(myIntent);
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if(id == DIALOG_ID){
            return new TimePickerDialog(EditProfile.this, kTimePickerListener, hourPick, minutePick, true);
        }
        return null;
    }

    protected TimePickerDialog.OnTimeSetListener kTimePickerListener = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {

            String showHour = i+"";
            String showMinute = i1+"";

            if(i<10){showHour = "0"+i;}
            if(i1<10){ showMinute = "0"+i1;}

            switch(DIALOG_ID){
                //TimeFromWeekday
                case 1: btnTimeFromWeekday.setText("Von "+showHour+":"+showMinute);
                    txtWeekTimeBegin = showHour+":"+showMinute;
                    break;
                //TimeToWeekday
                case 2: btnTimeToWeekday.setText("Bis "+showHour+":"+showMinute);
                    txtWeekTimeEnd = showHour+":"+showMinute;
                    break;
                //TimeFromWeekend
                case 3: btnTimeFromWeekend.setText("Von "+showHour+":"+showMinute);
                    txtWeekendTimeBegin = showHour+":"+showMinute;
                    break;
                //TimeToWeekend
                case 4: btnTimeToWeekend.setText("Bis "+showHour+":"+showMinute);
                    txtWeekendTimeEnd = showHour+":"+showMinute;
                    break;
            }
            //Toast.makeText(EditProfile.this, showHour+":"+showMinute, Toast.LENGTH_LONG).show();
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String profile = intent.getStringExtra("profile");
            try {
                JSONObject profileJson = new JSONObject(profile);
                JSONObject timeJson = new JSONObject(profileJson.getString("time"));
                etGender.setText(profileJson.getString("gender"));
                btnTimeFromWeekday.setText("Von "+timeJson.getString("txtWeekTimeBegin"));
                btnTimeToWeekday.setText("Bis "+timeJson.getString("txtWeekTimeEnd"));
                btnTimeFromWeekend.setText("Von "+timeJson.getString("txtWeekendTimeBegin"));
                btnTimeToWeekend.setText("Bis "+timeJson.getString("txtWeekendTimeEnd"));
                txtShowUserProfile.setText(profile.toString());
                //... fill user profile interface
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    public void onClick(View view) {
        Intent myIntent;
        switch (view.getId()) {

            case R.id.btnSendProfile:
                JSONObject profile = new JSONObject();
                try {
                    profile.put("gender", etGender.getText().toString());
                    // define http service call
                    myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    // define parameters for Service-Call
                    myIntent.putExtra("payload",profile.toString());
                    myIntent.putExtra("method","PUT");
                    myIntent.putExtra("from","PUTPROFILE");
                    myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId);
                    //call http service
                    startService(myIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnTimeSend:
                JSONObject times = new JSONObject();
                JSONObject newProfile = new JSONObject();
                try {
                    times.put("txtWeekTimeBegin", txtWeekTimeBegin);
                    times.put("txtWeekTimeEnd", txtWeekTimeEnd);
                    times.put("txtWeekendTimeBegin", txtWeekendTimeBegin);
                    times.put("txtWeekendTimeEnd", txtWeekendTimeEnd);
                    newProfile.put("time",times);
                    // define http service call
                    myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    // define parameters for Service-Call
                    myIntent.putExtra("payload",newProfile.toString());
                    myIntent.putExtra("method","PUT");
                    myIntent.putExtra("from","PUTPROFILE");
                    myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId);
                    //call http service
                    startService(myIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnTimeFromWeekday:
                DIALOG_ID = 1;
                showDialog(DIALOG_ID);
                break;

            case R.id.btnTimeToWeekday:
                DIALOG_ID = 2;
                showDialog(DIALOG_ID);
                break;

            case R.id.btnTimeFromWeekend:
                DIALOG_ID = 3;
                showDialog(DIALOG_ID);
                break;

            case R.id.btnTimeToWeekend:
                DIALOG_ID = 4;
                showDialog(DIALOG_ID);
                break;

        }
    }
}