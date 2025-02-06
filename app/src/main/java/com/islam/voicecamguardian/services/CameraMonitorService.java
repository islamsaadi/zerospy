package com.islam.voicecamguardian.services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.app.ActivityManager;

import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.helpers.NotificationHelper;
import com.islam.voicecamguardian.utils.Logger;
import com.islam.voicecamguardian.utils.RiskLevel;
import com.islam.voicecamguardian.utils.Utility;

public class CameraMonitorService extends BaseMonitorService{

    private static final String TAG = "CameraUsageService";
    private static final String SERVICE_ID = "CAMERA";

    private CameraManager cameraManager;
    private CameraManager.AvailabilityCallback cameraCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created.");

        // Initialize CameraManager
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Set up the callback to detect camera usage
        cameraCallback = new CameraManager.AvailabilityCallback() {
            private final Context context = getApplicationContext();

            @Override
            public void onCameraUnavailable(String cameraId) {
                super.onCameraUnavailable(cameraId);
                Log.d(TAG, "Camera is in use: " + cameraId);

                Logger.log(context, "camera", "Camera accessed by an app.", RiskLevel.MEDIUM);

                // Show notification about camera usage
                NotificationHelper.showNotification(
                        CameraMonitorService.this,
                        "Camera Detected",
                        "Camera is being used by an app.",
                        SERVICE_ID
                );

                // Send broadcast to refresh logs
                Intent broadcastIntent = new Intent(Utility.getPackageName(context) + ".REFRESH_LOGS");
                context.sendBroadcast(broadcastIntent);
            }

            @Override
            public void onCameraAvailable(String cameraId) {
                super.onCameraAvailable(cameraId);
                Log.d(TAG, "Camera is available: " + cameraId);
            }
        };

        // Register the callback
        if (cameraManager != null) {
            cameraManager.registerAvailabilityCallback(cameraCallback, null);
            Log.d(TAG, "Camera availability callback registered.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called.");

        // Ensure the service runs as a foreground service
        startForeground(NotificationHelper.getNotificationIdForService(SERVICE_ID), createForegroundNotification());

        // Return START_STICKY to restart the service if it gets killed
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the camera availability callback
        if (cameraManager != null && cameraCallback != null) {
            cameraManager.unregisterAvailabilityCallback(cameraCallback);
            Log.d(TAG, "Camera availability callback unregistered.");
        }
        Log.d(TAG, "Service destroyed.");
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
                if (CameraMonitorService.class.getName().equals(service.service.getClassName())) {
                    Log.d("isMyServiceRunning", "CAMERA TRUE");
                    return true;
                }
            }
        }
        Log.d("isMyServiceRunning", "CAMERA FALSE");
        return false;
    }

    /**
     * Creates the notification for the foreground service.
     *
     * @return Notification for the foreground service.
     */
    private Notification createForegroundNotification() {
        return new NotificationCompat.Builder(this, NotificationHelper.getNotificationChannelForService(SERVICE_ID))
                .setContentTitle("Camera Monitoring")
                .setContentText("Monitoring camera usage in the background.")
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }
}
