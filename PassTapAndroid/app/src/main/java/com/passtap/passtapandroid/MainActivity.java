package com.passtap.passtapandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sp = getSharedPreferences("privateKey", 0);
        String pk = sp.getString("privateKey", "ERROR");
        if(pk.equals("ERROR")){
            Intent intent = new Intent(this, Init.class);
            startActivity(intent);
        }
    }

    //TODO listen for pushes from server, authenticate

}
