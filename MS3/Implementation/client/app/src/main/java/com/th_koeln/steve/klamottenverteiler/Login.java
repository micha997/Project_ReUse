package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
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
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
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

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, UserInterface.class));
            finish();
        }
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

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("login"));

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
                    try {
                        throw task.getException();
                    } catch (FirebaseNetworkException e) {
                        Toast.makeText(Login.this, "Error! - Can't connect to login service. Is internet available?",
                                Toast.LENGTH_LONG).show();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(Login.this, "Error! - Account not available or wrong password!",
                                Toast.LENGTH_LONG).show();
                    } catch (FirebaseAuthInvalidUserException e) {
                        Toast.makeText(Login.this, "Error! - Account not available or wrong password!",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(Login.this, "Error! - Can not login.",
                                Toast.LENGTH_LONG).show();
                    }
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
            finish();
        }

    }

    private void checkVerified() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified()) {

            sendTokenToServer();
            Toast.makeText(this, "You are now logged in", Toast.LENGTH_SHORT).show();
            Intent in = new Intent(getApplicationContext(),UserInterface.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(in);
            finish();

        } else {

            Toast.makeText(getApplicationContext(), "Email is not verified", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();

        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String from = intent.getStringExtra("from");
            if (from.equals("POSTTOKENFAIL")) {
                showDialog("Error","Could not add Token!");
            }

        }
    };

    private void sendTokenToServer() {
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        try {
            mUser.getToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

                        public void onComplete(@NonNull Task<GetTokenResult> task) {

                            if (task.isSuccessful()) {

                                String idToken = FirebaseInstanceId.getInstance().getToken();
                                uID = mUser.getUid();

                                try {
                                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                                    JSONObject token = new JSONObject();
                                    token.put("token", idToken);
                                    myIntent.putExtra("payload", token.toString());
                                    myIntent.putExtra("method", "POST");
                                    myIntent.putExtra("from", "NEW_TOKEN");
                                    myIntent.putExtra("url", getString(R.string.DOMAIN) + "/users/" + uID + "/token/" + idToken);
                                    //call http service
                                    startService(myIntent);

                                } catch (JSONException e) {

                                    showDialog("Error", "Could not send your usertoken!");
                                    ;

                                }
                            }
                        }
                    });
        } catch (NullPointerException e) {
            showDialog("Error", "Could not send usertoken to server!");
        }
    }
    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(Login.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
