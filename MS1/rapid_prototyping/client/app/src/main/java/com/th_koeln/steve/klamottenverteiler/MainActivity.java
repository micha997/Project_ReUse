package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btnKlamottenAnlegen;
    private Button btnKlamottenSuchen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnKlamottenAnlegen = (Button) findViewById(R.id.btnKlamottenAnlegen);
        btnKlamottenSuchen = (Button) findViewById(R.id.btnKlamottenSuchen);

        btnKlamottenAnlegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), KlamottenAnlegen.class);
                startActivity(myIntent);
            }
        });

        btnKlamottenSuchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), KlamottenSuchen.class);
                startActivity(myIntent);
            }
        });
    }
}
