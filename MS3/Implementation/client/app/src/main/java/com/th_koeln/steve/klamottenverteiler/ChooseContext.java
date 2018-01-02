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
import android.widget.Button;

import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Frank on 25.12.2017.
 */

public class ChooseContext extends AppCompatActivity {

    private Button btnWinterOutfit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_context);

        btnWinterOutfit = (Button) findViewById(R.id.btnWinterOutfit);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showoutfit"));

        btnWinterOutfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                myIntent.putExtra("method","GET");
                myIntent.putExtra("from","SEARCHOUTFIT");
                myIntent.putExtra("url",getString(R.string.DOMAIN) +"/outfit/"+ "winter");
                startService(myIntent);
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String outfit = intent.getStringExtra("clothing");
            Intent showClothing = new Intent(getApplicationContext(),ShowOutfit.class);
            showClothing.putExtra("outfit", outfit);
            startActivity(showClothing);
            finish();
        }
    };
}
