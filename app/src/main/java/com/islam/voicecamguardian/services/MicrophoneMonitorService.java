package com.islam.voicecamguardian.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.helpers.NotificationHelper;
import com.islam.voicecamguardian.utils.Logger;
import com.islam.voicecamguardian.utils.RiskLevel;
import com.islam.voicecamguardian.utils.Utility;

public class MicrophoneMonitorService extends BaseMonitorService {

    private static final String TAG = "MicrophoneMonitorService";
    private static final String SERVICE_ID = "MICROPHONE";
    private static final int FOREGROUND_NOTIFICATION_ID = 1002; // Unique ID for the service's notification

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created.");

        // Initialize AudioManager and set up the AudioFocusChangeListener
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioFocusChangeListener = focusChange -> {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                Log.d(TAG, "Audio focus gained - possible microphone usage.");
                handleMicrophoneUsageDetected();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                Log.d(TAG, "Audio focus lost.");
            }
        };

        // Start monitoring audio focus
        startMonitoringAudioFocus();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called.");

        // Start the service as a foreground service
        startForeground(NotificationHelper.getNotificationIdForService(SERVICE_ID), createForegroundNotification());

        // Return START_STICKY to restart the service if it gets killed
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Abandon audio focus when the service is destroyed
        if (audioManager != null && audioFocusChangeListener != null) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
            Log.d(TAG, "Audio focus abandoned.");
        }
        Log.d(TAG, "Service destroyed.");
    }

    private void startMonitoringAudioFocus() {
        if (audioManager != null) {
            int result = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.d(TAG, "Started monitoring audio focus.");
            } else {
                Log.e(TAG, "Failed to request audio focus.");
            }
        }
    }

    private void handleMicrophoneUsageDetected() {
        Logger.log(this, "microphone", "Microphone usage detected.", RiskLevel.LOW);

        // Show notification about microphone usage
        NotificationHelper.showNotification(
                this,
                "Microphone Detected",
                "An app may be using the microphone.",
                SERVICE_ID
        );

        // Send broadcast to refresh logs
        Intent broadcastIntent = new Intent(Utility.getPackageName(this) + ".REFRESH_LOGS");
        sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    @Override
    public boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (MicrophoneMonitorService.class.getName().equals(service.service.getClassName())) {
                    Log.d("isMyServiceRunning", "MIC TRUE");
                    return true;
                }
            }
        }
        Log.d("isMyServiceRunning", "MIC FALSE");
        return false;
    }

    /**
     * Creates the notification for the foreground service.
     *
     * @return Notification for the foreground service.
     */
    private Notification createForegroundNotification() {
        return new NotificationCompat.Builder(this, NotificationHelper.getNotificationChannelForService(SERVICE_ID))
                .setContentTitle("Microphone Monitoring")
                .setContentText("Monitoring microphone usage in the background.")
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }
}
