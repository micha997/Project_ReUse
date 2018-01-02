package com.th_koeln.steve.klamottenverteiler;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Frank on 02.01.2018.
 */

public class ShowOutfit extends AppCompatActivity {

    private TextView txtShowOutfit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_outfit);
        String outfit =getIntent().getStringExtra("outfit");
        txtShowOutfit = (TextView) findViewById(R.id.txtShowOutfit);
        txtShowOutfit.setText(outfit);
    }
}
