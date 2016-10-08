package com.passtap.passtapandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

public class InitializeBrowserActivity extends AppCompatActivity {

    //public static FirebaseInstanceIdService instanceIdService = new FirebaseInstanceIdService();
    public static FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize_browser);
        getKey();
    }

    protected void getKey(){
        // Contact server to get private key, create "account"
        // Save key

        final TextView mTextView = (TextView) findViewById(R.id.output);
        RequestQueue queue = Volley.newRequestQueue(this);
        String token = instanceId.getToken();
        String url ="https://passtap.com/server.php?v1=generateKey&v2=";//thisisakey1&v3=";
        url += token + "&v3=";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        if(response != null) {
                            String browserCode = "ERROR";
                            String privateKey = null;
                            try {
                                JSONObject json = new JSONObject(response);
                                browserCode = json.getString("access_token");
                                privateKey = json.getString("private_key");


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mTextView.setText("Browser Code: " + browserCode);
                        }else{
                            mTextView.setText("null");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
