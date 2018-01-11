package com.th_koeln.steve.klamottenverteiler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvGoLogin;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private boolean isFirebaseCalledOnce =false;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, UserInterface.class));
        }

        progressDialog = new ProgressDialog(this);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        etEmail = (EditText) findViewById(R.id.etEmailRegistration);
        etPassword = (EditText) findViewById(R.id.etPasswordRegister);
        tvGoLogin = (TextView) findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        tvGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null && isFirebaseCalledOnce) {
                    sendMail();
                    isFirebaseCalledOnce=false;
                }

            }
        };
        firebaseAuth.addAuthStateListener(firebaseAuthListener);

    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        isFirebaseCalledOnce=true;

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Bitte Email-Adresse eingeben.", Toast.LENGTH_SHORT).show();
            return;

        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Bitte Passwort eingeben.", Toast.LENGTH_SHORT).show();
            return;
        }

        /*progressDialog.setMessage("Benutzer wird registriert");
        progressDialog.show();*/

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
             if (task.isSuccessful()) {

                 String uId = firebaseAuth.getCurrentUser().getUid().toString();
                 Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                 // define parameters for Service-Call
                 JSONObject payload = new JSONObject();
                 try {
                     payload.put("uId", uId);
                     myIntent.putExtra("payload",payload.toString());
                     myIntent.putExtra("method","POST");
                     myIntent.putExtra("from","NEWUSER");
                     myIntent.putExtra("url",getString(R.string.DOMAIN) + "/users/");
                     //call http service
                     startService(myIntent);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             } else {
                 Toast.makeText(getApplicationContext(), "Registrierung fehlgeschlagen", Toast.LENGTH_SHORT).show();
             }
            }
        });
    }

    private void sendMail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getApplicationContext(), Login.class));
                            finish();
                        } else {
                            // no email sent
                            Toast.makeText(getApplicationContext(), "Fehler beim Senden der Email", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(getIntent());
                        }
                    }
                });
    }
}
