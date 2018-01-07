package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.th_koeln.steve.klamottenverteiler.R;

import java.util.Map;

/**
 * Created by Frank on 07.01.2018.
 */

public class ShowRequest extends AppCompatActivity {
    private TextView txtShowRequests;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_show_requests);



        txtShowRequests = (TextView) findViewById(R.id.txtShowRequests);
        txtShowRequests.setText(intent.getStringExtra("uId")  + " möchte " + intent.getStringExtra("cId"));
    }
}
