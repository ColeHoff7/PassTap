package com.passtap.passtapandroid;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.MessageDigest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static com.passtap.passtapandroid.R.id.imageView;
import static com.passtap.passtapandroid.R.id.textView3;

public class Authenticate extends AppCompatActivity {

    String domain = null;
    String salt = null;
    private static final String KEY_NAME = "my_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    /** Alias for our key in the Android Key Store. */
    private static final byte[] SECRET_BYTE_ARRAY = new byte[] {1, 2, 3, 4, 5, 6};

    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;

    /**
     * If the user has unlocked the device Within the last this number of seconds,
     * it can be considered as an authenticator.
     */
    private static final int AUTHENTICATION_DURATION_SECONDS = 30;

    private KeyguardManager mKeyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);
        Bundle b = getIntent().getExtras();
        if(b != null){
            domain = b.getString("domain");
            salt = b.getString("salt");
        }
        else{
            b = getIntent().getExtras();
            if(b != null) domain = b.getString("domain");
            else{
                try {
                    throw new Exception("Failed to get domain data");
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        final TextView mTextView = (TextView) findViewById(textView3);
        mTextView.setText("Scan Fingerprint to Authenticate for " + domain);

        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        //Passcode if no fingerprint sensor
        if (!fingerprintManager.isHardwareDetected() || !fingerprintManager.hasEnrolledFingerprints()) {
            final ImageView iv = (ImageView) findViewById(imageView);
            iv.setVisibility(View.GONE);
            final TextView tv = (TextView) findViewById(textView3);
            tv.setText("Enter Passcode");
            mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (!mKeyguardManager.isKeyguardSecure()) {
                // Show a message that the user hasn't set up a lock screen.
                Toast.makeText(this,
                        "Secure lock screen hasn't set up.\n"
                                + "Go to 'Settings -> Security -> Screenlock' to set up a lock screen",
                        Toast.LENGTH_LONG).show();
                return;
            }
            createKey();
            tryEncrypt();
        }
        // if they have a fingerprint sensor, use it
        else {

            if (!keyguardManager.isKeyguardSecure()) {

                Toast.makeText(this,
                        "Lock screen security not enabled in Settings",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.USE_FINGERPRINT) !=
                    PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Fingerprint authentication permission not enabled",
                        Toast.LENGTH_LONG).show();

                return;
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {

                // This happens when no fingerprints are registered.
                Toast.makeText(this,
                        "Register at least one fingerprint in Settings",
                        Toast.LENGTH_LONG).show();
                return;
            }

            generateKey();


            if (cipherInit()) {
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
                FingerprintHandler helper = new FingerprintHandler(this, Authenticate.this);
                helper.startAuth(fingerprintManager, cryptoObject);
            }
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with device credentials within the last X seconds.
     */
    private void createKey() {
        // Generate a key to decrypt payment credentials, tokens, etc.
        // This will most likely be a registration step for the user when they are setting up your app.
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    // Require that the user has unlocked in the last 30 seconds
                    .setUserAuthenticationValidityDurationSeconds(AUTHENTICATION_DURATION_SECONDS)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException | KeyStoreException
                | CertificateException | IOException e) {
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }

    private void showAuthenticationScreen() {
        // Create the Confirm Credentials screen. You can customize the title and description. Or
        // we will provide a generic one for you if you leave it null
        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which is
     * only works if the user has just authenticated via device credentials.
     */
    private boolean tryEncrypt() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // Try encrypting something, it will only work if the user authenticated within
            // the last AUTHENTICATION_DURATION_SECONDS seconds.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            cipher.doFinal(SECRET_BYTE_ARRAY);

            // If the user has recently authenticated, you will reach here.
            showAlreadyAuthenticated();
            return true;
        } catch (UserNotAuthenticatedException e) {
            // User is not authenticated, let's authenticate with device credentials.
            showAuthenticationScreen();
            return false;
        } catch (KeyPermanentlyInvalidatedException e) {
            // This happens if the lock screen has been disabled or reset after the key was
            // generated after the key was generated.
            Toast.makeText(this, "Keys are invalidated after created. Retry the authentication\n"
                            + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return false;
        } catch (BadPaddingException | IllegalBlockSizeException | KeyStoreException |
                CertificateException | UnrecoverableKeyException | IOException
                | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                if (tryEncrypt()) {
                    sendAuthentication();
                }
            } else {
                // The user canceled or didn’t complete the lock screen
                // operation. Go to error/cancellation flow.
            }
        }
    }

    private void showAlreadyAuthenticated() {
        sendAuthentication();
    }

    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }



    public void sendAuthentication() {
        //send server authentication
        RequestQueue queue = Volley.newRequestQueue(this);
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
            SharedPreferences shp = getSharedPreferences("accountID", 0);
            String id = shp.getString("accountID", "ERROR");
            String pass = get_SHA_512_SecurePassword(pk, salt+domain);
            url += pass + "&v3=" + domain + "&v4=" + id;
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

    public String get_SHA_512_SecurePassword(String passwordToHash, String   salt){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes("UTF-8"));
            byte[] bytes = md.digest(passwordToHash.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return generatedPassword;
    }
}
