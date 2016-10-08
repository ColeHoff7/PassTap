package com.passtap.passtapandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import static com.passtap.passtapandroid.InitializeBrowserActivity.instanceId;

public class Authenticate extends AppCompatActivity {

    String domain = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);
        Bundle b = getIntent().getExtras();
        if(b != null) domain = b.getString("domain");
    }

    private void sendAuthentication() {
        //send server authentication
        RequestQueue queue = Volley.newRequestQueue(this);
        String token = instanceId.getToken();
        String url ="https://passtap.com/server.php?v1=setPass&v2=";
        SharedPreferences sp = getSharedPreferences("privateKey", 0);
        String pk = sp.getString("privateKey", "ERROR");
        if(pk.equals("ERROR")){
            try {
                throw new Exception("EVERYTHING'S FUCKED");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            url += pk + "&v3=" + domain;
        }
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

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
