package com.islam.voicecamguardian.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.adapters.LogAdapter;
import com.islam.voicecamguardian.interfaces.Refreshable;
import com.islam.voicecamguardian.services.BaseMonitorService;
import com.islam.voicecamguardian.services.CameraMonitorService;
import com.islam.voicecamguardian.services.MicrophoneMonitorService;
import com.islam.voicecamguardian.receivers.SmsScanReceiver;
import com.islam.voicecamguardian.managers.PermissionManager;
import com.islam.voicecamguardian.utils.Logger;

import java.util.List;
import java.util.Objects;

public class HomeActivity extends BaseActivity implements Refreshable {

    private static final String TAG = "HomeActivity";
    private static final String TOGGLE_PREFS_NAME = "ToggleStates";
    private static final String SMS_TOGGLE_KEY = "smsToggle";
    private static final String CAMERA_TOGGLE_KEY = "cameraToggle";
    private static final String MICROPHONE_TOGGLE_KEY = "microphoneToggle";

    private SharedPreferences sharedPreferences;

    private PermissionManager permissionManager;


    private SmsScanReceiver smsScanReceiver;

    // Permission request launcher
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    Log.d(TAG, "All permissions granted.");
                } else {
                    Log.d(TAG, "Some permissions were denied.");
                    permissionManager.handlePermissionDenied(this);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(TOGGLE_PREFS_NAME, MODE_PRIVATE);

        // Initialize PermissionManager
        permissionManager = new PermissionManager(this, requestPermissionsLauncher);


        smsScanReceiver = new SmsScanReceiver(this);

        // Initialize service toggles with their specific permissions
        setupReceiverToggle(
                R.id.smsServiceToggle,
                SMS_TOGGLE_KEY,
                "SMS Scan Service",
                "SMS"
        );
        setupServiceToggle(
                R.id.cameraServiceToggle,
                CameraMonitorService.class,
                CAMERA_TOGGLE_KEY,
                "Camera Monitor Service",
                "Camera"
        );
        setupServiceToggle(
                R.id.microphoneServiceToggle,
                MicrophoneMonitorService.class,
                MICROPHONE_TOGGLE_KEY,
                "Microphone Monitor Service",
                "Microphone"
        );
    }


    private void setupReceiverToggle(int toggleId, String toggleKey, String receiverName, String receiverType) {
        SwitchMaterial toggle = findViewById(toggleId);

        boolean isEnabled = sharedPreferences.getBoolean(toggleKey, false);
        toggle.setChecked(isEnabled);

        toggle.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                // Check permissions for the receiver
                if (permissionManager.hasPermissions(receiverType)) {
                    saveToggleState(toggleKey, true);
                    if(Objects.equals(toggleKey, SMS_TOGGLE_KEY)) {
                        smsScanReceiver.startMonitoring();
                    }
                    Toast.makeText(this, receiverName + " Started", Toast.LENGTH_SHORT).show();
                } else {
                    toggle.setChecked(false); // Revert the toggle if permissions are not granted
                    permissionManager.requestPermissions(receiverType);
                }
            } else {
                // Stop the receiver
                saveToggleState(toggleKey, false);
                if(Objects.equals(toggleKey, SMS_TOGGLE_KEY)) {
                    smsScanReceiver.stopMonitoring();
                }
                Toast.makeText(this, receiverName + " Stopped", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up a toggle switch for a specific service.
     *
     * @param toggleId      The ID of the toggle switch.
     * @param serviceClass  The service class to start/stop.
     * @param toggleKey     The shared preference key for saving the toggle state.
     * @param serviceName   The display name of the service.
     * @param serviceType   The type of service (e.g., "SMS", "Camera", "Microphone").
     */
    private void setupServiceToggle(int toggleId, Class<? extends BaseMonitorService> serviceClass, String toggleKey, String serviceName, String serviceType) {
        SwitchMaterial toggle = findViewById(toggleId);

        boolean isEnabled = sharedPreferences.getBoolean(toggleKey, false);
        toggle.setChecked(isEnabled);

        toggle.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                // Check permissions for the service
                if (permissionManager.hasPermissions(serviceType)) {
                    saveToggleState(toggleKey, true);
                    startServiceWithToast(serviceClass, serviceName + " Started");
                } else {
                    toggle.setChecked(false); // Revert the toggle if permissions are not granted
                    permissionManager.requestPermissions(serviceType);
                }
            } else {
                // Stop the service
                stopServiceWithToast(serviceClass, serviceName + " Stopped");
                saveToggleState(toggleKey, false);
            }
        });
    }
    private void saveToggleState(String key, boolean state) {
        sharedPreferences.edit().putBoolean(key, state).apply();
    }
    private void startServiceWithToast(Class<? extends BaseMonitorService>  serviceClass, String message) {
        try {
            // Check if the service is already running
            BaseMonitorService serviceInstance = serviceClass.getDeclaredConstructor().newInstance();
            if (!serviceInstance.isMyServiceRunning(this)) {
                // Start the service if it's not running
                Intent intent = new Intent(this, serviceClass);
                startForegroundService(intent);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Service is already running.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ServiceError", "Failed to start service: " + e.getMessage());
        }
    }

    private void stopServiceWithToast(Class<?> serviceClass, String message) {
        Intent intent = new Intent(this, serviceClass);
        stopService(intent);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(); // Refresh logs when returning to the activity
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.getBooleanExtra("NOTIFICATION_CLICKED", false)) {
            refreshData(); // Refresh logs after notification click
        }
    }

    public void refreshData() {
        List<Logger.LogEntry> recentLogs = Logger.getRecentLogs(this);
        RecyclerView recentLogsRecyclerView = findViewById(R.id.recentLogsRecyclerView);
        recentLogsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentLogsRecyclerView.setAdapter(new LogAdapter(recentLogs));
        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recentLogsRecyclerView.addItemDecoration(divider);
    }

}
