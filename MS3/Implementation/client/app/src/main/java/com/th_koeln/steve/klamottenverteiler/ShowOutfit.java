package com.th_koeln.steve.klamottenverteiler;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Frank on 02.01.2018.
 */

public class ShowOutfit extends AppCompatActivity {

    private TextView txtShowOutfit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_outfit);
        txtShowOutfit = (TextView) findViewById(R.id.txtShowOutfit);
        txtShowOutfit.setText("");

        try {
            String outfit= getIntent().getStringExtra("outfit");
            JSONObject outfitsArray = new JSONObject(outfit);
            JSONArray layers = outfitsArray.getJSONArray("layers");

            for (int i = 0; i < layers.length(); i++) {
                txtShowOutfit.append(layers.getString(i) + ":" + outfitsArray.getString(layers.getString(i)) +  "\n \n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
