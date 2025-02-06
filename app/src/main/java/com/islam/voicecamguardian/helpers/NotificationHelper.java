package com.islam.voicecamguardian.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.ui.HomeActivity;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    // Service-specific channel IDs and notification IDs
    private static final Map<String, NotificationData> SERVICE_NOTIFICATION_DATA = new HashMap<String, NotificationData>() {{
        put("MICROPHONE", new NotificationData("MICROPHONE_CHANNEL", "Microphone Monitor Notifications", 1001));
        put("CAMERA", new NotificationData("CAMERA_CHANNEL", "Camera Monitor Notifications", 1002));
        put("SMS", new NotificationData("SMS_CHANNEL", "SMS Scan Notifications", 1003));
    }};

    private static final String DEFAULT_CHANNEL_ID = "DEFAULT_CHANNEL";
    private static final String DEFAULT_CHANNEL_NAME = "Default Notifications";
    private static final int DEFAULT_NOTIFICATION_ID = 1000;

    /**
     * Creates all required notification channels.
     * This should be called once during app initialization.
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager == null) {
                Log.e("NotificationHelper", "NotificationManager is null");
                return;
            }

            // Create specific channels
            for (NotificationData data : SERVICE_NOTIFICATION_DATA.values()) {
                NotificationChannel channel = new NotificationChannel(
                        data.getChannelId(),
                        data.getChannelName(),
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifications for " + data.getChannelName().toLowerCase());
                notificationManager.createNotificationChannel(channel);
            }

            // Create default channel
            NotificationChannel defaultChannel = new NotificationChannel(
                    DEFAULT_CHANNEL_ID,
                    DEFAULT_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            defaultChannel.setDescription("Default app notifications");
            notificationManager.createNotificationChannel(defaultChannel);

            Log.d("NotificationHelper", "Notification channels created.");
        }
    }

    /**
     * Shows a dynamic notification.
     *
     * @param context       Application context.
     * @param title         Notification title.
     * @param message       Notification message.
     * @param serviceType   The type of service (e.g., "MICROPHONE", "CAMERA", "SMS").
     */
    public static void showNotification(Context context, String title, String message, String serviceType) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationData data = SERVICE_NOTIFICATION_DATA.getOrDefault(serviceType.toUpperCase(),
                new NotificationData(DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, DEFAULT_NOTIFICATION_ID));

        // Intent to open HomeActivity when the notification is clicked
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, data.getChannelId())
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        if (notificationManager != null) {
            notificationManager.notify(data.getNotificationId(), builder.build());
            Log.d("NotificationHelper", "Notification displayed for " + serviceType + ": " + title);
        } else {
            Log.e("NotificationHelper", "NotificationManager is null");
        }
    }

    /**
     * Retrieves the notification ID for a specific service.
     *
     * @param serviceType The type of service (e.g., "MICROPHONE", "CAMERA", "SMS").
     * @return The notification ID associated with the service, or the default notification ID if not found.
     */
    public static int getNotificationIdForService(String serviceType) {
        return SERVICE_NOTIFICATION_DATA.getOrDefault(serviceType.toUpperCase(),
                new NotificationData(DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, DEFAULT_NOTIFICATION_ID)).getNotificationId();
    }

    public static String getNotificationChannelForService(String serviceType) {
        return SERVICE_NOTIFICATION_DATA.getOrDefault(serviceType.toUpperCase(),
                new NotificationData(DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, DEFAULT_NOTIFICATION_ID)).getChannelId();
    }


    /**
     * A helper class to encapsulate notification data.
     */
    private static class NotificationData {
        private final String channelId;
        private final String channelName;
        private final int notificationId;

        public NotificationData(String channelId, String channelName, int notificationId) {
            this.channelId = channelId;
            this.channelName = channelName;
            this.notificationId = notificationId;
        }

        public String getChannelId() {
            return channelId;
        }

        public String getChannelName() {
            return channelName;
        }

        public int getNotificationId() {
            return notificationId;
        }
    }
}
