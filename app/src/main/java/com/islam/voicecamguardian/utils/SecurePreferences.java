package com.islam.voicecamguardian.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecurePreferences {

    private static final String TAG = "SecurePreferences";
    private static final String PREFS_NAME = "SecureAppPrefs";
    private static final String PASSWORD_KEY = "password";
    private static final String AUTHENTICATED_KEY = "isAuthenticated";

    private SharedPreferences encryptedPrefs;

    public SecurePreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences", e);
            handleCorruptedKeystore(context);
            throw new RuntimeException("Error initializing EncryptedSharedPreferences", e);
        }
    }

    private void handleCorruptedKeystore(Context context) {
        Log.e(TAG, "Detected corrupted Keystore, clearing preferences...");

        // Delete corrupted preferences
        File corruptedPrefs = new File(context.getFilesDir(), "../shared_prefs/" + PREFS_NAME + ".xml");
        if (corruptedPrefs.exists()) {
            corruptedPrefs.delete();
        }
    }

    // Save the password
    public void savePassword(String password) {
        encryptedPrefs.edit().putString(PASSWORD_KEY, password).apply();
    }

    // Retrieve and verify the password
    public boolean verifyPassword(String inputPassword) {
        String storedPassword = encryptedPrefs.getString(PASSWORD_KEY, null);
        return storedPassword != null && storedPassword.equals(inputPassword);
    }

    // Save authentication state
    public void setAuthenticated(boolean isAuthenticated) {
        encryptedPrefs.edit().putBoolean(AUTHENTICATED_KEY, isAuthenticated).apply();
    }

    // Check authentication state
    public boolean isAuthenticated() {
        return encryptedPrefs.getBoolean(AUTHENTICATED_KEY, false);
    }

    public boolean isPasswordSet() {
        return encryptedPrefs.contains("password"); // Check if the password key exists
    }
}
