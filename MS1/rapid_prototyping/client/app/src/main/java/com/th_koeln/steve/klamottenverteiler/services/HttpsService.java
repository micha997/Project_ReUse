package com.th_koeln.steve.klamottenverteiler.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
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

    @Override
    protected void onHandleIntent(Intent intent) {
        String payload = intent.getStringExtra("payload");
        String uri = intent.getStringExtra("url");
        try {
            sendJSON(uri,payload);
        } catch (KeyManagementException e) {
            // send alertdialog by brodcast
        } catch (NoSuchAlgorithmException e) {
            // send alertdialog by brodcast
        }


    }

    private void sendJSON(String uri, String payload) throws KeyManagementException, NoSuchAlgorithmException {
            // necessary for self signed certificate
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
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
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);


        HttpsURLConnection connection = null;
        try {

            URL url=new URL(uri);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            // define HTTP Method
            connection.setRequestMethod("POST");
            // define content-type for REQ and RES as JSON
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            OutputStreamWriter streamWriter = new OutputStreamWriter(connection.getOutputStream());
            streamWriter.write(payload);
            streamWriter.flush();
            StringBuilder stringBuilder = new StringBuilder();
            // if data connection is OK
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                String response = null;
                // read response
                while ((response = bufferedReader.readLine()) != null) {
                    stringBuilder.append(response + "\n");
                }
                bufferedReader.close();

                Log.d("test", stringBuilder.toString());
            } else {
                Log.e("test", connection.getResponseMessage());
                // send reponse + alertdialog by brodcast
            }
        } catch (Exception exception){
            Log.e("test", exception.toString());
            // send reponse + alertdialog by brodcast

        } finally {
            if (connection != null){
                connection.disconnect();
            }
        }
    }
}
