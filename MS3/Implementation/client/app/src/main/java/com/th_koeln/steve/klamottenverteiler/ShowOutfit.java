package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.adapter.ClothingOfferAdapter;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;
import com.th_koeln.steve.klamottenverteiler.structures.ClothingOffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Steffen Owtschinnikow on 02.01.2018.
 */

public class ShowOutfit extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView headRecycler, layer1Recycler,
            layer2Recycler, layer3Recycler,
            bottomRecycler, shoesRecycler;
    private Spinner spinnerMissingClothing;

    private ArrayList<ClothingOffer> HeadForAdapter = new ArrayList<>(), Layer1ForAdapter = new ArrayList(),
            Layer2ForAdapter = new ArrayList(), Layer3ForAdapter = new ArrayList(),
            BottomForAdapter = new ArrayList(), ShoesForAdapter = new ArrayList();

    private ArrayList<String> miss = new ArrayList();
    private ArrayAdapter<String> missingAdapter;

    private Toolbar addOutfitTB;
    private Button btnSendClothingRequest;
    private Button btnSubscribeMissingClothing;

    private ProgressDialog progressDialog;
    private String model;
    private int index = 0;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId = firebaseAuth.getCurrentUser().getUid();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_outfit_2);

        //RecyclerView fuer die Kopfbedeckungen
        headRecycler = (RecyclerView) findViewById(R.id.headRecycler);
        headRecycler.setHasFixedSize(true);
        headRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper0 = new PagerSnapHelper();
        helper0.attachToRecyclerView(headRecycler);

        //RecyclerView fuer die erste Oberkoerperschicht
        layer1Recycler = (RecyclerView) findViewById(R.id.layer1Recycler);
        layer1Recycler.setHasFixedSize(true);
        layer1Recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper1 = new PagerSnapHelper();
        helper1.attachToRecyclerView(layer1Recycler);

        //RecyclerView fuer die zweite Oberkoerperschicht
        layer2Recycler = (RecyclerView) findViewById(R.id.layer2Recycler);
        layer2Recycler.setHasFixedSize(true);
        layer2Recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper2 = new PagerSnapHelper();
        helper2.attachToRecyclerView(layer2Recycler);

        //RecyclerView fuer die zweite Oberkoerperschicht
        layer3Recycler = (RecyclerView) findViewById(R.id.layer3Recycler);
        layer3Recycler.setHasFixedSize(true);
        layer3Recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper3 = new PagerSnapHelper();
        helper3.attachToRecyclerView(layer3Recycler);

        //RecyclerView fuer die zweite Oberkoerperschicht
        bottomRecycler = (RecyclerView) findViewById(R.id.bottomRecycler);
        bottomRecycler.setHasFixedSize(true);
        bottomRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper4 = new PagerSnapHelper();
        helper4.attachToRecyclerView(bottomRecycler);

        //RecyclerView fuer die zweite Oberkoerperschicht
        shoesRecycler = (RecyclerView) findViewById(R.id.shoesRecycler);
        shoesRecycler.setHasFixedSize(true);
        shoesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper helper5 = new PagerSnapHelper();
        helper5.attachToRecyclerView(shoesRecycler);


        spinnerMissingClothing = (Spinner) findViewById(R.id.spinnerMissingClothing);

        btnSendClothingRequest = (Button) findViewById(R.id.btnSendClothingRequest);
        btnSendClothingRequest.setOnClickListener(this);
        btnSubscribeMissingClothing = (Button) findViewById(R.id.btnSubscribeMissingClothing);
        btnSubscribeMissingClothing.setOnClickListener(this);

        addOutfitTB = (Toolbar) findViewById(R.id.addOutfitTB);
        setSupportActionBar(addOutfitTB);

        IntentFilter filter = new IntentFilter();
        filter.addAction("getClothingDetail2");
        filter.addAction("showoutfit");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,filter);
    }

    public void getDetailedData(String outfit){
        HeadForAdapter.clear();
        Layer1ForAdapter.clear();
        Layer2ForAdapter.clear();
        Layer3ForAdapter.clear();
        BottomForAdapter.clear();
        ShoesForAdapter.clear();
        miss.clear();
        try {
            JSONObject outfitsArray = new JSONObject(outfit);
            JSONArray layers = outfitsArray.getJSONArray("layers");
            model = outfitsArray.getString("model");
            String objects;
            for(int ilayer = 0; ilayer < layers.length(); ilayer++)
            {
                objects = outfitsArray.getString((String) layers.get(ilayer));
                if (objects.equals("[]")) {
                    miss.add((String) layers.get(ilayer));
                } else {
                    switch (ilayer){
                        case 0: JSONArray headArray = outfitsArray.getJSONArray("head");
                            for (int j=0;headArray.length()>j;j++){
                                sendClothingRequest((String) headArray.get(j),"head");
                            }
                            break;
                        case 1: JSONArray layer1Array = outfitsArray.getJSONArray("layer1");
                            for (int j=0;layer1Array.length()>j;j++){
                                sendClothingRequest((String) layer1Array.get(j),"layer1");
                            }
                            break;
                        case 2: JSONArray layer2Array = outfitsArray.getJSONArray("layer2");
                            for (int j=0;layer2Array.length()>j;j++){
                                sendClothingRequest((String) layer2Array.get(j),"layer2");
                            }
                            break;
                        case 3: JSONArray layer3Array = outfitsArray.getJSONArray("layer3");
                            for (int j=0;layer3Array.length()>j;j++){
                                sendClothingRequest((String) layer3Array.get(j),"layer3");
                            }
                            break;
                        case 4: JSONArray bottomArray = outfitsArray.getJSONArray("bottom");
                            for (int j=0;bottomArray.length()>j;j++){
                                sendClothingRequest((String) bottomArray.get(j),"bottom");
                            }
                            break;
                        case 5: JSONArray shoesArray = outfitsArray.getJSONArray("shoes");
                            for (int j=0;shoesArray.length()>j;j++){
                                sendClothingRequest((String) shoesArray.get(j),"shoes");
                            }
                            break;
                    }
                }
            }
            missingAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, miss);
            spinnerMissingClothing.setAdapter(missingAdapter);

        } catch (JSONException e) {
            showDialog("Error", "Could not process outfit data!");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btnSendClothingRequest:
                if(HeadForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) headRecycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(HeadForAdapter.get(position));
                }
                if(Layer1ForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) layer1Recycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(Layer1ForAdapter.get(position));
                }
                if(Layer2ForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) layer2Recycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(Layer2ForAdapter.get(position));
                }
                if(Layer3ForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) layer3Recycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(Layer3ForAdapter.get(position));
                }
                if(BottomForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) bottomRecycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(BottomForAdapter.get(position));
                }
                if(ShoesForAdapter.size() > 0){
                    LinearLayoutManager lm = (LinearLayoutManager) shoesRecycler.getLayoutManager();
                    int position = lm.findFirstCompletelyVisibleItemPosition();
                    sendRequest(ShoesForAdapter.get(position));
                }
                showDialog("Success", "Send requests to selected items!");
                break;

            case R.id.btnSubscribeMissingClothing:
                if(spinnerMissingClothing.getSelectedItem() !=null) {
                    JSONObject subscribe = new JSONObject();
                    try {
                        subscribe.put("model", model);
                        subscribe.put("missing", spinnerMissingClothing.getSelectedItem().toString());

                        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                        myIntent.putExtra("payload", subscribe.toString());
                        myIntent.putExtra("method", "POST");
                        myIntent.putExtra("from", "SUBSCRIBECLOTHING");
                        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + uId + "/search");
                        startService(myIntent);
                        showDialog("Success", "Subscribed to selected item!");
                    } catch (JSONException e) {
                        showDialog("Error", "Could not subscribe for missing clothing!");
                    }
                }
                break;
        }
    }

    public void sendClothingRequest(String clothingID, String layer){
        Intent reqIntent = new Intent(getApplicationContext(), HttpsService.class);
        reqIntent.putExtra("method","GET");
        reqIntent.putExtra("from","GETCLOTHINGDETAIL2");
        reqIntent.putExtra("layer", layer);
        reqIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothing/" + clothingID);
        startService(reqIntent);
    }

    private void sendRequest(ClothingOffer cOff){
        try {
            JSONObject request = new JSONObject();
            request.put("uId", uId);
            request.put("ouId", cOff.getuId());

            Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
            myIntent.putExtra("payload", request.toString());
            myIntent.putExtra("method", "POST");
            myIntent.putExtra("from", "NEWREQUEST");
            myIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + cOff.getId());
            startService(myIntent);
        }catch (JSONException e) {
            showDialog("Error", "Could not send Requests");
        }
    }

    private void sendOutfitRequest(String selected){
        // Outfits vom Server abrufen
        if(selected.equals("Winter")) {
            Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
            myIntent.putExtra("method", "GET");
            myIntent.putExtra("from", "SEARCHOUTFIT");
            myIntent.putExtra("url", getString(R.string.DOMAIN) + "/outfit/" + "winter");
            startService(myIntent);
            progressDialog = new ProgressDialog(ShowOutfit.this);
            progressDialog.setMessage("Trying to get outfit..");
            progressDialog.show();
        }
    }

    private void fillView(ArrayList<ClothingOffer> options, RecyclerView putRecyc) {
        ClothingOfferAdapter optAdapter;
        optAdapter = new ClothingOfferAdapter(this, options);
        putRecyc.setAdapter(optAdapter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getStringExtra("from").equals("GETCLOTHINGDETAIL2")){
                ClothingOffer tmpOffer = null;
                try {
                    JSONObject request = new JSONObject(intent.getStringExtra("clothing"));
                    String absPath = "";

                    if (!request.isNull("image")) {
                        String filename = "img" + index++;
                        FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                        byte[] decodedBytes = Base64.decode(request.getString("image"), 0);
                        outputStream.write(decodedBytes);
                        outputStream.close();
                        absPath = ShowOutfit.this.getFilesDir().getAbsolutePath() + "/" + filename;
                    }

                    tmpOffer = new ClothingOffer(request.getString("id"),request.getString("uId"),
                            request.getString("art"),request.getString("size"),request.getString("style"),
                            request.getString("gender"),"",request.getString("fabric"), request.getString("notes"),
                            request.getString("brand"),absPath,-200);

                } catch (JSONException e) {
                    showDialog("Error", "Could not process clothing data!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(tmpOffer != null) {
                    switch (intent.getStringExtra("layer")) {
                        case "head":
                            HeadForAdapter.add(tmpOffer);
                            fillView(HeadForAdapter,headRecycler);
                            break;
                        case "layer1":
                            Layer1ForAdapter.add(tmpOffer);
                            fillView(Layer1ForAdapter,layer1Recycler);
                            break;
                        case "layer2":
                            Layer2ForAdapter.add(tmpOffer);
                            fillView(Layer2ForAdapter,layer2Recycler);
                            break;
                        case "layer3":
                            Layer3ForAdapter.add(tmpOffer);
                            fillView(Layer3ForAdapter,layer3Recycler);
                            break;
                        case "bottom":
                            BottomForAdapter.add(tmpOffer);
                            fillView(BottomForAdapter,bottomRecycler);
                            break;
                        case "shoes":
                            ShoesForAdapter.add(tmpOffer);
                            fillView(ShoesForAdapter,shoesRecycler);
                            break;
                    }
                }
            }

            if (intent.getStringExtra("from").equals("SEARCHOUTFITFAIL")) {
                // Fehler beim Suchen der Outfits
                showDialog("Error!", "Could not get outfit!");
                progressDialog.dismiss();
            }
            if (intent.getStringExtra("from").equals("SEARCHOUTFIT")) {
                progressDialog.dismiss();
                // Starte Aktivität um gelieferte Kleidung anzuzeigen.
                String outfit = intent.getStringExtra("clothing");
                getDetailedData(outfit);
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.outfit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int actionID = item.getItemId();

        //Search Button
        if(actionID == R.id.action_add){
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowOutfit.this);
            final String[] tmpString = new String[]{"Winter", "Sommer", "Winter-Sport", "Herbst"};
            builder.setTitle("Choose context")
                    .setSingleChoiceItems(tmpString,0, null)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ListView lw = ((AlertDialog)dialogInterface).getListView();
                    String selected = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
                    sendOutfitRequest(selected);
                    dialogInterface.dismiss();
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
            .create()
            .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ShowOutfit.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (!isFinishing()) alertDialog.show();
    }
}