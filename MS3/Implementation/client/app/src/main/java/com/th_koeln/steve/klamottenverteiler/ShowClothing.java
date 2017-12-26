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
import android.widget.ImageView;
import android.widget.TextView;

import com.th_koeln.steve.klamottenverteiler.R;

/**
 * Created by Frank on 25.12.2017.
 */

public class ShowClothing extends AppCompatActivity {

    private ImageView imgClothingDetails;
    private TextView txtClothing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showclothing);
        txtClothing = (TextView) findViewById(R.id.txtClothing);
        imgClothingDetails = (ImageView) findViewById(R.id.imgCLothingDetail);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showdetails"));


    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String clothinglist = intent.getStringExtra("clothing");
            txtClothing.setText(clothinglist);
        }
    };
}

