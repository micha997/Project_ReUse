package com.th_koeln.steve.klamottenverteiler;


/**
 * MyClothing dient als Übersicht der im Besitzt befindlichen Kleidungsstücke.
 * Die Kleidungsstücke werden vom Server geladen und dem Benutzer präsentiert.
 * Sollten Kleidungsstücke vorhanden sein, können verschiedene Operationen, wie
 * das Löschen eines Kleidungsstücks oder das Bearbeiten initialisiert werden.
 *
 * Created by Frank on 30.12.2017.
 */

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyClothing extends AppCompatActivity {

    private Spinner spinChooseClothing;
    private Button btnEditClothing;
    private String names[] = {"Red", "Blue", "Green"};
    private ArrayList<String> ids = new ArrayList();
    private String cId;

    private ArrayAdapter<String> clothingAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_clothing);
        btnEditClothing = (Button) findViewById(R.id.btnEditClothing);
        spinChooseClothing = (Spinner) findViewById(R.id.spinChooseClothing);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uId = firebaseAuth.getCurrentUser().getUid();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("myclothing"));

        // Hole Kleidung des Benutzers vom Server
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("payload", "");
        myIntent.putExtra("method", "GET");
        myIntent.putExtra("from", "MYCLOTHING");
        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + uId + "/clothing");
        //call http service
        startService(myIntent);


        spinChooseClothing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cId = spinChooseClothing.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnEditClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Starte Aktivität zum Bearbeiten der Kleidung
                Intent editIntent = new Intent(getApplicationContext(), EditClothing.class);
                editIntent.putExtra("cId",cId);
                startActivity(editIntent);
            }
        });
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service

            String from = intent.getStringExtra("from");
            if (from.equals("MYCLOTHINGFAIL")) {
                // Kleidung des Benutzers konnte nicht geholt werden
                showDialog("Error","Could not get clothing from Server!");
            } else {
                try {
                String clothing = intent.getStringExtra("clothing");
                    JSONArray clothingJsonArray = new JSONArray(clothing);
                    if (clothingJsonArray.length() > 1) {
                        for (int i = 0; i < clothingJsonArray.length(); i++) {
                            // lösche Hypermedia Element
                            if (clothingJsonArray.getJSONObject(i).has("_links")) {
                                clothingJsonArray = remove(i, clothingJsonArray);
                            }
                        }
                        for (int i = 0; i < clothingJsonArray.length(); i++) {
                            JSONObject clothingJsonObject = clothingJsonArray.getJSONObject(i);
                            ids.add(clothingJsonObject.getString("id").toString());
                        }
                        clothingAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, ids);
                        spinChooseClothing.setAdapter(clothingAdapter);
                    } else {
                        // kein Kleidungsstück vorhanden
                        showDialog("Error", "You don't have any clothing!");
                    }

                } catch (JSONException e) {
                    // Fehler beim auslesen der Kleidungsdaten
                    showDialog("Error", "Could not process obtained data!");
                }
            }

        }
    };


    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(MyClothing.this).create();
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


    /* Die folgenden zwei Funktionen dienen zum Entfernen eines JSON-Objekts auf einem Array und wurden von
    https://gist.github.com/emmgfx/0f018b5acfa3fd72b3f6 übernommen.
    Um eine hohe Kompabilität zu gewährleisten, wurde auf die Verwendung von der einfachen
    remove() Funktion verzichtet, da diese erst ab Android API19 verfügbar ist.
     */


    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }
}


