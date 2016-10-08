package com.passtap.passtapandroid;

import android.content.SharedPreferences;
import android.util.Log;

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

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("privateKey", refreshedToken);
        editor.commit();
    }
}
