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
import android.widget.Button;

import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

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
            String from = intent.getStringExtra("from");
            if (from.equals("SEARCHOUTFITFAIL")) {
                AlertDialog alertDialog = new AlertDialog.Builder(ChooseContext.this).create();
                alertDialog.setTitle("Error!");
                alertDialog.setMessage("Could not get Outfits!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                String outfit = intent.getStringExtra("clothing");
                Intent showClothing = new Intent(getApplicationContext(), ShowOutfit.class);
                showClothing.putExtra("outfit", outfit);
                startActivity(showClothing);
            }
        }
    };


}
