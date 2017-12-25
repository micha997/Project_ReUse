package com.th_koeln.steve.klamottenverteiler.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
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
    private int status=0;
    private String method;
    private String from;
    HttpURLConnection connection = null;


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

            if (method.equals("POST")) {
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
            // get Statuscode from Response
            int status = connection.getResponseCode();
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
                    Intent intent = new Intent();
                    switch (from) {
                        case "SEARCH":
                            // send clothing JSON array to Google Map
                            intent = new Intent("clothing");
                            intent.putExtra("clothing", stringBuilder.toString());
                            intent.putExtra("from", "SEARCH");
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
                    }
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    Log.d("test", stringBuilder.toString());
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
