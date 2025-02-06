package com.islam.voicecamguardian.utils;

import com.islam.voicecamguardian.R;

public enum RiskLevel {
    HIGH("High Risk", R.color.red),
    MEDIUM("Medium Risk", R.color.orange),
    LOW("Low Risk", R.color.yellow),
    SAFE("Safe", R.color.green),
    UNKNOWN("Unknown", R.color.gray);

    private final String label;
    private final int color;

    RiskLevel(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public int getColor() {
        return color;
    }
}
