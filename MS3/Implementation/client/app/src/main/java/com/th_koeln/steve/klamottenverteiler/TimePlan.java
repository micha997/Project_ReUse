package com.th_koeln.steve.klamottenverteiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.services.GPStracker;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimePlan extends AppCompatActivity {

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_plan);

        //Eigene User-ID besorgen
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uId = firebaseAuth.getCurrentUser().getUid();

        //Intent mit einem Service-Call erstellen
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("profile"));
        //Parameter des Service-Call definieren
        //IDs der Kleidung soll besorgt werden,
        //die von dem Nutzer abgeholt werden soll
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","PROFILE");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId);
        //Service-Call starten
        startService(myIntent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Response vom Service erhalten
            String profile = intent.getStringExtra("profile");
            try {
                JSONObject clothingIDS = new JSONObject(profile);
                //JSONObject an die Funktion weitergeben
                makeTimePlan(clothingIDS);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    /*
    Soll ein Array erhalten mit den Clothing-IDS der
    Objekte, die von einem User angenommen wurden und
    abgeholt werden sollen.
    */
    public void makeTimePlan(JSONObject clothingIDS) throws JSONException {

        //Array zum fuellen mit den IDs
        ArrayList<myTransaktion> Transaktionen = new ArrayList<myTransaktion>();

        //JSONObject in eigene Struktur transferieren und zum Array hinufuegen
        try {
            JSONArray cIDs = clothingIDS.getJSONArray("nameIDs");
            for(int i=0;cIDs.length()>i;i++){
                String cID = (String) cIDs.get(i);
                myTransaktion tmpTrans = new myTransaktion(cID);
                Transaktionen.add(tmpTrans);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Eigene Location
        double myLongitude = 0;
        double myLatitude = 0;

        //Service wird gestartet um eigene Koordianten zu holen
        GPStracker gpsTracker = new GPStracker(getApplicationContext());
        Location myLocation = gpsTracker.getLocation();
        if(myLocation != null){
            myLongitude = myLocation.getLongitude();
            myLatitude = myLocation.getLatitude();
        }else{
            Toast.makeText(getApplicationContext(),"Location unavailable",Toast.LENGTH_LONG).show();
        }

        /*
        HTTPS-SERVICE CALL FOR USER-IDS
        Fuer jede clothing-ID muss eine user-ID besorgt werden
        */
        //-> IDs und weitere Infos werden zu den "myTransaktion"-Objekten hinzugefuegt

        //Neue ArrayList die nach "STEP 1" gefuellt sein soll
        ArrayList<myTransaktion> Clean_Transaktionen = new ArrayList<myTransaktion>();

        //////////////////////////////
        //STEP 1 Dulpilkate entfernen
        //////////////////////////////

        //Dulipikate von IDS werden gesucht und entfernt
        /*Hat eine Complexity von O(n^2), aber die Laenge des Arrays
        wird sehr wahrscheinich unter der Groesse 10 bleiben */

        while(Transaktionen.size()>0){
            myTransaktion tmpTransaktion = Transaktionen.get(0);
            Clean_Transaktionen.add(tmpTransaktion);
            Transaktionen.remove(0);

            for(int k = 0;k<Transaktionen.size();k++){
                if(Transaktionen.get(k).getuID().equals(tmpTransaktion.getuID())){
                    Transaktionen.remove(k);
                    k--;
                }
            }
        }

        /*
        HTTPS-SERVICE CALL FOR USER-ZEITEN
        Fuer jede user-id sollen user-spezifische Zeiten besorgt werden
        */
        //->Zeiten werden den "myTransaktion"-Objekten in der "Clean_Transaktionen"-ArrayList hinzugefuegt

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

        while(Clean_Transaktionen.size()>0){
            Date earliestTime = null;
            long shortestPeriod = 0;

            for(int i=0;Clean_Transaktionen.size()>i;i++){
                //Je nachdem ob es Wochenende ist oder nicht werden die Zeiten gewählt
                Date tmpEarliestTime = weekend ? Clean_Transaktionen.get(i).getTimeFromWeekend() : Clean_Transaktionen.get(i).getTimeFromWorkday();
                long tmpShortestPeriod = weekend ? Clean_Transaktionen.get(i).getTimeToWeekend().getTime() - Clean_Transaktionen.get(i).getTimeFromWeekend().getTime()
                        : Clean_Transaktionen.get(i).getTimeToWorkday().getTime() - Clean_Transaktionen.get(i).getTimeFromWorkday().getTime();

                //Abfrage ob neue fruehste und kuerzeste Zeit gefunden wurde
                if((earliestTime == null && shortestPeriod == 0) ||
                        ((earliestTime.after(tmpEarliestTime)) || (shortestPeriod > tmpShortestPeriod) && (earliestTime.after(tmpEarliestTime)))){
                    earliestTime = tmpEarliestTime;
                    shortestPeriod = tmpShortestPeriod;
                    index = i;
                }
            }
            ZeitClean_Transaktionen.add(Clean_Transaktionen.get(index));
            Clean_Transaktionen.remove(index);
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
}
