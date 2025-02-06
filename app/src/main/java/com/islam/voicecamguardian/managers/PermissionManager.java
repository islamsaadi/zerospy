package com.islam.voicecamguardian.managers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    private final Context context;
    private final ActivityResultLauncher<String[]> permissionLauncher;

    // Permissions for each service
    private static final Map<String, String[]> SERVICE_PERMISSIONS = new HashMap<String, String[]>() {{
        put("SMS", new String[]{
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.INTERNET, // Needed for API calls
                Manifest.permission.POST_NOTIFICATIONS // For notifications
        });
        put("Camera", new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.FOREGROUND_SERVICE, // Required for background monitoring
                Manifest.permission.POST_NOTIFICATIONS // For notifications
        });
        put("Microphone", new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.FOREGROUND_SERVICE, // Required for background monitoring
                Manifest.permission.POST_NOTIFICATIONS // For notifications
        });
    }};

    public PermissionManager(Context context, ActivityResultLauncher<String[]> permissionLauncher) {
        this.context = context;
        this.permissionLauncher = permissionLauncher;
    }

    /**
     * Check if all required permissions for a specific service are granted.
     *
     * @param serviceName The name of the service (e.g., "SMS", "Camera", "Microphone").
     * @return true if all permissions for the service are granted, false otherwise.
     */
    public boolean hasPermissions(String serviceName) {
        String[] requiredPermissions = SERVICE_PERMISSIONS.get(serviceName);
        if (requiredPermissions == null) {
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request permissions for a specific service.
     *
     * @param serviceName The name of the service (e.g., "SMS", "Camera", "Microphone").
     */
    public void requestPermissions(String serviceName) {
        String[] requiredPermissions = SERVICE_PERMISSIONS.get(serviceName);
        if (requiredPermissions == null) {
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }

    /**
     * Handle permission denial by showing a dialog and directing the user to app settings.
     *
     * @param activity The current activity.
     */
    public void handlePermissionDenied(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("Permissions Denied")
                .setMessage("Some permissions are essential for the app to function. Please enable them in settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings(activity))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Open the app's settings page to manually grant permissions.
     *
     * @param activity The current activity.
     */
    private void openAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
