package com.islam.voicecamguardian.services;

import android.app.Service;
import android.content.Context;

public abstract class BaseMonitorService extends Service {
    public abstract boolean isMyServiceRunning(Context context);

}