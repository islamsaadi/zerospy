package com.islam.voicecamguardian.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.islam.voicecamguardian.interfaces.Refreshable;
import com.islam.voicecamguardian.utils.Utility;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((Utility.getPackageName(context)+"REFRESH_LOGS").equals(intent.getAction())) {
            if (context instanceof Refreshable) {
                ((Refreshable) context).refreshData();
            }
        }
    }
}