package com.islam.voicecamguardian.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.utils.Logger;
import com.islam.voicecamguardian.utils.RiskLevel;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final List<Logger.LogEntry> logs;

    public LogAdapter(List<Logger.LogEntry> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        Logger.LogEntry logEntry = logs.get(position);

        String timestamp = logEntry.getFormattedTimestamp();
        String channel = logEntry.getChannel();
        String riskLabel = logEntry.getRiskLevel().getLabel();
        String content = logEntry.getMessage();

        holder.logDateTime.setText(timestamp);
        holder.logChannel.setText(channel.toUpperCase());
        holder.riskTitle.setText(riskLabel);
        holder.logTextView.setText(content);

        // Assign Risk Level Color
        RiskLevel riskLevel = getRiskLevelFromLabel(riskLabel);
        holder.logRiskIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), riskLevel.getColor()));
        holder.riskTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), riskLevel.getColor()));
    }


    // Convert Risk Label to RiskLevel Enum
    private RiskLevel getRiskLevelFromLabel(String label) {
        for (RiskLevel level : RiskLevel.values()) {
            if (level.getLabel().equalsIgnoreCase(label)) {
                return level;
            }
        }
        return RiskLevel.SAFE; // Default to SAFE if not found
    }


    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView logChannel, riskTitle, logDateTime, logTextView;
        View logRiskIndicator;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logChannel = itemView.findViewById(R.id.logChannel);       // Channel Name
            riskTitle = itemView.findViewById(R.id.riskTitle);       // Risk Title
            logDateTime = itemView.findViewById(R.id.logDateTime);     // Date & Time
            logTextView = itemView.findViewById(R.id.logTextView);     // Log Content
            logRiskIndicator = itemView.findViewById(R.id.logRiskIndicator); // Risk Level Indicator
        }
    }

}
