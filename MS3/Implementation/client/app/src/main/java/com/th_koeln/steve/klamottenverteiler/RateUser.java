package com.th_koeln.steve.klamottenverteiler;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Frank on 10.01.2018.
 */

public class RateUser extends AppCompatActivity {

    private EditText txtComment;
    private Spinner spinChooseRating;
    private Button btnSendRating;
    private TextView txtTransactionDetails;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        txtComment = (EditText) findViewById(R.id.txtComment);
        spinChooseRating = (Spinner) findViewById(R.id.spinChooseRating);
        btnSendRating = (Button) findViewById(R.id.btnSendRating);
        txtTransactionDetails = (TextView) findViewById(R.id.txtTransactionDetails);
        ArrayAdapter<String> aArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
        aArrayAdapter.add("Good");
        aArrayAdapter.add("Okay");
        aArrayAdapter.add("Bad");
        spinChooseRating.setAdapter(aArrayAdapter);

        String tId = getIntent().getStringExtra("tId");
        final String request = getIntent().getStringExtra("request");
        txtTransactionDetails.append(tId);
        txtTransactionDetails.append(request);

        btnSendRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date now = new Date();

                    String strDate = sdfDate.format(now);
                    Date date = sdfDate.parse(strDate);
                    JSONObject rating = new JSONObject(request);
                    rating.put("choice", spinChooseRating.getSelectedItem().toString());
                    rating.put("comment", txtComment.getText().toString());
                    rating.put("time",date.getTime() );
                    String ouId=null;
                    if (uId.equals(rating.getString("ouId"))) {
                       ouId = rating.getString("uId");
                    } else {
                       ouId = rating.getString("ouId");
                    }
                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    myIntent.putExtra("payload",rating.toString());
                    myIntent.putExtra("method","POST");
                    myIntent.putExtra("from","POSTRATING");
                    myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + ouId + "/rating");
                    startService(myIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
