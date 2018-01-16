package com.th_koeln.steve.klamottenverteiler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by Frank on 15.01.2018.
 */

public class RequestListAdapter extends ArrayAdapter<Request> {
    private static final String TAG = "RequestAdapter";
    private Context mContext;
    private int mRessource;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();

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
        String from = getItem(position).getFrom();
        String ouId = getItem(position).getOuId();
        String confirmed = getItem(position).getConfirmed();

        Request request = new Request(name, art, size, brand, status, from, ouId, confirmed);

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

        switch (status) {
            case "open":
                txtRequestStatus.setText("Status: " + "Waiting for response..");
                break;
            case "accepted":
                txtRequestStatus.setText("Status: accepted");
                break;
            case "waiting":
                if (confirmed.equals(uId)) {
                    txtRequestStatus.setText("Status: Waiting for confirmation");
                } else {
                    txtRequestStatus.setText("Status: Waiting for your confirmation");
                }
                break;
            default:
                txtRequestStatus.setText(status);
                break;
        }



        return convertView;
    }
}
