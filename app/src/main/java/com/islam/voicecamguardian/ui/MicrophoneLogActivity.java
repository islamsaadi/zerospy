package com.islam.voicecamguardian.ui;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.adapters.LogAdapter;
import com.islam.voicecamguardian.interfaces.Refreshable;
import com.islam.voicecamguardian.utils.Logger;

import java.util.List;

public class MicrophoneLogActivity extends BaseActivity implements Refreshable {

    RecyclerView microphoneLogRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Microphone Log");

        microphoneLogRecyclerView = findViewById(R.id.microphoneLogRecyclerView);

        refreshData();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_microphone_log;
    }

    @Override
    public void refreshData() {

        // Get Microphone logs
        List<Logger.LogEntry> microphoneLogs = Logger.getLogs(this, "microphone");

        microphoneLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        microphoneLogRecyclerView.setAdapter(new LogAdapter(microphoneLogs));
        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        microphoneLogRecyclerView.addItemDecoration(divider);
    }
}
