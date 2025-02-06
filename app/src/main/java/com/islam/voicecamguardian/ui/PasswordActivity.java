package com.islam.voicecamguardian.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.islam.voicecamguardian.R;
import com.islam.voicecamguardian.utils.SecurePreferences;

public class PasswordActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PASSWORD_KEY = "encryptedPassword";
    private static final String ENCRYPTION_KEY = "encryptionKey";

    SecurePreferences securePreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        securePreferences = new SecurePreferences(this);

        setAppNameColors();

        TextView passwordTitle = findViewById(R.id.passwordTitle);
        // Check if a password is already set
        boolean isPasswordSet = securePreferences.isPasswordSet();

        // Update the TextView based on the password state
        if (isPasswordSet) {
            passwordTitle.setText("Enter Password");
        } else {
            passwordTitle.setText("Set New Password");
        }


        EditText passwordInput = findViewById(R.id.passwordInput);
        Button submitButton = findViewById(R.id.submitPasswordButton);


        submitButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isPasswordSet) {
                // Verify the entered password
                if (securePreferences.verifyPassword(password)) {
                    goToMainActivity();
                } else {
                    Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Set a new password
                securePreferences.savePassword(password);
                Toast.makeText(this, "Password set successfully!", Toast.LENGTH_SHORT).show();
                goToMainActivity();
            }
        });
    }

    private void setAppNameColors() {

        TextView appName = findViewById(R.id.app_name);

        SpannableString spannableString = new SpannableString("ZeroSpy");

        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.logo_left_color)), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // "Zero"
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.logo_right_color)), 4, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // "Spy"

        appName.setText(spannableString);
    }


    private void goToMainActivity() {
        securePreferences.setAuthenticated(true);
        // Navigate to MainActivity
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close PasswordActivity
    }
}
