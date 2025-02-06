package com.islam.voicecamguardian.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.receivers.NotificationReceiver;
import com.islam.voicecamguardian.utils.Utility;

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected MaterialToolbar toolbar;

    private NotificationReceiver notificationReceiver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);

        int layoutResId = getLayoutResourceId();
        if (layoutResId != 0) {
            View content = getLayoutInflater().inflate(layoutResId, null);
            FrameLayout contentFrame = findViewById(R.id.contentFrame);
            if (contentFrame != null) {
                contentFrame.addView(content);
            } else {
                throw new RuntimeException("Content frame not found in BaseActivity layout.");
            }
        }

        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            throw new RuntimeException("Toolbar with ID 'toolbar' not found. Check your layout.");
        }

        setSupportActionBar(toolbar);

        // Set navigation icon
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setTint(getResources().getColor(R.color.white, getTheme()));
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                // Handle navigation item clicks
                return handleNavigationItemSelected(item.getItemId());
            });
        }

        // Set toolbar navigation icon click behavior
        toolbar.setNavigationOnClickListener(view -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });


        notificationReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter(Utility.getPackageName(this)+"REFRESH_LOGS");
        registerReceiver(notificationReceiver, filter);
    }

    // Abstract method to provide the layout ID
    @LayoutRes
    protected abstract int getLayoutResourceId();

    // Abstract method for handling navigation item clicks
    protected boolean handleNavigationItemSelected(int itemId) {
        if (itemId == R.id.home) {
            startActivity(new Intent(this, HomeActivity.class));
            return true;
        } else if (itemId == R.id.menu_camera) {
            startActivity(new Intent(this, CameraLogActivity.class));
            return true;
        } else if (itemId == R.id.menu_microphone) {
            startActivity(new Intent(this, MicrophoneLogActivity.class));
            return true;
        } else if (itemId == R.id.menu_sms) {
            startActivity(new Intent(this, SmsLogActivity.class));
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }
}
