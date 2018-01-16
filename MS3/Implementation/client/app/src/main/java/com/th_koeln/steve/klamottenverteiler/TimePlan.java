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
    private double tmpLongitude = 0;
    private double tmpLatitude = 0;
    private String action;
    private int addIndex;
    private boolean weekend = true;
    private boolean gotGPSDATA = false;
    private String GOOGLE_MAPS_API = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&";
    private String API_KEY = "&mode=walking&key=AIzaSyAY6iMYLBkXbxDfmAbISNbUZUWI_7NtsoQ";

    //Array mit Requests mit dem ein Zeitplan erstellt werden soll
    private ArrayList<myTransaktion> Transaktionen = new ArrayList<myTransaktion>();
    //Neue ArrayList die nach "STEP 1" gefuellt sein soll
    private ArrayList<myTransaktion> Clean_Transaktionen = new ArrayList<myTransaktion>();
    //Neue ArrayList die nach "STEP 2" gefuellt sein soll
    private ArrayList<myTransaktion> ZeitClean_Transaktionen = new ArrayList<myTransaktion>();
    //Neue ArrayList die nach "STEP 3" gefuellt sein soll
    private ArrayList<myTransaktion> WayClean_Transaktionen = new ArrayList<myTransaktion>();
    //Neue ArrayList die Requests ohne Zeiten speichert
    private ArrayList<myTransaktion> AppendLater_Transaktionen = new ArrayList<myTransaktion>();

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

        //Permission Check
        checkGPSPermission();

        //BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("profile"));
        LocalBroadcastManager.getInstance(this).registerReceiver(nReceiver,
                new IntentFilter("showdetails"));
        LocalBroadcastManager.getInstance(this).registerReceiver(oReceiver,
                new IntentFilter("maps"));

        //Intent mit einem Service-Call erstellen
        action = "profile";
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        //Requests werden besorgt (Kleidungsstuecke die angefragt wurden und abgeholt werden sollen)
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","PROFILE");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId + "/requests");
        //Service-Call starten
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
            }

            if(action.equals("timeReceive")){
                String profile = intent.getStringExtra("profile");
                try{
                    JSONObject profileJson = new JSONObject(profile);
                    if(profileJson.has("time")) {
                        JSONObject timeJson = new JSONObject(profileJson.getString("time"));
                        DateFormat format = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                        Date date1 = format.parse(timeJson.getString("txtWeekendTimeBegin"));
                        Clean_Transaktionen.get(addIndex).setTimeFromWeekend(date1);
                        Date date2 = format.parse(timeJson.getString("txtWeekendTimeEnd"));
                        Clean_Transaktionen.get(addIndex).setTimeToWeekend(date2);
                        Date date3 = format.parse(timeJson.getString("txtWeekTimeBegin"));
                        Clean_Transaktionen.get(addIndex).setTimeFromWorkday(date3);
                        Date date4 = format.parse(timeJson.getString("txtWeekTimeEnd"));
                        Clean_Transaktionen.get(addIndex).setTimeToWorkday(date4);

                        String timeAppend = "";
                        timeAppend = "FromWeekend: " + Clean_Transaktionen.get(addIndex).getTimeFromWeekend() +"\n"
                                + "ToWeekend: " + Clean_Transaktionen.get(addIndex).getTimeToWeekend() +"\n"
                                + "FromWorkday: " + Clean_Transaktionen.get(addIndex).getTimeFromWorkday() +"\n"
                                + "ToWorkday: " + Clean_Transaktionen.get(addIndex).getTimeToWorkday() +"\n";
                        textViewTimePlan.append(timeAppend);
                    }

                    addIndex++;
                    if (addIndex == Clean_Transaktionen.size()) {
                        makeTimePlanPart2(Clean_Transaktionen);
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private BroadcastReceiver nReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(action.equals("coordReceive")){
                String clothing = intent.getStringExtra("clothing");
                try{
                    JSONObject clothingJson = new JSONObject(clothing);
                    double tmpLong = clothingJson.getDouble("longitude");
                    double tmpLat = clothingJson.getDouble("latitude");
                    ZeitClean_Transaktionen.get(addIndex).setLongitude(tmpLong);
                    ZeitClean_Transaktionen.get(addIndex).setLatitude(tmpLat);

                    String coordAppend = "";
                    coordAppend = "Coord Long: " + ZeitClean_Transaktionen.get(addIndex).getLongitude() +"\n"
                            + "Coord Lat: " + ZeitClean_Transaktionen.get(addIndex).getLatitude() +"\n";
                    textViewTimePlan.append(coordAppend);

                    addIndex++;
                    if (addIndex == ZeitClean_Transaktionen.size()) {
                        makeTimePlanPart3();
                    }
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }
    };

    private BroadcastReceiver oReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(action.equals("mapsReceive")){
                String mapsData = intent.getStringExtra("mapsData");
                try{
                    JSONObject mapsJSON = new JSONObject(mapsData);
                    JSONArray rowsArray = mapsJSON.getJSONArray("rows");
                    JSONObject elementObj = rowsArray.getJSONObject(0);
                    JSONArray elementArray = elementObj.getJSONArray("elements");
                    JSONObject finalObject = elementArray.getJSONObject(0);
                    JSONObject distanceObj = finalObject.getJSONObject("distance");
                    JSONObject durationObj = finalObject.getJSONObject("duration");

                    WayClean_Transaktionen.get(addIndex).setDistanceToMeFromLast(distanceObj.getLong("value"));
                    WayClean_Transaktionen.get(addIndex).setTimeToMeFromLast(durationObj.getLong("value"));

                    textViewTimePlan.append("Distance in Meter: " + WayClean_Transaktionen.get(addIndex).getDistanceToMeFromLast()+"\n");
                    textViewTimePlan.append("Time in Sekunden: " + WayClean_Transaktionen.get(addIndex).getTimeToMeFromLast()+"\n");

                    addIndex++;
                    if (addIndex == WayClean_Transaktionen.size()) {
                        makeTimePlanPart4();
                    }
                }
                catch(JSONException e){
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
        addIndex = 0;
        action = "timeReceive";
        for (int i = 0; Clean_Transaktionen.size() > i; i++) {
            Intent timeIntent = new Intent(getApplicationContext(), HttpsService.class);
            timeIntent.putExtra("payload", "");
            timeIntent.putExtra("method", "GET");
            timeIntent.putExtra("from", "PROFILE");
            timeIntent.putExtra("url", getString(R.string.DOMAIN) + "/user/" + Clean_Transaktionen.get(i).getuID());
            //call http service
            startService(timeIntent);
        }
    }

    public void makeTimePlanPart2(ArrayList<myTransaktion> dirtyTransaktionen) {
        //////////////////////////////////
        //STEP 2 Den Zeiten nach sortieren
        //////////////////////////////////

        int index = 0;

        //Check ob es Wochentag oder Wochenende ist
        checkWeekdayWeekend();

        //Requests entfernen, die keine Zeiten angegeben haben
        for (int i = 0; dirtyTransaktionen.size() > i; i++) {
            if (dirtyTransaktionen.get(i).getTimeToWeekend() == null) {
                AppendLater_Transaktionen.add(dirtyTransaktionen.get(i));
                dirtyTransaktionen.remove(i);
                i--;
            }
        }

        while (dirtyTransaktionen.size() > 0) {
            Date earliestTime = null;
            long shortestPeriod = 0;

            for (int i = 0; dirtyTransaktionen.size() > i; i++) {
                //Je nachdem ob es Wochenende ist oder nicht werden die Zeiten gewählt
                Date tmpEarliestTime = weekend ? dirtyTransaktionen.get(i).getTimeFromWeekend() : dirtyTransaktionen.get(i).getTimeFromWorkday();
                long tmpShortestPeriod = weekend ? dirtyTransaktionen.get(i).getTimeToWeekend().getTime() - dirtyTransaktionen.get(i).getTimeFromWeekend().getTime()
                        : dirtyTransaktionen.get(i).getTimeToWorkday().getTime() - dirtyTransaktionen.get(i).getTimeFromWorkday().getTime();

                //Abfrage ob neue fruehste und kuerzeste Zeit gefunden wurde
                if ((earliestTime == null && shortestPeriod == 0) ||
                        ((earliestTime.after(tmpEarliestTime)) || (shortestPeriod > tmpShortestPeriod) && (earliestTime.after(tmpEarliestTime)))) {
                    earliestTime = tmpEarliestTime;
                    shortestPeriod = tmpShortestPeriod;
                    index = i;
                }
            }
            ZeitClean_Transaktionen.add(dirtyTransaktionen.get(index));
            dirtyTransaktionen.remove(index);
        }
        String reihenfolge = "";
        for(int i=0;ZeitClean_Transaktionen.size()>i;i++){
            if(weekend) {
                reihenfolge = reihenfolge + ZeitClean_Transaktionen.get(i).getcID() + "\n"
                        + ZeitClean_Transaktionen.get(i).getTimeFromWeekend()+"\n"
                        + ZeitClean_Transaktionen.get(i).getTimeToWeekend()+"\n\n";
            }else{
                reihenfolge = reihenfolge + ZeitClean_Transaktionen.get(i).getcID() + "\n"
                        + ZeitClean_Transaktionen.get(i).getTimeFromWorkday()+"\n"
                        + ZeitClean_Transaktionen.get(i).getTimeToWorkday()+"\n\n";
            }
        }
        textViewTimePlan.append(reihenfolge);

        //Koordinaten der Kleidungsstuecke besorgen bevor die weitere Zeitplanung erfolgt
        addIndex = 0;
        action = "coordReceive";
        for (int k = 0; ZeitClean_Transaktionen.size() > k; k++) {
            Intent coordIntent = new Intent(getApplicationContext(), HttpsService.class);
            coordIntent.putExtra("payload","");
            coordIntent.putExtra("method", "GET");
            coordIntent.putExtra("from", "SHOWDETAILS");
            coordIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + ZeitClean_Transaktionen.get(k).getcID());
            //call http service
            startService(coordIntent);
        }
    }

    public void makeTimePlanPart3() {
        ////////////////////////////////////////////////////////////
        //STEP 3 Wenn gleiche Zeiten dann der Distanz nach sortieren
        ////////////////////////////////////////////////////////////

        //Eigene Location soll besorgt werden -> Startet danach die weitere Zeitplanung
        getLocation();
    }

    public void makeTimePlanPart4() {
        ///////////////////////////
        //STEP 4 Termine festlegen
        ///////////////////////////

        for (int i = 0; WayClean_Transaktionen.size()-1 > i; i++) {
            Date iFrom;
            Date i1From;
            Date iTo;
            Date i1To;
            Date iSetTime = null;
            Date i1SetTime = null;
            long i1DurationTo = WayClean_Transaktionen.get(i+1).getTimeToMeFromLast();

            //Calendar um die Termine festzulegen
            Calendar cal = Calendar.getInstance();

            //Parameter holen basierend auf Wochenende oder Wochentag
            if(weekend){
                iFrom = WayClean_Transaktionen.get(i).getTimeFromWeekend();
                i1From = WayClean_Transaktionen.get(i+1).getTimeFromWeekend();
                iTo = WayClean_Transaktionen.get(i).getTimeToWeekend();
                i1To = WayClean_Transaktionen.get(i+1).getTimeToWeekend();
            }else{
                iFrom = WayClean_Transaktionen.get(i).getTimeFromWorkday();
                i1From = WayClean_Transaktionen.get(i+1).getTimeFromWorkday();
                iTo = WayClean_Transaktionen.get(i).getTimeToWorkday();
                i1To = WayClean_Transaktionen.get(i+1).getTimeToWorkday();
            }
            //
            long iTo_i1From_Diff = i1From.getTime() - iTo.getTime();
            int addMinutes = (int)i1DurationTo/60;

            if(iTo_i1From_Diff<0){

                cal.setTime(i1From);
                cal.add(Calendar.MINUTE,addMinutes);
                i1SetTime = cal.getTime();
                if(i1SetTime.before(i1To)){
                    iSetTime = i1From;
                }else{
                    cal.setTime(i1To);
                    addMinutes *= -1;
                    cal.add(Calendar.MINUTE,addMinutes);
                    iSetTime = cal.getTime();
                    if(iSetTime.after(iFrom)){
                        i1SetTime = i1To;
                    }else{
                        i1SetTime = i1To;
                        iSetTime = iFrom;
                    }
                }

            }else if(iTo_i1From_Diff>=0){

                if((iTo_i1From_Diff/1000)>i1DurationTo){
                    iSetTime = iTo;
                    i1SetTime = i1From;
                }else{
                    iSetTime = iTo;
                    cal.setTime(iTo);
                    cal.add(Calendar.MINUTE,addMinutes);
                    i1SetTime = cal.getTime();
                    if(i1SetTime.after(i1To)){
                        cal.setTime(i1To);
                        addMinutes *= -1;
                        cal.add(Calendar.MINUTE,addMinutes);
                        iSetTime = cal.getTime();
                        if(iSetTime.after(iFrom)){
                            i1SetTime = i1To;
                        }else{
                            i1SetTime = i1To;
                            iSetTime = iFrom;
                        }
                    }
                }
            }
            //
            WayClean_Transaktionen.get(i).setTimeToGet(iSetTime);
            WayClean_Transaktionen.get(i+1).setTimeToGet(i1SetTime);
        }
        makeTimePlanPart5();

    }

    public void makeTimePlanPart5() {
        //////////////////////////////////////////////////////////////
        //STEP 5 Reihenfolge mit den angenommenen Angeboten abgleichen
        //////////////////////////////////////////////////////////////

        for(int i=0;WayClean_Transaktionen.size()>i;i++){
            textViewTimePlan.append("Termin: "+WayClean_Transaktionen.get(i).getTimeToGet()+"\n");
        }

        //Fuer die Anzeige sollen alle angenommenen Angebote angezeigt werden
        //Im vorhinein entfernte Dulplikate, wegen der userID, sollen nun
        //wieder eingefuegt werden
    }

    public void checkWeekdayWeekend(){
        //Check ob es Wochenende ist. Wochentage und Wochenendtage haben andere Zeiten
        weekend = true;
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        if (calendar.get(Calendar.DAY_OF_WEEK) > 1 && calendar.get(Calendar.DAY_OF_WEEK) < 7)
            weekend = false;
    }

    //Sucht in der ArrayList die kuerzeste Route zu den uebergebenen Koordinaten
    public int getShortestWay(double lng, double lat, ArrayList<myTransaktion> tmpArray){
        float shortestDistance = -1;
        int index = 0;
        for(int i=0;tmpArray.size()>i;i++){
            Location loc1 = new Location("1");
            loc1.setLongitude(lng);
            loc1.setLatitude(lat);

            Location loc2 = new Location("2");
            loc2.setLongitude(tmpArray.get(i).getLongitude());
            loc2.setLatitude(tmpArray.get(i).getLatitude());

            float tmpDistance = loc1.distanceTo(loc2);

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
        if(!gotGPSDATA) {
            gotGPSDATA = true;
            textViewTimePlan.append("Longitude: " + myLongitude + "\n" + "Latitude: " + myLatitude + "\n");

            tmpLongitude = myLongitude;
            tmpLatitude = myLatitude;

            while (ZeitClean_Transaktionen.size() > 0) {
                ArrayList<myTransaktion> Copy_Transaktionen = new ArrayList<myTransaktion>();
                Copy_Transaktionen.addAll(ZeitClean_Transaktionen);
                ArrayList<myTransaktion> step3_tmpArray = new ArrayList<myTransaktion>();
                step3_tmpArray.add(Copy_Transaktionen.get(0));
                Copy_Transaktionen.remove(0);

                for (int i = 0; Copy_Transaktionen.size() > i; i++) {
                    if (weekend) {
                        //Wenn es Wochenende ist
                        if (step3_tmpArray.get(0).getTimeFromWeekend().equals(Copy_Transaktionen.get(i).getTimeFromWeekend())
                                && Math.abs(step3_tmpArray.get(0).getTimeToWeekend().getTime()
                                - Copy_Transaktionen.get(i).getTimeToWeekend().getTime()) / (60 * 1000) % 60 <= 30) {
                            step3_tmpArray.add(Copy_Transaktionen.get(i));
                            Copy_Transaktionen.remove(i);
                            i--;
                        }
                    } else {
                        //Wenn es Wochentage sind
                        if (step3_tmpArray.get(0).getTimeFromWorkday().equals(Copy_Transaktionen.get(i).getTimeFromWorkday())
                                && Math.abs(step3_tmpArray.get(0).getTimeToWorkday().getTime()
                                - Copy_Transaktionen.get(i).getTimeToWorkday().getTime()) / (60 * 1000) % 60 <= 30) {
                            step3_tmpArray.add(Copy_Transaktionen.get(i));
                            Copy_Transaktionen.remove(i);
                            i--;
                        }
                    }
                }

                if (step3_tmpArray.size() > 1) {
                    int nIndex = getShortestWay(tmpLongitude, tmpLatitude, step3_tmpArray);
                    tmpLongitude = ZeitClean_Transaktionen.get(nIndex).getLongitude();
                    tmpLatitude = ZeitClean_Transaktionen.get(nIndex).getLatitude();
                    WayClean_Transaktionen.add(ZeitClean_Transaktionen.get(nIndex));
                    ZeitClean_Transaktionen.remove(nIndex);
                } else {
                    tmpLongitude = ZeitClean_Transaktionen.get(0).getLongitude();
                    tmpLatitude = ZeitClean_Transaktionen.get(0).getLatitude();
                    WayClean_Transaktionen.add(ZeitClean_Transaktionen.get(0));
                    ZeitClean_Transaktionen.remove(0);
                }
            }

            //Distanzen und Dauer der Distanz von GoogleMapsAPI holen
            addIndex = 0;
            action = "mapsReceive";
            for (int k = 0; WayClean_Transaktionen.size() > k; k++) {

                if(k == 0) {
                    tmpLongitude = myLongitude;
                    tmpLatitude = myLatitude;
                }else{
                    tmpLongitude = WayClean_Transaktionen.get(k-1).getLongitude();
                    tmpLatitude = WayClean_Transaktionen.get(k-1).getLatitude();
                }

                double toLongitude = WayClean_Transaktionen.get(k).getLongitude();
                double toLatitude = WayClean_Transaktionen.get(k).getLatitude();

                Intent mapsIntent = new Intent(getApplicationContext(), HttpsService.class);
                mapsIntent.putExtra("payload","");
                mapsIntent.putExtra("method", "GET");
                mapsIntent.putExtra("from", "GMAPS");
                mapsIntent.putExtra("url", GOOGLE_MAPS_API
                        + "origins=" + tmpLatitude + "," + tmpLongitude
                        + "&destinations=" + toLatitude + "," + toLongitude + API_KEY);
                //call http service
                startService(mapsIntent);
            }
        }
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
