package com.islam.voicecamguardian.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.content.SharedPreferences;

import com.islam.voicecamguardian.helpers.NotificationHelper;
import com.islam.voicecamguardian.utils.SafeSmsScanner;

public class SmsScanReceiver {

    private static final String TAG = "SmsScanReceiver";
    private static final String SERVICE_ID = "SMS";

    private BroadcastReceiver smsReceiver;
    private SharedPreferences sharedPreferences;

    private final Context context;

    public SmsScanReceiver(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("ToggleStates", Context.MODE_PRIVATE);
    }

    public void startMonitoring() {
        Log.d(TAG, "Starting SMS Monitoring...");

        // Show notification (if needed)
        NotificationHelper.showNotification(
                context,
                "SMS Monitoring",
                "Monitoring SMS ...",
                SERVICE_ID
        );

        // Register SMS Receiver
        if (smsReceiver == null) {
            smsReceiver = new SmsReceiver();
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            context.registerReceiver(smsReceiver, filter);
            Log.d(TAG, "SMS Receiver registered.");
        }
    }

    public void stopMonitoring() {
        Log.d(TAG, "Stopping SMS Monitoring...");
        if (smsReceiver != null) {
            context.unregisterReceiver(smsReceiver);
            smsReceiver = null;
            Log.d(TAG, "SMS Receiver unregistered.");
        }
    }

    // BroadcastReceiver for handling SMS
    public static class SmsReceiver extends BroadcastReceiver {
        private static final String TAG = "SmsReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Object[] pdus = (Object[]) extras.get("pdus");
                String format = extras.getString("format"); // Get the SMS format
                if (pdus != null) {

                    StringBuilder fullMessage = new StringBuilder();
                    String sender = null;

                    // Iterate through PDUs to reconstruct the SMS
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);

                        // Get the sender (same for all parts)
                        if (sender == null) {
                            sender = smsMessage.getDisplayOriginatingAddress();
                        }

                        // Append the message part
                        fullMessage.append(smsMessage.getMessageBody());
                    }

                    // Log the complete message
                    Log.d(TAG, "SMS received from: " + sender);
                    Log.d(TAG, "Complete message: " + fullMessage.toString());

                    // Scan for scam links
                    if (fullMessage.toString().contains("http")) {
                        SafeSmsScanner.scanLinksInSms(context, fullMessage.toString());
                    }
                }
            }
        }
    }
}
