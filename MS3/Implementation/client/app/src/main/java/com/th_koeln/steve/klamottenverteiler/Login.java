package com.th_koeln.steve.klamottenverteiler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.UploadTask;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by steve on 05.11.17.
 */

public class Login extends AppCompatActivity implements View.OnClickListener {
    private TextView etPasswordLogin;
    private TextView etEmailLogin;
    private TextView tvLogin;
    private String uID;
    private Button btnLogin;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPasswordLogin = (EditText) findViewById(R.id.etPasswordLogin);
        etEmailLogin = (EditText) findViewById(R.id.etEmailLogin);

        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(this);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), UserInterface.class));
        }
        progressDialog = new ProgressDialog(this);

    }

    private void userLogin() {
        String email = etEmailLogin.getText().toString().trim();
        String password = etPasswordLogin.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Bitte Email-Adresse eingeben.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Bitte Passwort eingeben.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Benutzer wird registriert");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();

                if(!task.isSuccessful()) {
                    startActivity(new Intent(getApplicationContext(), UserInterface.class));
                } else {
                    checkVerified();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == btnLogin) {
            userLogin();
        } else if (view == tvLogin) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void checkVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified()) {
            sendTokenToServer();
            finish();
            Toast.makeText(this, "You are now logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(),UserInterface.class));
        } else {
            Toast.makeText(getApplicationContext(), "Email is not verified", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
        }
    }

    private void sendTokenToServer() {
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = FirebaseInstanceId.getInstance().getToken();
                            uID = mUser.getUid();

                            Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                            JSONObject token = new JSONObject();
                            try {
                                token.put("token",idToken);
                                myIntent.putExtra("payload",token.toString());
                                myIntent.putExtra("method","POST");
                                myIntent.putExtra("from", "NEW_TOKEN");
                                myIntent.putExtra("url",getString(R.string.DOMAIN) + "/users/"+ uID + "/token/" + idToken);
                                //call http service
                                startService(myIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
}
