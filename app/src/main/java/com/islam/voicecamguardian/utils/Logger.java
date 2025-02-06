package com.islam.voicecamguardian.utils;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Logger {

    private static final String TAG = "Logger";
    private static final String LOGS_DIR = "logs";
    private static final int MAX_LOG_LINES = 1000;
    private static final int MAX_RECENT_LOGS = 5; // Maximum number of recent logs to display

    /**
     * Logs a message with a given risk level.
     */
    public static void log(Context context, String channel, String message, RiskLevel riskLevel) {
        try {
            File logsDir = new File(context.getDataDir(), LOGS_DIR);
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            File logFile = new File(logsDir, channel + ".log");

            // Read all lines into a list
            List<String> logLines = new ArrayList<>();
            if (logFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    logLines.add(line);
                }
                reader.close();
            }

            // Create a new log entry with risk level
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String logEntry = timestamp + " - " + channel.toUpperCase() + " - " + riskLevel.name() + " - " + message;

            logLines.add(logEntry);

            // Remove the oldest log if max log lines exceeded
            while (logLines.size() > MAX_LOG_LINES) {
                logLines.remove(0);
            }

            // Write logs back to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, false)); // Overwrite file
            for (String log : logLines) {
                writer.write(log);
                writer.newLine();
            }
            writer.close();

            Log.d(TAG, "Log saved: " + logEntry);
        } catch (Exception e) {
            Log.e(TAG, "Error saving log: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves logs for a specific channel (newest logs first).
     */
    public static List<LogEntry> getLogs(Context context, String channel) {
        List<LogEntry> logs = new ArrayList<>();
        try {
            File logsDir = new File(context.getDataDir(), LOGS_DIR);
            File logFile = new File(logsDir, channel + ".log");

            if (logFile.exists()) {
                List<LogEntry> tempLogs = new ArrayList<>();

                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    tempLogs.add(parseLogEntry(line)); // Convert string to LogEntry object
                }
                reader.close();

                // Reverse order to get the newest logs first
                Collections.reverse(tempLogs);
                logs.addAll(tempLogs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading logs: " + e.getMessage(), e);
        }
        return logs;
    }

    /**
     * Retrieves the most recent logs across all channels (sorted newest first).
     */
    public static List<LogEntry> getRecentLogs(Context context) {
        List<LogEntry> recentLogs = new ArrayList<>();
        try {
            File logsDir = new File(context.getDataDir(), LOGS_DIR);
            if (!logsDir.exists()) {
                return recentLogs;
            }

            File[] logFiles = logsDir.listFiles();
            if (logFiles != null) {
                List<LogEntry> allLogs = new ArrayList<>();

                // Read logs from all files
                for (File logFile : logFiles) {
                    BufferedReader reader = new BufferedReader(new FileReader(logFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        allLogs.add(parseLogEntry(line));
                    }
                    reader.close();
                }

                // Sort logs by timestamp (newest first)
                allLogs.sort((log1, log2) -> Long.compare(log2.timestamp, log1.timestamp));

                // Collect only the most recent logs
                for (LogEntry logEntry : allLogs) {
                    if (recentLogs.size() >= MAX_RECENT_LOGS) break;
                    recentLogs.add(logEntry);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading recent logs: " + e.getMessage(), e);
        }
        return recentLogs;
    }

    /**
     * Parses a log string into a LogEntry object.
     */
    private static LogEntry parseLogEntry(String logLine) {
        try {
            String[] parts = logLine.split(" - ");
            if (parts.length >= 4) {
                String timestampStr = parts[0];
                String channel = parts[1];
                RiskLevel riskLevel = RiskLevel.valueOf(parts[2]);
                String message = parts[3];

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                long timestamp = sdf.parse(timestampStr).getTime();

                return new LogEntry(timestamp, channel, riskLevel, message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing log entry: " + logLine, e);
        }
        return new LogEntry(System.currentTimeMillis(), "UNKNOWN", RiskLevel.SAFE, logLine); // Default fallback
    }

    /**
     * Extracts timestamp from log line.
     */
    private static long extractTimestamp(String logLine) {
        try {
            String timestampStr = logLine.split(" - ")[0]; // Extract timestamp part
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.parse(timestampStr).getTime();
        } catch (Exception e) {
            return System.currentTimeMillis(); // Use current time if parsing fails
        }
    }

    /**
     * Data class for structured log entries.
     */
    public static class LogEntry {
        long timestamp;
        String channel;
        RiskLevel riskLevel;
        String message;

        public LogEntry(long timestamp, String channel, RiskLevel riskLevel, String message) {
            this.timestamp = timestamp;
            this.channel = channel;
            this.riskLevel = riskLevel;
            this.message = message;
        }

        public String getFormattedTimestamp() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        }

        public String getChannel() {
            return channel;
        }

        public String getMessage() {
            return message;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }
    }
}
