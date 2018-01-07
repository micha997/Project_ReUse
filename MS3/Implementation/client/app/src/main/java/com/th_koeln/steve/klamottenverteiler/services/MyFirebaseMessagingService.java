package com.th_koeln.steve.klamottenverteiler.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.th_koeln.steve.klamottenverteiler.MainActivity;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.ShowRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Frank on 06.01.2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";


    public void onMessageReceived(RemoteMessage remoteMessage) {

        showNotification(remoteMessage);
    }

    private void showNotification(RemoteMessage message) {


        Intent myIntent = new Intent(getApplicationContext(),ShowRequest.class);
        Map<String, String> params = message.getData();
        JSONObject paramsJson = new JSONObject(params);

        try {
            myIntent.putExtra("cId", paramsJson.getString("cId"));
            myIntent.putExtra("uId", paramsJson.getString("ouId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        myIntent.putExtra("from", "showNotification");

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, myIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("Klamotten-Verteiler")
                .setContentText(message.getData().toString())
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());
    }


}