package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class UserInterface extends AppCompatActivity implements View.OnClickListener {
    private Button btnAddClothingUserInterface;
    private Button btnSearchClothingUserInterface;
    private Button btnLogout;
    private Button btnMyClothing;
   // private Button btnShowPrefer;
    private Button btnChooseContext;
    private Button btnEditProfile;
    private Button btnShowRequests;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        btnAddClothingUserInterface = (Button) findViewById(R.id.btnAddClothingUserInterface);
        btnAddClothingUserInterface.setOnClickListener(this);

        btnSearchClothingUserInterface = (Button) findViewById(R.id.btnSearchClothingUserInterface);
        btnSearchClothingUserInterface.setOnClickListener(this);

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);

        btnMyClothing = (Button) findViewById(R.id.btnMyClothing);
        btnMyClothing.setOnClickListener(this);

        btnChooseContext = (Button) findViewById(R.id.btnChooseContext);
        btnChooseContext.setOnClickListener(this);

        btnEditProfile = (Button) findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(this);

        btnShowRequests = (Button) findViewById(R.id.btnRequests);
        btnShowRequests.setOnClickListener(this);


        firebaseAuth=FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, Login.class));
        }

        //btnShowPrefer = (Button) findViewById(R.id.btnShowPrefer);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnSearchClothingUserInterface:
                startActivity(new Intent(getApplicationContext(), SearchClothing.class));
                break;

            case R.id.btnAddClothingUserInterface:
                startActivity(new Intent(getApplicationContext(), AddClothing.class));
                break;

            case R.id.btnLogout:
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                break;

            case R.id.btnChooseContext:
                startActivity(new Intent(getApplicationContext(),ChooseContext.class));
                break;

            case R.id.btnEditProfile:
                startActivity(new Intent(getApplicationContext(), EditProfile.class));
                break;

            case R.id.btnMyClothing:
                startActivity(new Intent(getApplicationContext(), MyClothing.class));
                break;

            case R.id.btnRequests:
                startActivity(new Intent(getApplicationContext(), ShowRequest.class));
                break;

        }
    }
}
