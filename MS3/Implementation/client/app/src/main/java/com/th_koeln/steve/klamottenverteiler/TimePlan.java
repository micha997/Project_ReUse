package com.th_koeln.steve.klamottenverteiler;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.services.GPStracker;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimePlan extends AppCompatActivity implements LocationListener {

    private TextView textViewTimePlan;
    private Button btnDoStuff;
    private LocationManager locationManager;
    private double myLongitude = 0;
    private double myLatitude = 0;
    private String action;
    private int addIndex;

    //Array mit Requests mit dem ein Zeitplan erstellt werden soll
    private ArrayList<myTransaktion> Transaktionen = new ArrayList<myTransaktion>();
    //Neue ArrayList die nach "STEP 1" gefuellt sein soll
    private ArrayList<myTransaktion> Clean_Transaktionen = new ArrayList<myTransaktion>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_plan);

        //Eigene User-ID besorgen
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uId = firebaseAuth.getCurrentUser().getUid();

        //Objekte aus dem Layout
        textViewTimePlan = (TextView) findViewById(R.id.textViewTimePlan);
        btnDoStuff = (Button) findViewById(R.id.btnDoStuff);

        //Location Longitude/Latitude
        checkGPSPermission();
        getLocation();

        //BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("profile");
        filter.addAction("timeReceive");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        //Intent mit einem Service-Call erstellen
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        //Requests werden besorgt (Kleidungsstuecke die angefragt wurden und abgeholt werden sollen)
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","PROFILE");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests");
        //Service-Call starten
        action = "profile";
        startService(myIntent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Response vom Service erhalten
            if(action.equals("profile")) {
                String profile = intent.getStringExtra("profile");
                try {
                    JSONArray requestArray = new JSONArray(profile);
                    for (int i = 0; requestArray.length() > i; i++) {
                        JSONObject tmpRequest = new JSONObject(requestArray.get(i).toString());
                        if (tmpRequest.getString("ouId").length() > 6) {
                            myTransaktion tmpTrans = new myTransaktion(tmpRequest.getString("ouId"), tmpRequest.getString("cId"));
                            Transaktionen.add(tmpTrans);
                            textViewTimePlan.append("ouID: " + tmpTrans.getuID() + "\n" + "cID: " + tmpTrans.getcID() + "\n\n");
                        }
                    }
                    //TimePlan Call mit der ArrayList der Transaktionen
                    makeTimePlanPart1(Transaktionen);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(action.equals("timeReceive")){
                String profile = intent.getStringExtra("profile");
                try{
                    JSONObject profileJson = new JSONObject(profile);
                    DateFormat format = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                    Date date1 = format.parse(profileJson.getString("txtWeekendTimeBegin"));
                    Clean_Transaktionen.get(addIndex).setTimeFromWeekend(date1);
                    Date date2 = format.parse(profileJson.getString("txtWeekendTimeEnd"));
                    Clean_Transaktionen.get(addIndex).setTimeToWeekend(date2);
                    Date date3 = format.parse(profileJson.getString("txtWeekTimeBegin"));
                    Clean_Transaktionen.get(addIndex).setTimeFromWorkday(date3);
                    Date date4 = format.parse(profileJson.getString("txtWeekTimeEnd"));
                    Clean_Transaktionen.get(addIndex).setTimeToWorkday(date4);
                    //textViewTimePlan.append("Times: " + profileJson.getString("txtWeekTimeBegin")+"\n");
                    if(addIndex+1 == Clean_Transaktionen.size()){
                        //makeTimePlanPart2(Clean_Transaktionen);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void makeTimePlanPart1(ArrayList<myTransaktion> dirtyTransaktionen) {

        //////////////////////////////
        //STEP 1 Dulpilkate entfernen
        //////////////////////////////

        //Dulipikate von IDS werden gesucht und entfernt
        /*Hat eine Complexity von O(n^2), aber die Laenge des Arrays
        wird sehr wahrscheinich unter der Groesse 10 bleiben */

        while (dirtyTransaktionen.size() > 0) {
            myTransaktion tmpTransaktion = dirtyTransaktionen.get(0);
            Clean_Transaktionen.add(tmpTransaktion);
            dirtyTransaktionen.remove(0);

            for (int k = 0; k < dirtyTransaktionen.size(); k++) {
                if (dirtyTransaktionen.get(k).getuID().equals(tmpTransaktion.getuID())) {
                    dirtyTransaktionen.remove(k);
                    k--;
                }
            }
        }

        //Zeiten der User besorgen bevor die weitere Zeitplanung erfolgt
        for (int i = 0; Clean_Transaktionen.size() > i; i++) {
            Intent timeIntent = new Intent(getApplicationContext(), HttpsService.class);
            timeIntent.putExtra("payload", "");
            timeIntent.putExtra("method", "GET");
            timeIntent.putExtra("from", "PROFILE");
            timeIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + Clean_Transaktionen.get(0).getuID());
            //call http service
            action = "timeReceive";
            addIndex = i;
            startService(timeIntent);
        }
    }

    public void makeTimePlanPart2(ArrayList<myTransaktion> dirtyTransaktionen){

        //////////////////////////////
        //STEP 2 Zeiten nach sortieren
        //////////////////////////////

        ArrayList<myTransaktion> ZeitClean_Transaktionen = new ArrayList<myTransaktion>();
        int index = 0;

        //Check ob es Wochenende ist. Wochentage und Wochenendtage haben andere Zeiten
        boolean weekend = true;
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        if(calendar.get(Calendar.DAY_OF_WEEK)>1 && calendar.get(Calendar.DAY_OF_WEEK)<7) weekend = false;

        while(dirtyTransaktionen.size()>0){
            Date earliestTime = null;
            long shortestPeriod = 0;

            for(int i=0;dirtyTransaktionen.size()>i;i++){
                //Je nachdem ob es Wochenende ist oder nicht werden die Zeiten gewählt
                Date tmpEarliestTime = weekend ? dirtyTransaktionen.get(i).getTimeFromWeekend() : dirtyTransaktionen.get(i).getTimeFromWorkday();
                long tmpShortestPeriod = weekend ? dirtyTransaktionen.get(i).getTimeToWeekend().getTime() - dirtyTransaktionen.get(i).getTimeFromWeekend().getTime()
                        : dirtyTransaktionen.get(i).getTimeToWorkday().getTime() - dirtyTransaktionen.get(i).getTimeFromWorkday().getTime();

                //Abfrage ob neue fruehste und kuerzeste Zeit gefunden wurde
                if((earliestTime == null && shortestPeriod == 0) ||
                        ((earliestTime.after(tmpEarliestTime)) || (shortestPeriod > tmpShortestPeriod) && (earliestTime.after(tmpEarliestTime)))){
                    earliestTime = tmpEarliestTime;
                    shortestPeriod = tmpShortestPeriod;
                    index = i;
                }
            }
            ZeitClean_Transaktionen.add(dirtyTransaktionen.get(index));
            dirtyTransaktionen.remove(index);
        }

        ////////////////////////////////////////////////////////////
        //STEP 3 Wenn gleiche Zeiten dann der Disnatz nach sortieren
        ////////////////////////////////////////////////////////////

        ArrayList<myTransaktion> WayClean_Transaktionen = new ArrayList<myTransaktion>();

        while(ZeitClean_Transaktionen.size()>0){
            ArrayList<myTransaktion> Copy_Transaktionen = new ArrayList<myTransaktion>();
            Copy_Transaktionen.addAll(ZeitClean_Transaktionen);
            ArrayList<myTransaktion> tmpArray = new ArrayList<myTransaktion>();

            tmpArray.add(Copy_Transaktionen.get(0));
            Copy_Transaktionen.remove(0);

            for(int i=0;Copy_Transaktionen.size()>i;i++){
                if(weekend){
                    //Wenn es Wochenende ist
                    if(tmpArray.get(0).getTimeFromWeekend().equals(Copy_Transaktionen.get(i).getTimeFromWeekend())
                            && Math.abs(tmpArray.get(0).getTimeToWeekend().getTime()
                            - Copy_Transaktionen.get(i).getTimeToWeekend().getTime())/(60*1000)%60 <= 30){
                        tmpArray.add(Copy_Transaktionen.get(i));
                        Copy_Transaktionen.remove(i);
                        i--;
                    }
                }else{
                    //Wenn es Wochentage sind
                    if(tmpArray.get(0).getTimeFromWorkday().equals(Copy_Transaktionen.get(i).getTimeFromWorkday())
                            && Math.abs(tmpArray.get(0).getTimeToWorkday().getTime()
                            - Copy_Transaktionen.get(i).getTimeToWorkday().getTime())/(60*1000)%60 <= 30){
                        tmpArray.add(Copy_Transaktionen.get(i));
                        Copy_Transaktionen.remove(i);
                        i--;
                    }
                }
            }

            if(tmpArray.size()>1){
                int nIndex = getShortestWay(myLongitude,myLatitude,tmpArray);
                myLongitude = ZeitClean_Transaktionen.get(nIndex).getLongitude();
                myLatitude = ZeitClean_Transaktionen.get(nIndex).getLatitude();

                WayClean_Transaktionen.add(ZeitClean_Transaktionen.get(nIndex));
                ZeitClean_Transaktionen.remove(nIndex);
            }else{
                myLongitude = ZeitClean_Transaktionen.get(0).getLongitude();
                myLatitude = ZeitClean_Transaktionen.get(0).getLatitude();
                WayClean_Transaktionen.add(ZeitClean_Transaktionen.get(0));
                ZeitClean_Transaktionen.remove(0);
            }

        }

        ///////////////////////////
        //STEP 4 Termine festlegen
        ///////////////////////////

        if(weekend){
            //Termine mit den Wochenendzeiten
            for (int i = 0; WayClean_Transaktionen.size() > i; i++) {
                if(WayClean_Transaktionen.get(i).getTimeFromWeekend().equals(WayClean_Transaktionen.get(i+1).getTimeFromWeekend())
                        && WayClean_Transaktionen.get(i).getTimeToWeekend().equals(WayClean_Transaktionen.get(i+1).getTimeToWeekend())){
                    //Erneute Google Maps abfrage um Termin festlgen zu koennen
                }else if((WayClean_Transaktionen.get(i+1).getTimeFromWeekend().getTime() - WayClean_Transaktionen.get(i).getTimeToWeekend().getTime())>0){
                    //WayClean_Transaktionen.get(i).setTimeToGet();
                }else if(!WayClean_Transaktionen.get(i+1).getTimeFromWeekend().equals(WayClean_Transaktionen.get(i+1).getTimeFromWeekend())
                        || !WayClean_Transaktionen.get(i+1).getTimeToWeekend().equals(WayClean_Transaktionen.get(i+1).getTimeToWeekend())){
                    //WayClean_Transaktionen.get(i).setTimeToGet();
                }
            }
        }else{
            //Termine mit den Wochentagzeiten
        }

        //////////////////////////////////////////////////////////////
        //STEP 5 Reihenfolge mit den angenommenen Angeboten abgleichen
        //////////////////////////////////////////////////////////////

        //Fuer die Anzeige sollen alle angenommenen Angebote angezeigt werden
        //Im vorhinein entfernte Dulplikate, wegen der userID, sollen nun
        //wieder eingefuegt werden

    }

    //Sucht in der ArrayList die kuerzeste Route zu den uebergebenen Koordinaten
    public int getShortestWay(double myLongitude, double myLatitude, ArrayList<myTransaktion> tmpArray){
        double shortestDistance = -1;
        int index = 0;
        for(int i=0;tmpArray.size()>i;i++){
            double tmpDistance = -1;
            //Request an Google Maps tmpDistance =
            //https://maps.googleapis.com/maps/api/distancematrix/
            //json?units=imperial&origins=myLongitude,myLatitude&destinations=LONGITUDE_of_tmpArray[i],LATITUDE_of_tmpArray[i]&key=GMAPS-KEY
            if(shortestDistance == -1 || shortestDistance > tmpDistance){
                shortestDistance = tmpDistance;
                index = i;
            }
        }
        return index;
    }

    //Prueft ob die Permission vorhanden ist GPS zu nutzen
    private void checkGPSPermission(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    //Holt die aktuelle Position
    private void getLocation(){
        try{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        myLongitude = location.getLongitude();
        myLatitude = location.getLatitude();
        textViewTimePlan.append("Longitude: "+myLongitude+"\n"+"Latitude: "+myLatitude+"\n");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(getApplicationContext(),"Enable GPS & Internet!",Toast.LENGTH_SHORT).show();
    }
}
