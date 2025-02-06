package com.islam.voicecamguardian;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.islam.voicecamguardian.helpers.NotificationHelper;
import com.islam.voicecamguardian.ui.PasswordActivity;
import com.islam.voicecamguardian.utils.SecurePreferences;

import java.io.File;

public class App extends Application {

    private static final String TAG = "App";
    private SecurePreferences securePreferences;
    private boolean isAppInBackground = true;

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper.createNotificationChannels(this);

        try {
            securePreferences = new SecurePreferences(this);
            Log.d(TAG, "EncryptedSharedPreferences initialized successfully");
        } catch (RuntimeException e) {
            Log.e(TAG, "Keystore issue detected, restarting app", e);
            restartApp();
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            private int activityReferences = 0;
            private boolean isChangingConfigurations = false;

            @Override
            public void onActivityStarted(Activity activity) {
                if (++activityReferences == 1 && !isChangingConfigurations) {
                    // App is in the foreground
                    if (!securePreferences.isAuthenticated() && !(activity instanceof PasswordActivity)) {
                        Intent intent = new Intent(activity, PasswordActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                    }
                    isAppInBackground = false;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {
                isChangingConfigurations = activity.isChangingConfigurations();
                if (--activityReferences == 0 && !isChangingConfigurations) {
                    // App is in the background
                    isAppInBackground = true;
                    securePreferences.setAuthenticated(false); // Reset authentication when app goes to the background
                }
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityResumed(Activity activity) {}

            @Override
            public void onActivityPaused(Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }


    private void restartApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public SecurePreferences getSecurePreferences() {
        return securePreferences;
    }

}
