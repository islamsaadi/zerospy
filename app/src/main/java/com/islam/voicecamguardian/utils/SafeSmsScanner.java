package com.islam.voicecamguardian.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.islam.voicecamguardian.helpers.NotificationHelper;
import com.islam.voicecamguardian.interfaces.ScanLinkCallback;

import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.*;
public class SafeSmsScanner {

    private static final String TAG = "SafeSmsScanner";
    private static final String URLSCAN_API_KEY = com.islam.voicecamguardian.BuildConfig.API_KEY;
    private static final String URLSCAN_API_URL = "https://endpoint.apivoid.com/urlrep/v1/pay-as-you-go/";



    // Function to scan links in SMS body
    public static void scanLinksInSms(Context context, String smsBody) {
        ArrayList<String> links = extractLinks(smsBody);

        if (links.isEmpty()) {
            Log.d(TAG, "No links found in SMS.");
            return;
        }

        for (String link : links) {
            SafeSmsScanner.scanLink(link, new ScanLinkCallback() {
                @Override
                public void onSuccess(ScanResult result) {
                    Log.d("ScanResult", result.getMessage()  + " " + result.getRiskLevel().getLabel());
                    Logger.log(context, "sms", "Scanned SMS => " + result.getMessage(), result.getRiskLevel());
                    NotificationHelper.showNotification(
                            context,
                            "SMS SCAN RESULT",
                            result.getMessage(),
                            "SMS"
                    );

                    Intent broadcastIntent = new Intent(Utility.getPackageName(context)+"REFRESH_LOGS");
                    context.sendBroadcast(broadcastIntent);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("ScanResult", "Error: " + e.getMessage(), e);
                }
            });;

        }
    }

    // Extract links from SMS body
    private static ArrayList<String> extractLinks(String text) {
        ArrayList<String> links = new ArrayList<>();

        // Regex for matching URLs
        String urlRegex = "http[s]?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(/[a-zA-Z0-9._~:/?#[\\\\]@!$&'()*+,;=%-]*)?";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            links.add(matcher.group());
        }

        return links;
    }


    private static void scanLink(String link, ScanLinkCallback callback) {
        try {
            Log.d(TAG, "Scanning link: " + link);

            OkHttpClient client = new OkHttpClient();

            // Build the request URL with query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(URLSCAN_API_URL).newBuilder();
            urlBuilder.addQueryParameter("key", URLSCAN_API_KEY);
            urlBuilder.addQueryParameter("url", link);
            // Build the HTTP request
            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Asynchronous execution
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error scanning link: " + e.getMessage(), e);
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API request failed: " + response.code() + " - " + response.message());
                        callback.onFailure(new IOException("API request failed: " + response.code() + " - " + response.message()));
                        return;
                    }

                    // Parse response body
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response Body: " + responseBody);

                    try {
                        JSONObject responseJson = new JSONObject(responseBody);

                        // Navigate to risk score in the JSON structure
                        if (responseJson.has("data")) {
                            JSONObject data = responseJson.getJSONObject("data");
                            if (data.has("report")) {
                                JSONObject report = data.getJSONObject("report");
                                if (report.has("risk_score")) {
                                    JSONObject riskScore = report.getJSONObject("risk_score");
                                    if (riskScore.has("result")) {
                                        int score = riskScore.getInt("result");

                                        ScanResult result = getScanResult(score, link);

                                        callback.onSuccess(result);
                                        return;
                                    }
                                }
                            }
                        }

                        ScanResult result = new ScanResult(RiskLevel.UNKNOWN, "No risk score available for link: | " + link);
                        // Default case if risk score is not found
                        callback.onSuccess(result);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        callback.onFailure(e);
                    }
                }

            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing request: " + e.getMessage(), e);
            callback.onFailure(e);
        }
    }

    @NonNull
    private static ScanResult getScanResult(int score, String link) {
        ScanResult result;

        // Evaluate the risk score
        if (score >= 50) {
            result = new ScanResult(RiskLevel.HIGH, "High risk detected | " + link);
        } else if (score >= 20) {
            result = new ScanResult(RiskLevel.MEDIUM, "Medium risk detected | " + link);
        } else if (score > 5) {
            result = new ScanResult(RiskLevel.LOW, "Low risk detected | " + link);
        } else {
            result = new ScanResult(RiskLevel.SAFE, "No risk detected | " + link);
        }
        return result;
    }

}
