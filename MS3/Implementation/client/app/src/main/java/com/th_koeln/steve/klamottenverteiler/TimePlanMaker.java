package com.th_koeln.steve.klamottenverteiler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael on 10.01.2018.
 */

public class TimePlanMaker {

    /*
    Soll ein Array erhalten mit den Clothing-IDS der
    Objekte, die von einem User angenommen wurden und
    abgeholt werden sollen.
    */
    public void makeTimePlan(String[] cIDS){

        //Eigene Location soll geholt werden
        double myLongitude = 2.3123;
        double myLatitude = 4.4123;



        //Klasse "myTransaktion" soll genutzt werden, um die Transaktionobjekte zu erstellen und weiter zu nutzen

        /*
        HTTPS-SERVICE CALL FOR USER-IDS
        Fuer jede clothing-ID muss eine user-ID besorgt werden
        */
        //-> IDs und weitere Infos werden zu den "myTransaktion"-Objekten hinzugefuegt



        //Array mit den geholten IDs
        ArrayList<myTransaktion> Transaktionen = new ArrayList<myTransaktion>();
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
