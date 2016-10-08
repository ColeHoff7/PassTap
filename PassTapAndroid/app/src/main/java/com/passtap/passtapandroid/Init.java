package com.passtap.passtapandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Init extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
    }

    /** Called when the user clicks the Start button */
    public void initialSetup(View view) {
        Intent intent = new Intent(this, InitializeBrowserActivity.class);
        startActivity(intent);
        finish();
    }
}
