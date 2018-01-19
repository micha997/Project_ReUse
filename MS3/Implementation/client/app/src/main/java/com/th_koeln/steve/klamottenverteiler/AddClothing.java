package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;
import com.th_koeln.steve.klamottenverteiler.services.ListViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Michael on 17.01.18.
 */

public class AddClothing extends AppCompatActivity implements View.OnClickListener {

    public static final int PICK_IMAGE = 1;
    public static final int CHOOSE_OPTION = 77;
    private int OPT = 1;
    private static int PLACE_PICKER_REQUEST;

    private ImageView imageViewPic;

    private EditText editTextTitle;

    private ListView listViewOptions;
    private ArrayList<String> clothingOptionsLevel1 = new ArrayList<String>();

    private Button btnChooseLocation;
    private FloatingActionButton actionBtnAddPic;
    private FloatingActionButton actionBtnRemovePic;
    private Button btnCreate;

    private Geocoder geocoder;

    //Fuer das gewaehlte Item
    private int choosenOption;
    private String choosenArea;

    private double latitude= 0;
    private double longitude=0;
    private String city = null;
    private String result;
    private JSONArray clothingOptions;
    private JSONArray removedSizeOptions = new JSONArray();

    private ProgressDialog progress;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final String uiD= firebaseAuth.getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_clothing_2);

        geocoder = new Geocoder(this, Locale.getDefault());

        listViewOptions = (ListView) findViewById(R.id.listViewOptions);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);

        btnChooseLocation = (Button) findViewById(R.id.btnChooseLocation);
        btnChooseLocation.setOnClickListener(this);

        btnCreate = (Button) findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(this);

        actionBtnAddPic = (FloatingActionButton) findViewById(R.id.actionBtnAddPic);
        actionBtnAddPic.setOnClickListener(this);

        actionBtnRemovePic = (FloatingActionButton) findViewById(R.id.actionBtnRemovePic);
        actionBtnRemovePic.setOnClickListener(this);

        imageViewPic = (ImageView) findViewById(R.id.imageViewPic);

        IntentFilter filter = new IntentFilter();
        filter.addAction("addclothing");
        filter.addAction("clothingOptions");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        Intent optionsIntent = new Intent(getApplicationContext(), HttpsService.class);
        optionsIntent.putExtra("method","GET");
        optionsIntent.putExtra("from","CLOTHINGOPTIONS");
        optionsIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothingOptions/");
        startService(optionsIntent);

        listViewOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showDetailActivity = new Intent(getApplicationContext(), ClothingOptionsDetail.class);
                choosenOption = i;

                try {
                    JSONObject tmpObject = new JSONObject(clothingOptions.get(i).toString());
                    JSONArray tmpArray = tmpObject.getJSONArray("options");
                    choosenArea = tmpObject.getString("topic");
                    JSONObject tmpObject2 = tmpArray.optJSONObject(0);
                    if(tmpObject2==null){OPT = 2;}else{OPT = 1;}
                    showDetailActivity.putExtra("items", tmpObject.toString());
                    showDetailActivity.putExtra("option", OPT);
                    startActivityForResult(showDetailActivity, CHOOSE_OPTION);
                }catch(JSONException e){
                    showDialog("Error", "Could not process request data!");
                }
            }
        });

        progress = new ProgressDialog(this);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PLACE_PICKER_REQUEST) {
                if (resultCode == RESULT_OK) {
                    // get Place Picker location
                    Place place = PlacePicker.getPlace(getApplicationContext(), data);
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                }
            }
            if (requestCode == PICK_IMAGE) {
                if(data!=null && resultCode == RESULT_OK) {
                    actionBtnAddPic.setVisibility(View.GONE);
                    actionBtnRemovePic.setVisibility(View.VISIBLE);

                    InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] byteData = new byte[16384];


                    while ((nRead = inputStream.read(byteData, 0, byteData.length)) != -1) {
                        buffer.write(byteData, 0, nRead);
                    }

                    buffer.flush();

                    byte imageData[] = buffer.toByteArray();
                    inputStream.read(imageData);
                    result = Base64.encodeToString(imageData, Base64.DEFAULT);
                    imageViewPic.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                }
            }

            if(requestCode == CHOOSE_OPTION){
                if(data!=null) {
                    String StringResult = data.getStringExtra("StringResult");
                    String area = data.getStringExtra("area");
                    View tmpView = getViewByPosition(choosenOption, listViewOptions);
                    TextView selectionTextView = (TextView) tmpView.findViewById(R.id.selectionTextView);
                    selectionTextView.setText(StringResult);
                    selectionTextView.setVisibility(View.VISIBLE);

                    /*
                    if(choosenArea.equals("Art")) {

                        switch (area) {
                            case "Kopfbedeckung":

                                break;

                            case "Oberbekleidung":
                                break;

                            case "Unterbekleidung":
                                break;

                            case "Fußbekleidung":
                                break;

                            case "Accessoires":

                                break;
                        }
                    }
                    */
                }
            }

        } catch (IOException e) {
            showDialog("Error", "Could not get your location.");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnChooseLocation:
                try {
                    Intent intent = new PlacePicker.IntentBuilder().build(AddClothing.this);
                    startActivityForResult(intent, AddClothing.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    new AlertDialog.Builder(AddClothing.this)
                            .setTitle("Error")
                            .setMessage("Connection to maps failed!")
                            .setCancelable(false)
                            .show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    new AlertDialog.Builder(AddClothing.this)
                            .setTitle("Error")
                            .setMessage("Connection to maps failed!")
                            .setCancelable(false)
                            .show();
                }
                break;

            case R.id.btnCreate:

                String art="", gender="" , size="", style="", color="", fabric="", brand="";

                for(int i=0;listViewOptions.getAdapter().getCount()>i;i++){
                    View tmpView = getViewByPosition(i,listViewOptions);
                    TextView nameTextView = (TextView) tmpView.findViewById(R.id.nameTextView);
                    TextView selectionTextView = (TextView) tmpView.findViewById(R.id.selectionTextView);
                    String tmpTopic = nameTextView.getText().toString();
                    String tmpSelection = selectionTextView.getText().toString();

                    switch(tmpTopic){
                        case "Art":
                            art = tmpSelection;
                            break;
                        case "Gender":
                            gender = tmpSelection;
                            break;
                        case "Size":
                            size = tmpSelection;
                            break;
                        case "Style":
                            style = tmpSelection;
                            break;
                        case "Color":
                            color = tmpSelection;
                            break;
                        case "Fabric":
                            fabric = tmpSelection;
                            break;
                        case "Brand":
                            brand = tmpSelection;
                            break;
                    }
                }
                String notes = editTextTitle.getText().toString();

                if(art != "" && !art.isEmpty() && size!= "" && !size.isEmpty() && longitude != 0 && latitude != 0 ) {
                    // build JSON object for clothing post
                    JSONObject kleidung = new JSONObject();
                    try {
                        kleidung.put("size",size);
                        kleidung.put("art",art);
                        kleidung.put("style",style);
                        kleidung.put("gender",gender);
                        kleidung.put("colours", color);
                        kleidung.put("brand", brand);
                        kleidung.put("fabric", fabric);
                        kleidung.put("notes", notes);
                        kleidung.put("longitude", longitude);
                        kleidung.put("latitude",latitude);
                        kleidung.put("city",city);
                        kleidung.put("image",result);
                        kleidung.put("uId", uiD);
                        // define http service call
                        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                        // define parameters for Service-Call
                        myIntent.putExtra("payload",kleidung.toString());
                        myIntent.putExtra("method","POST");
                        myIntent.putExtra("from","ADDCLOTHING");
                        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/klamotten");
                        //call http service
                        progress.setTitle("Please wait!");
                        progress.setMessage("Trying to add clothing..");
                        progress.show();
                        //startService(myIntent);
                    } catch (JSONException e) {
                        showDialog("Error", "Could not add clothing.");
                    }
                } else {
                    showDialog("Error", "Some of the following values missing: \n Art \n Size \n Location");
                }
                break;

            case R.id.actionBtnAddPic:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                break;

            case R.id.actionBtnRemovePic:
                result = null;
                actionBtnAddPic.setVisibility(View.VISIBLE);
                actionBtnRemovePic.setVisibility(View.GONE);
                imageViewPic.setImageDrawable(null);
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getStringExtra("from").equals("ADDCLOTHING")) {
                String success = intent.getStringExtra("success");

                progress.dismiss();
                if (success.equals("1")) {
                    showDialog("Success!", "Successfully added clothing!");
                } else {
                    showDialog("Error!", "Failed to add clothing!");
                }
            }

            if(intent.getStringExtra("from").equals("CLOTHINGOPTIONS")){
                String rawData = intent.getStringExtra("optionsData");
                try {
                    clothingOptions = new JSONArray(rawData);
                    for(int i=0;clothingOptions.length()>i;i++){
                        JSONObject tmpObject = new JSONObject(clothingOptions.get(i).toString());
                        clothingOptionsLevel1.add(tmpObject.getString("topic"));
                    }
                    fillListView(clothingOptionsLevel1);
                }catch(JSONException e){
                    showDialog("Error", "Could not process request data!");
                }
            }

        }
    };

    private void fillListView(ArrayList<String> options) {
        ClothingOptionsAdapter optAdapter;
        optAdapter = new ClothingOptionsAdapter(this, options);
        listViewOptions.setAdapter(optAdapter);
        ListViewHelper.getListViewSize(listViewOptions);
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(AddClothing.this).create();
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

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}