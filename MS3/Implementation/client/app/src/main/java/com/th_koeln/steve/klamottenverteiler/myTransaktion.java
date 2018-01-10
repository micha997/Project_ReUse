package com.th_koeln.steve.klamottenverteiler;

import java.util.Date;

/**
 * Created by Michael on 10.01.2018.
 */

//Klasse um Objekte zu erstellen, die fuer die Transaktion benoetigt werden
public class myTransaktion {
    //ID des Angebotes
    String uID;
    //ID des Users
    String cID;

    //Ort des Angebotes
    double longitude;
    double latitude;

    //Zeit Von Bis an Wochentagen
    Date timeFromWorkday;
    Date timeToWorkday;
    //Zeit Von Bis am Wochenende
    Date timeFromWeekend;
    Date timeToWeekend;

    //Zeitpunkt zum abholen
    Date timeToGet;

    public myTransaktion(String clothingID){
        uID = "";
        cID = clothingID;
        longitude = 0;
        latitude = 0;
        timeFromWorkday = null;
        timeToWorkday = null;
        timeFromWeekend = null;
        timeToWeekend = null;
        timeToGet = null;
    }

    public String getuID(){
        return uID;
    }

    public void setuID(String userID){
        uID = userID;
    }

    public String getcID(){
        return cID;
    }

    public void setcID(String clothingID){
        cID = clothingID;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setLongitude(double lng){
        longitude = lng;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setLatitude(double lat){
        latitude = lat;
    }

    public Date getTimeFromWorkday(){
        return timeFromWorkday;
    }

    public void setTimeFromWorkday(Date tfWork){
        timeFromWorkday = tfWork;
    }

    public Date getTimeToWorkday(){
        return timeToWorkday;
    }

    public void setTimeToWorkday(Date ttWork){
        timeToWorkday = ttWork;
    }

    public Date getTimeFromWeekend(){
        return timeFromWeekend;
    }

    public void setTimeFromWeekend(Date tfWeek){
        timeFromWeekend = tfWeek;
    }

    public Date getTimeToWeekend(){
        return timeToWeekend;
    }

    public void setTimeToWeekend(Date ttWeek){
        timeToWeekend = ttWeek;
    }

    public Date getTimeToGet(){
        return timeToGet;
    }

    public void setTimeToGet(Date ttG){
        timeToGet = ttG;
    }


}
