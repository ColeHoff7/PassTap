package com.passtap.passtapandroid;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by Cole on 10/8/16.
 */

public class IdentifierManager extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        try {
            sendNewTokenToServer(refreshedToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO test
    public void sendNewTokenToServer(String refreshedToken) throws Exception {
        SharedPreferences sp = getSharedPreferences("privateKey", 0);
        String pk = sp.getString("privateKey", "ERROR");
        String url ="https://passtap.com/server.php?v1=updateToken&v2=";
        if(pk.equals("ERROR")){
            throw new Exception("EVERYTHING'S FUCKED");
        }else {
            url += pk + "&v3=" + refreshedToken;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response != null) {


                        }else{

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("privateKey", refreshedToken);
        editor.commit();
    }
}
