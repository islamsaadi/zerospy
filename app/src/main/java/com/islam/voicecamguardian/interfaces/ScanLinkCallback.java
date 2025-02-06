package com.islam.voicecamguardian.interfaces;

import com.islam.voicecamguardian.utils.ScanResult;

public interface ScanLinkCallback {
    void onSuccess(ScanResult result);
    void onFailure(Exception error);
}
