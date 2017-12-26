package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

public class UserInterface extends AppCompatActivity {
    private Button btnKlamottenAnlegen;
    private Button btnKlamottenSuchen;
    private Button btnLogout;
    private Button btnShowPrefer;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);
        firebaseAuth=FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, Login.class));
        }
        btnShowPrefer = (Button) findViewById(R.id.btnShowPrefer);
        btnKlamottenAnlegen = (Button) findViewById(R.id.btnKlamottenAnlegen);
        btnKlamottenSuchen = (Button) findViewById(R.id.btnKlamottenSuchen);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        btnShowPrefer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), ShowPrefer.class);
                startActivity(myIntent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mUser = FirebaseAuth.getInstance();
                String uID = mUser.getCurrentUser().getUid();
                Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                myIntent.putExtra("payload","");
                myIntent.putExtra("method","DELETE");
                myIntent.putExtra("from", "DELETETOKEN");
                myIntent.putExtra("url",getString(R.string.DOMAIN) + "/users/"+ uID + "/token");
                //call http service
                startService(myIntent);

                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

        btnKlamottenAnlegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), AddClothing.class);
                startActivity(myIntent);
            }
        });

        btnKlamottenSuchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), SearchClothing.class);
                startActivity(myIntent);
            }
        });
    }
}
