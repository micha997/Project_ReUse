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
import android.util.Base64;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.th_koeln.steve.klamottenverteiler.adapter.RequestAdapter;
import com.th_koeln.steve.klamottenverteiler.adapter.RequestListAdapter;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;
import com.th_koeln.steve.klamottenverteiler.structures.ClothingOffer;
import com.th_koeln.steve.klamottenverteiler.structures.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Steffen Owtschinnikow on 07.01.2018.
 */

public class ShowRequest extends AppCompatActivity implements View.OnClickListener  {
    private JSONArray requestJsonArray;
    private RadioButton rbOwnRequests;
    private RadioButton rbForeignRequests;
    private ArrayList<Request> foreignRequestList = new ArrayList<>();
    private ArrayList<Request> ownRequestList = new ArrayList<>();
    private boolean menu_first;
    private ProgressDialog progress;
    private AlertDialog alertDialog;
    private RequestListAdapter reqAdapter;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String uId = firebaseAuth.getCurrentUser().getUid();

    private ArrayList<ClothingOffer> MyRequestsForAdapter;
    private ArrayList<ClothingOffer> ForeignRequestsForAdapter;

    private ListView requestsListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_requests);

        requestsListView = (ListView) findViewById(R.id.requestsListView);

        rbOwnRequests = (RadioButton) findViewById(R.id.rbOwnRequest);
        rbOwnRequests.setOnClickListener(this);

        rbForeignRequests = (RadioButton) findViewById(R.id.rbForeignRequest);
        rbForeignRequests.setOnClickListener(this);

        MyRequestsForAdapter = new ArrayList<>();
        ForeignRequestsForAdapter = new ArrayList<>();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("showrequests"));

        // Hole die verfügbaren Requests vom Server
        getRequestsFromServer();

        requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.showContextMenu();
            }
        });

        registerForContextMenu(requestsListView);

        progress = new ProgressDialog(this);
        progress.setTitle("Please wait");
        progress.setMessage("Trying to get your requests..");
        progress.show();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // Setze Contextmenu Optionen je nach status des Kleidungsstück
        if (v.getId() == R.id.requestsListView) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            ClothingOffer obj = (ClothingOffer) lv.getItemAtPosition(acmi.position);

            if (obj.getStatus().equals("open") && obj.getFrom().equals("foreign")) {
                // Fremde Anfrage wurde noch nicht angenommen
                menu.add("Accept");
                menu.add("Delete");
            } else if (obj.getStatus().equals("open") && obj.getFrom().equals("own")) {
                // Eigene Anfrage wurde noch nicht angenommen
                menu.add("Delete");
            } else if (obj.getStatus().equals("accepted") && obj.getFrom().equals("own")) {
                // Eigene Anfrage wurde akzeptiert
                menu.add("Chat");
                menu.add("Delete");
                menu.add("Success");
            } else if (obj.getStatus().equals("accepted") && obj.getFrom().equals("foreign")) {
                // Fremde Anfrage wurde akzeptiert
                menu.add("Chat");
                menu.add("Delete");
                menu.add("Success");
            } else if (obj.getStatus().equals("waiting") && !obj.getConfirmed().equals(uId)) {
                // Anfrage wurde vom Benutzer als erfolgreich markiert
                menu.add("Chat");
                menu.add("Confirm");
                menu.add("Delete");
            } else if (obj.getStatus().equals("waiting") && obj.getConfirmed().equals(uId)) {
                // Anfrage wurde vom Transaktionspartner als erfolgreich markiert
                menu.add("Chat");
                menu.add("Delete");
            } else if (obj.getStatus().equals("confirmed")) {
                // Anfrage wurde von beiden Transaktionspartnern als erfolgreich markiert
                menu.add("Chat");
                menu.add("Delete");
                menu.add("Start User Rating");
            } else if (obj.getStatus().equals("closed") && obj.getClosed().equals("foreign") && obj.getFrom().equals("own") && obj.getFinished().equals("0")) {
                // Transaktion wurde von einer Seite bewertet
                menu.add("Rate User");
            } else if (obj.getStatus().equals("closed") && obj.getClosed().equals("own")&& obj.getFrom().equals("foreign") && obj.getFinished().equals("0")) {
                // Transaktion wurde von einer Seite bewertet
                menu.add("Rate User");
            } else if (obj.getFinished().equals("1")) {
                // Transaktion wurde von beiden Seiten bewertet
                menu.add("Delete");
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

            if (info.targetView.getParent() != findViewById(R.id.requestsListView))
                return super.onContextItemSelected(item);

            String title = (String) item.getTitle();
            int menuItemIndex = info.position;
            Intent myIntent;
            if (rbForeignRequests.isChecked()) {

                ClothingOffer menuItems = ForeignRequestsForAdapter.get(menuItemIndex);
                String name = menuItems.getId();

                switch (title) {
                    case "Delete":
                        // Lösche Anfrage vom Server
                        sendDelete(menuItems.getuId(), name);
                        break;
                    case "Accept":
                        // Akzeptiere Anfrage
                        setStatus("accepted", name);
                        break;
                    case "Chat":
                        // Starte Chat-Service
                        myIntent = new Intent(getApplicationContext(), Chat.class);
                        myIntent.putExtra("rId", menuItems.getId());
                        myIntent.putExtra("to", menuItems.getuId());
                        myIntent.putExtra("from", uId);
                        startActivity(myIntent);
                        break;
                    case "Success":
                        // Anfrage wird einseitig als erfolgreich markiert
                        setStatus("waiting", menuItems.getId());
                        break;
                    case "Confirm":
                        // Erfolgreiche Anfrage wird bestätigt
                        setStatus("confirmed", name);
                        break;
                    case "Rate User":
                        // Transaktionspartner hat die Transaktion bereits bewertet. Aktivität zum Bewerten der Tranksaktion wird gestartet,
                        myIntent = new Intent(getApplicationContext(), RateUser.class);
                        myIntent.putExtra("tId", menuItems.getId());
                        myIntent.putExtra("ouId", menuItems.getuId());
                        myIntent.putExtra("rFrom",menuItems.getFrom());
                        myIntent.putExtra("finished","1");
                        startActivity(myIntent);
                        break;
                    case "Start User Rating":
                        // Es liegt noch keine Bewertung für die Transaktion vor. Bewertungsaktivität wird gestartet
                        myIntent = new Intent(getApplicationContext(), RateUser.class);
                        myIntent.putExtra("tId", menuItems.getId());
                        myIntent.putExtra("ouId", menuItems.getuId());
                        myIntent.putExtra("rFrom",menuItems.getFrom());
                        myIntent.putExtra("finished","0");
                        startActivity(myIntent);
                        break;
                }

            } else {

                ClothingOffer menuItems = (ClothingOffer) MyRequestsForAdapter.get(menuItemIndex);
                String name = menuItems.getId();

                switch (title) {
                    case "Delete":
                        sendDelete(uId, name);
                        break;
                    case "Chat":
                        myIntent = new Intent(getApplicationContext(), Chat.class);
                        myIntent.putExtra("rId", menuItems.getId());
                        myIntent.putExtra("to", menuItems.getuId());
                        myIntent.putExtra("from", uId);
                        startActivity(myIntent);
                        break;
                    case "Success":
                        setStatus("waiting", menuItems.getId());
                        break;
                    case "Confirm":
                        setStatus("confirmed", name);
                        break;
                    case "Rate User":
                        myIntent = new Intent(getApplicationContext(), RateUser.class);
                        myIntent.putExtra("tId", menuItems.getuId());
                        myIntent.putExtra("ouId", menuItems.getuId());
                        myIntent.putExtra("rFrom",menuItems.getFrom());
                        myIntent.putExtra("finished","1");
                        startActivity(myIntent);
                        break;
                    case "Start User Rating":
                        myIntent = new Intent(getApplicationContext(), RateUser.class);
                        myIntent.putExtra("tId", menuItems.getId());
                        myIntent.putExtra("ouId", menuItems.getuId());
                        myIntent.putExtra("rFrom",menuItems.getFrom());
                        myIntent.putExtra("finished","0");
                        startActivity(myIntent);
                        break;
                }
            }

        }
        return true;
    }

    /**
     * Sucht innerhalb der Datenstruktur der Requests nach dem Request, der angepasst werden soll
     * und sendet dem Server anschließend die ID des Requests und den Status der gewünscht ist.
     *
     * @param  status  Beschreibt den status, den der Request bekommen soll.
     * @param  spin Enthält die ID des Requests, welcher bearbeitet werden soll.
     */

    private void setStatus(String status, String spin) {
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
            // Sende neuen Status an den Server
            myIntent.putExtra("method","PUT");
            myIntent.putExtra("from","PUTREQUEST");
            myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests/" + spin);
            startService(myIntent);
        } catch (JSONException e1) {
            showDialog("Error", "Could not set status of clothing!");
        }
    }

    /**
     * Sendet einen Aufruf zum Löschen eines bestimmten Requests an den Server.
     *
     * @param  uId  Enthält die Identifikationsnummer des Benutzers, dessen Request gelöscht werden soll
     * @param  id Enthält die ID des Requests, welcher gelöscht werden soll.
     */

    private void sendDelete(String uId, String id) {
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","DELETE");
        myIntent.putExtra("from","DELETEREQUEST");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests/" + id);
        startService(myIntent);
    }

    //Leitet den Aufruf vorhandener Requests vom Server ein.
    private void getRequestsFromServer() {
        String idToken = FirebaseInstanceId.getInstance().getToken();
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SHOWREQUESTS");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/" + idToken + "/requests");
        startService(myIntent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String success = intent.getStringExtra("success");
            if (success.equals("1")) {
                // Status eines Requests wurde erfolgreich geaendert.
                getRequestsFromServer();
                reqAdapter.notifyDataSetChanged();
                showDialog("Success!", "Successfully edited status of request!");
            } else if(success.equals("2")) {
                getRequestsFromServer();
                reqAdapter.notifyDataSetChanged();
                // Request wurde erfolgreich gelöscht
                showDialog("Success!", "Successfully deleted requests!");
            } else {
                String from = intent.getStringExtra("from");
                if (from.equals("SHOWREQUESTSFAIL")) {
                    progress.dismiss();
                    // Requests konnten nicht vom Server geholt werden
                    showDialog("Error", "Could not get request!");
                } else {
                    String requests = intent.getStringExtra("clothing");
                    try {
                        requestJsonArray = new JSONArray(requests);
                        // Trenne eigene Requests von fremden Requests
                        // und erstelle für jeden Request ein Objekt
                        for (int i = 0; i < requestJsonArray.length(); i++) {
                            ClothingOffer tmpCloth;
                            JSONObject reqJO = requestJsonArray.getJSONObject(i);
                            String absPath = "";
                            if (!reqJO.isNull("image")) {
                                String filename = "img" + i;
                                String string = reqJO.getString("image");
                                FileOutputStream outputStream;
                                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                byte[] decodedBytes = Base64.decode(string, 0);
                                outputStream.write(decodedBytes);
                                outputStream.close();

                                absPath = ShowRequest.this.getFilesDir().getAbsolutePath() + "/" + filename;
                            }
                            //Objekt mit allen Informationen wird erstellt
                            tmpCloth = new ClothingOffer(reqJO.getString("id"), reqJO.getString("ouId"),
                                    reqJO.getString("art"), reqJO.getString("size"), reqJO.getString("style"),
                                    reqJO.getString("gender"), "", reqJO.getString("fabric"),
                                    reqJO.getString("notes"), reqJO.getString("brand"), absPath, -400);
                            tmpCloth.setStatus(reqJO.getString("status"));
                            tmpCloth.setFrom(reqJO.getString("from"));
                            tmpCloth.setConfirmed(reqJO.getString("confirmed"));
                            tmpCloth.setFinished(reqJO.getString("finished"));
                            tmpCloth.setClosed(reqJO.getString("closed"));

                            //Objekt wird zur jeweiligen Liste hinzugefuegt
                            if (tmpCloth.getFrom().equals("own")) {MyRequestsForAdapter.add(tmpCloth);}
                            else if(tmpCloth.getFrom().equals("foreign")){ForeignRequestsForAdapter.add(tmpCloth);}
                        }

                        //Je nachdem welche Anzeige per RadioButton
                        //gewaehlt ist wird auch das passende angezeigt
                        if (rbForeignRequests.isChecked()) {
                            progress.dismiss();
                            if (ForeignRequestsForAdapter.size() > 0) {fillView(ForeignRequestsForAdapter);}
                            else{requestsListView.setEmptyView(findViewById(R.id.txtEmptyRequestList));}
                        } else {
                            progress.dismiss();
                            if (MyRequestsForAdapter.size() > 0) {fillView(MyRequestsForAdapter);}
                            else{requestsListView.setEmptyView(findViewById(R.id.txtEmptyRequestList));}
                        }

                    } catch (JSONException e) {
                        showDialog("Error", "Could not process request data!");
                        progress.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                        progress.dismiss();
                    }

                }
            }
        }
    };

    //Nimmt die aktuell ausgewaehlten Requests entgegen
    //und zeigt diese in einer ListView an
    private void fillView(ArrayList<ClothingOffer> options) {
        RequestAdapter optAdapter;
        optAdapter = new RequestAdapter(this, options);
        requestsListView.setAdapter(optAdapter);
    }

    private void showDialog(String title, String message) {
        alertDialog = new AlertDialog.Builder(ShowRequest.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (!isFinishing())
            alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //Anzeige der eigenen Requests
            case R.id.rbOwnRequest:
                if (MyRequestsForAdapter.size() > 0) {fillView(MyRequestsForAdapter);}
                else{
                    requestsListView.setAdapter(null);
                    requestsListView.setEmptyView(findViewById(R.id.txtEmptyRequestList));
                }
                break;
            //Anzeige der fremden Requests
            case R.id.rbForeignRequest:
                if (ForeignRequestsForAdapter.size() > 0) {fillView(ForeignRequestsForAdapter);}
                else{
                    requestsListView.setAdapter(null);
                    requestsListView.setEmptyView(findViewById(R.id.txtEmptyRequestList));
                }
                break;
        }
    }
}