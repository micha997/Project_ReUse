package com.th_koeln.steve.klamottenverteiler.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.structures.myTransaktion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael on 17.01.2018.
 */

public class TimePlanAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<myTransaktion> ListItems;
    private boolean weekend = true;

    public TimePlanAdapter(Context c, ArrayList<myTransaktion> i){
        ListItems = i;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return ListItems.size();
    }

    @Override
    public Object getItem(int i) {
        return ListItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View v = mInflater.inflate(R.layout.time_plan_detail, null);
        TextView idTextView = (TextView) v.findViewById(R.id.idTextView);
        TextView availableTextView = (TextView) v.findViewById(R.id.availableTextView);
        TextView dateTextView = (TextView) v.findViewById(R.id.dateTextView);

        checkWeekdayWeekend();

        String id = ListItems.get(i).getcID();
        String available = "";
        String getFrom="",getTo="";
        String date = "Termin: " + ListItems.get(i).getTimeToGetString();

        if(weekend){
            getFrom = ListItems.get(i).getTimeFromWeekendString();
            getTo = ListItems.get(i).getTimeToWeekendString();
        }else{
            getFrom = ListItems.get(i).getTimeFromWorkdayString();
            getTo = ListItems.get(i).getTimeToWorkdayString();
        }

        available = "Verfügbar: " + getFrom+" - "+getTo;

        idTextView.setText(id);
        availableTextView.setText(available);
        dateTextView.setText(date);

        return v;
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
}