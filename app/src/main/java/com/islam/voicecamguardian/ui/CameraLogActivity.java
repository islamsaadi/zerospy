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

public class CameraLogActivity extends BaseActivity implements Refreshable {

    RecyclerView cameraLogRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Camera Log");

        cameraLogRecyclerView = findViewById(R.id.cameraLogRecyclerView);

        refreshData();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_camera_log;
    }

    @Override
    public void refreshData() {

        // Get Camera logs
        List<Logger.LogEntry> cameraLogs = Logger.getLogs(this, "camera");

        cameraLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cameraLogRecyclerView.setAdapter(new LogAdapter(cameraLogs));
        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        cameraLogRecyclerView.addItemDecoration(divider);
    }
}
