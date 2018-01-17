package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Frank on 07.01.2018.
 */

public class ShowRequest extends AppCompatActivity implements View.OnClickListener  {
    private JSONArray requestJsonArray;
    private RadioButton rbOwnRequests;
    private RadioButton rbForeignRequests;
    private ArrayList<Request> foreignRequestList = new ArrayList<>();
    private ArrayList<Request> ownRequestList = new ArrayList<>();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();
    private ListView lvShowRequests;
    private boolean menu_first;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_requests);


        lvShowRequests = (ListView) findViewById(R.id.lvShowRequests);

        rbOwnRequests = (RadioButton) findViewById(R.id.rbOwnRequest);
        rbOwnRequests.setOnClickListener(this);

        rbForeignRequests = (RadioButton) findViewById(R.id.rbForeignRequest);
        rbForeignRequests.setOnClickListener(this);
        registerForContextMenu(lvShowRequests);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showrequests"));

        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SHOWREQUESTS");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests");
        startService(myIntent);

        lvShowRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.showContextMenu();
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lvShowRequests) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Request obj = (Request) lv.getItemAtPosition(acmi.position);
            if (obj.getStatus().equals("open") && obj.getFrom().equals("foreign")) {
                menu.add("Accept");
                menu.add("Decline");
            } else if (obj.getStatus().equals("open") && obj.getFrom().equals("own")) {
                menu.add("Delete");
            } else if (obj.getStatus().equals("accepted") && obj.getFrom().equals("own")) {
                menu.add("Chat");
                menu.add("Delete");
                menu.add("Success");
            } else if (obj.getStatus().equals("accepted") && obj.getFrom().equals("foreign")) {
                menu.add("Chat");
                menu.add("Decline");
                menu.add("Success");
            } else if (obj.getStatus().equals("accepted") && obj.getFrom().equals("foreign")) {
                menu.add("Chat");
                menu.add("Decline");
                menu.add("Success");
            } else if (obj.getStatus().equals("waiting") && !obj.getConfirmed().equals(uId)) {
                menu.add("Chat");
                menu.add("Confirm");
            } else if (obj.getStatus().equals("waiting") && obj.getConfirmed().equals(uId)) {
                menu.add("Chat");
                menu.add("Delete");
            } else if (obj.getStatus().equals("confirmed")) {
                menu.add("Chat");
                menu.add("Delete");
                menu.add("Rate User");
            }
            for (int i = 0; i < menu.size(); ++i) {
                MenuItem item = menu.getItem(i);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        onContextItemSelected(item);
                        return true;
                    }
                });
            }
            menu_first=true;
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (menu_first) {
            menu_first=false;
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            if (info.targetView.getParent() != findViewById(R.id.lvShowRequests))
                return super.onContextItemSelected(item);

            String title = (String) item.getTitle();
            int menuItemIndex = info.position;
            if (rbForeignRequests.isChecked()) {
                Request menuItems = foreignRequestList.get(menuItemIndex);
                String name = menuItems.getName();
                if (title.equals("Decline")) {
                    sendDelete(menuItems.getOuId(), name);
                }
                if (title.equals("Delete")) {
                    sendDelete(menuItems.getOuId(), name);
                }
                if (title.equals("Accept")) {
                    setStatus("accepted", name);
                }
                if (title.equals("Chat")) {
                    Intent myIntent = new Intent(getApplicationContext(), Chat.class);
                    myIntent.putExtra("rId", menuItems.getName());
                    myIntent.putExtra("to", menuItems.getOuId());
                    myIntent.putExtra("from", uId);
                    startActivity(myIntent);
                }
                if (title.equals("Success")) {
                    setStatus("waiting", menuItems.getName());
                }
                if (title.equals("Confirm")) {
                    setStatus("confirmed", name);
                }
                if (title.equals("Rate User")) {
                    Intent myIntent;
                    myIntent = new Intent(getApplicationContext(), RateUser.class);
                    myIntent.putExtra("tId", menuItems.getName());
                    myIntent.putExtra("ouId", menuItems.getOuId());
                    startActivity(myIntent);
                }
            } else {
                Request menuItems = (Request) ownRequestList.get(menuItemIndex);
                String name = menuItems.getName();
                if (title.equals("Delete")) {
                    sendDelete(uId, name);
                }
                if (title.equals("Chat")) {
                    Intent myIntent = new Intent(getApplicationContext(), Chat.class);
                    myIntent.putExtra("rId", menuItems.getName());
                    myIntent.putExtra("to", menuItems.getOuId());
                    myIntent.putExtra("from", uId);
                    startActivity(myIntent);
                }
                if (title.equals("Success")) {
                    setStatus("waiting", menuItems.getName());
                }
                if (title.equals("Confirm")) {
                    setStatus("confirmed", name);
                }
                if (title.equals("Rate User")) {
                    Intent myIntent;
                    myIntent = new Intent(getApplicationContext(), RateUser.class);
                    myIntent.putExtra("tId", menuItems.getName());
                    myIntent.putExtra("ouId", menuItems.getOuId());
                    startActivity(myIntent);
                }
            }

        }
        return true;
    }

    private JSONObject setStatus(String status, String spin) {
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);

        try {
            for (int i = 0; i < requestJsonArray.length(); i++) {

                if (requestJsonArray.getJSONObject(i).getString("id").equals(spin)) {
                    JSONObject putRequest = requestJsonArray.getJSONObject(i);
                    putRequest.put("status", status);

                    if(status.equals("waiting")) {
                        putRequest.put("confirmed", uId);
                    } else {
                        putRequest.put("confirmed", 0);
                    }
                    myIntent.putExtra("payload",putRequest.toString());
                }
            }
            myIntent.putExtra("method","PUT");
            myIntent.putExtra("from","PUTREQUEST");
            myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests/" + spin);
            startService(myIntent);
        } catch (JSONException e1) {
            showDialog("Error", "Could not set status of clothing!");
        }

        return null;
        }

        private void sendDelete(String uId, String id) {
            Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
            myIntent.putExtra("method","DELETE");
            myIntent.putExtra("from","DELETEREQUEST");
            myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests/" + id);
            startService(myIntent);
        }


        private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String success = intent.getStringExtra("success");
            if (success.equals("1")) {
                showDialog("Success!", "Successfully edited status of request!");
            } else if(success.equals("2")) {
                showDialog("Success!", "Successfully deleted request!");
            } else {

                String from = intent.getStringExtra("from");
                if (from.equals("SHOWREQUESTSFAIL")) {
                    showDialog("Error", "Could not get request!");
                } else {

                    String requests = intent.getStringExtra("clothing");

                    try {

                        requestJsonArray = new JSONArray(requests);
                        for (int i = 0; i < requestJsonArray.length(); i++) {
                            JSONObject requestJsonObject = requestJsonArray.getJSONObject(i);
                            if (requestJsonObject.getString("from").equals("own")) {
                                Request ownnRequest= new Request(requestJsonObject.getString("id").toString(),
                                        requestJsonObject.getString("art").toString(),requestJsonObject.getString("size").toString(),
                                        requestJsonObject.getString("brand").toString(),requestJsonObject.getString("status").toString(),requestJsonObject.getString("from").toString(), requestJsonObject.getString("ouId"),requestJsonObject.getString("confirmed"));
                                ownRequestList.add(ownnRequest);

                            } else if (requestJsonObject.getString("from").equals("foreign")) {
                                Request foreignRequest= new Request(requestJsonObject.getString("id").toString(),
                                        requestJsonObject.getString("art").toString(),requestJsonObject.getString("size").toString(),
                                        requestJsonObject.getString("brand").toString(),requestJsonObject.getString("status").toString(),requestJsonObject.getString("from").toString(), requestJsonObject.getString("uId"),requestJsonObject.getString("confirmed"));
                                foreignRequestList.add(foreignRequest);
                            }

                        }

                        if (rbForeignRequests.isChecked()) {
                            fillListView(foreignRequestList);

                        } else {
                            fillListView(ownRequestList);
                        }

                    } catch (JSONException e) {
                        showDialog("Error", "Could not process request data!");
                    }

                }
            }
        }
    };

    private void fillListView(ArrayList<Request> requests) {
        RequestListAdapter reqAdapter;
        reqAdapter = new RequestListAdapter(getApplicationContext(), R.layout.list_requests,requests );
        lvShowRequests.setAdapter(reqAdapter);

    }


    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ShowRequest.this).create();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rbOwnRequest:
                fillListView(ownRequestList);
                break;
            case R.id.rbForeignRequest:
                fillListView(foreignRequestList);
                break;
        }
    }
}
