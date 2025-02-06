package com.islam.voicecamguardian.utils;

public class ScanResult {
    private final RiskLevel riskLevel;
    private final String message;

    public ScanResult(RiskLevel riskLevel, String message) {
        this.riskLevel = riskLevel;
        this.message = message;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getMessage() {
        return message;
    }
}
