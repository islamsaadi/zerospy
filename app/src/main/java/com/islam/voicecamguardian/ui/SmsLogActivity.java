package com.islam.voicecamguardian.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.adapters.LogAdapter;
import com.islam.voicecamguardian.interfaces.Refreshable;
import com.islam.voicecamguardian.utils.Logger;

import java.util.List;

public class SmsLogActivity extends BaseActivity implements Refreshable {

    RecyclerView smsLogRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("SMS Log");

        smsLogRecyclerView = findViewById(R.id.smsLogRecyclerView);

        refreshData();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_sms_log;
    }

    @Override
    public void refreshData() {

        // Get SMS logs
        List<Logger.LogEntry> smsLogs = Logger.getLogs(this, "sms");

        smsLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsLogRecyclerView.setAdapter(new LogAdapter(smsLogs));
        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        smsLogRecyclerView.addItemDecoration(divider);
    }
}
