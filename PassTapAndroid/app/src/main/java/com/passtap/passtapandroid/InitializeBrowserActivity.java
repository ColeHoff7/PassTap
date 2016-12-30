package com.passtap.passtapandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
import java.util.Random;


public class InitializeBrowserActivity extends AppCompatActivity {

    //public static FirebaseInstanceIdService instanceIdService = new FirebaseInstanceIdService();
    public FirebaseInstanceId instanceId = FirebaseInstanceId.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize_browser);
        getKey();
    }

    //called when finished with browser authentication
    public void toMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    protected void getKey(){
        // Contact server to get private key, create "account"
        // Save key

        final TextView mTextView = (TextView) findViewById(R.id.output);
        final TextView tv2 = (TextView) findViewById(R.id.textView4);
        final TextView tv3 = (TextView) findViewById(R.id.textView5);
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
                            String accountID = null;
                            try {
                                JSONObject json = new JSONObject(response);
                                browserCode = json.getString("access_token");
                                accountID = json.getString("acc_id");
                                privateKey = generateRandString();
                                SharedPreferences sp = getSharedPreferences("privateKey", 0);
                                SharedPreferences.Editor editor = sp.edit();
                                SharedPreferences shp = getSharedPreferences("accoutID", 0);
                                SharedPreferences.Editor edit = shp.edit();
                                editor.putString("privateKey", privateKey);
                                edit.putString("accountID", accountID);
                                editor.commit();
                                edit.commit();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mTextView.setText("Enter this code into your browser:");
                            tv2.setText(browserCode);
                            tv3.setText("Right after you enter it into your browser press 'All Set'!");
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
        return;
    }

    protected String generateRandString(){
        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int charactersLength = characters.length();
        String randomString = "";
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            int get = rand.nextInt(charactersLength)-1;
            if(get<0) get = 0;
            randomString = randomString + characters.charAt(get);
        }
        return randomString;
    }
}
