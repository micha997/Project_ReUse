package com.th_koeln.steve.klamottenverteiler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Frank on 15.01.2018.
 */

public class RequestListAdapter extends ArrayAdapter<Request> {
    private static final String TAG = "RequestAdapter";
    private Context mContext;
    private int mRessource;

    public RequestListAdapter(Context context, int resource, List<Request> objects) {
        super(context, resource, objects);
        mContext=context;
        mRessource = resource;


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).getName();
        String art = getItem(position).getArt();
        String size = getItem(position).getSize();
        String brand = getItem(position).getBrand();
        String status = getItem(position).getStatus();

        Request request = new Request(name, art, size, brand,status);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mRessource,parent,false);

        TextView txtRequestName = (TextView) convertView.findViewById(R.id.txtRequestName);
        TextView txtRequestArt = (TextView) convertView.findViewById(R.id.txtRequestArt);
        TextView txtRequestSize = (TextView) convertView.findViewById(R.id.txtRequestSize);
        TextView txtRequestBrand = (TextView) convertView.findViewById(R.id.txtRequestBrand);
        TextView txtRequestStatus = (TextView) convertView.findViewById(R.id.txtRequestStatus);
        txtRequestName.setText("Name : " + name);
        txtRequestArt.setText("Art: " + art);
        txtRequestSize.setText("Size: " + size);
        txtRequestBrand.setText("Brand: " + brand);
        txtRequestStatus.setText("Status: " + status);


        return convertView;
    }
}
