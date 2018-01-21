package com.th_koeln.steve.klamottenverteiler.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by steve on 31.10.17.
 */
public class HttpsService extends IntentService {
    public HttpsService() {
        super("name");
    }

    private String response = null;
    private String method;
    private String from;

    HttpURLConnection connection = null;
    Handler mHandler;

    public void onCreate() {
        super.onCreate();
         mHandler = new Handler();
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        String payload = intent.getStringExtra("payload");
        String uri = intent.getStringExtra("url");
        method = intent.getStringExtra("method");
        from = intent.getStringExtra("from");

        // necessary for self signed certificate. WARNING: connection is not secure with this
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


       // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        //HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        URL url = null;
        try {
            url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            // define HTTP Method
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000);

            if (method.equals("POST") || method.equals("PUT")) {
                // http-req with body
                connection.setDoOutput(true);
                // http-req with res body
                connection.setDoInput(true);
                // define content-type for REQ and RES as JSON
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                OutputStreamWriter streamWriter = new OutputStreamWriter(connection.getOutputStream());
                //write payload
                streamWriter.write(payload);
                streamWriter.flush();
            } else if (method.equals("GET")) {
                connection.setUseCaches(false);
                connection.setAllowUserInteraction(false);
                connection.connect();
            }
            sendJSON(uri, payload);
        }  catch (SocketTimeoutException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HttpsService.this, "Can not connect to server. Internet missing?", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void sendJSON(String uri, String payload) throws KeyManagementException, NoSuchAlgorithmException {

        try {
            //container for response
            StringBuilder stringBuilder = new StringBuilder();
            // get statuscode from response
            int status = connection.getResponseCode();
            Intent intent = new Intent();
            // analyse Status code
            switch (status) {
                case 200:
                case 201:
                    InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(streamReader);

                    // read response and safe to String
                    while ((response = bufferedReader.readLine()) != null) {
                        stringBuilder.append(response + "\n");
                    }
                    bufferedReader.close();


                    //send Data back
                    switch (from) {

                        case "SEARCH":
                            // send clothing JSON array to Google Map
                            intent = new Intent("clothing");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "SEARCH");
                            break;

                        case "GMAPS":
                            intent = new Intent("gmaps");
                            intent.putExtra("mapsData",stringBuilder.toString());
                            intent.putExtra("from","GMAPS");
                            break;

                        case "CLOTHINGOPTIONS":
                            intent = new Intent("clothingOptions");
                            intent.putExtra("optionsData", stringBuilder.toString());
                            intent.putExtra("from", "CLOTHINGOPTIONS");
                            break;

                        case "SEARCHPREFER":
                            intent = new Intent("prefer");
                            intent.putExtra("prefer", stringBuilder.toString());
                            intent.putExtra("from", "SEARCHPREFER");
                            break;

                        case "SEARCHPREFCLOTHING":
                            intent = new Intent("clothing");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "SEARCHPREFCLOTHING");
                            break;

                        case "SHOWDETAILS":
                            intent = new Intent("showdetails");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "SHOWDETAILS");
                            break;

                        case "PROFILE":
                            intent = new Intent("profile");
                            intent.putExtra("profile", stringBuilder.toString());
                            intent.putExtra("from", "SEARCHPROFILE");
                            break;

                        case "MYCLOTHING":
                            intent = new Intent("myclothing");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "MYCLOTHING");
                            break;

                        case "EDITCLOTHING":
                            intent = new Intent("editclothing");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "EDITCLOTHING");
                            break;

                        case "SEARCHOUTFIT":
                            intent = new Intent("showoutfit");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "SEARCHCLOTHING");
                            break;

                        case "SHOWREQUESTS":
                            intent = new Intent("showrequests");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("success", "0");
                            intent.putExtra("from", "SHOWREQUESTS");
                            break;

                        case "GETCONVERSATION":
                            intent = new Intent("chat");
                            intent.putExtra("params", stringBuilder.toString());
                            intent.putExtra("from", "GETCONVERSATION");
                        break;

                        case "ADDCLOTHING":
                            intent = new Intent("addclothing");
                            intent.putExtra("from", "ADDCLOTHING");
                            intent.putExtra("success", "1");
                            break;
                        case "PUTREQUEST":
                            intent = new Intent("showrequests");
                            intent.putExtra("from", "PUTREQUEST");
                            intent.putExtra("success", "1");
                            break;
                        case "DELETEREQUEST":
                            intent = new Intent("showrequests");
                            intent.putExtra("from", "PUTREQUEST");
                            intent.putExtra("success", "2");
                            break;
                        case "POSTRATING":
                            intent = new Intent("RATEUSER");
                            intent.putExtra("from", "POSTRATING");
                            break;


                    }
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    break;

                case 500:
                    switch (from) {

                        case "ADDCLOTHING":
                            intent = new Intent("addclothing");
                            intent.putExtra("from", "ADDCLOTHING");
                            intent.putExtra("success", "0");
                            break;
                        case "SEARCHOUTFIT":
                            intent = new Intent("showoutfit");
                            intent.putExtra("from", "SEARCHOUTFITFAIL");
                            intent.putExtra("success", "0");
                            break;
                        case "GETCONVERSATION":
                            intent = new Intent("chat");
                            intent.putExtra("from", "GETCONVERSATIONFAIL");
                            break;
                        case "POSTMESSAGE":
                            intent = new Intent("chat");
                            intent.putExtra("from", "POSTMESSAGEFAIL");
                            break;
                        case "EDITCLOTHING":
                            intent = new Intent("editclothing");
                            intent.putExtra("from", "EDITCLOTHINGFAIL");
                            break;
                        case "PUTCLOTHING":
                            intent = new Intent("editclothing");
                            intent.putExtra("from", "PUTCLOTHINGFAIL");
                            break;
                        case "PROFILE":
                            intent = new Intent("profile");
                            intent.putExtra("from", "SEARCHPROFILEFAIL");
                            break;
                        case "PUTPROFILE":
                            intent = new Intent("profile");
                            intent.putExtra("from", "PUTPROFILEFAIL");
                            break;
                        case "NEW_TOKEN":
                            intent = new Intent("login");
                            intent.putExtra("from", "POSTTOKENFAIL");
                            break;
                        case "NEWUSER":
                            intent = new Intent("main");
                            intent.putExtra("from", "POSTUSERFAIL");
                            break;
                        case "SHOWDETAILS":
                            intent = new Intent("showdetails");
                            intent.putExtra("from", "SHOWDETAILSFAIL");
                            break;

                        case "MYCLOTHING":
                            intent = new Intent("myclothing");
                            intent.putExtra("from", "MYCLOTHINGFAIL");
                            break;
                        case "POSTRATING":
                            intent = new Intent("RATEUSER");
                            intent.putExtra("from", "POSTRATINGFAIL");
                            break;
                        case "SEARCH":
                            intent = new Intent("clothing");
                            intent.putExtra("from", "SEARCHFAIL");
                            break;
                        case "SEARCHPREFER":
                            intent = new Intent("clothing");
                            intent.putExtra("from", "SEARCHPREFERFAIL");
                            break;
                        case "NEWREQUEST":
                            intent = new Intent("showclothing");
                            intent.putExtra("from", "NEWREQUESTFAIL");
                            break;
                        case "SUBSCRIBECLOTHING":
                            intent = new Intent("showoutfit");
                            intent.putExtra("from", "SUBSCRIBECLOTHINGFAIL");
                            break;
                        case "SHOWREQUESTS":
                            intent = new Intent("showrequests");
                            intent.putExtra("from", "SHOWREQUESTSFAIL");
                            intent.putExtra("success", "0");
                            break;


                    }
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    break;
                default:
                    Log.e("test", connection.getResponseMessage());
                    break;
            }


        } catch (Exception exception) {
            Log.e("test", exception.toString());
            // send reponse + alertdialog by brodcast

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
