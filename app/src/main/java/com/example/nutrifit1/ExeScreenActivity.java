package com.example.nutrifit1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class ExeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exe_screen);

        new Handler().postDelayed(() -> {
            File userDataFile = new File(getFilesDir(), "user_data.json");

            Intent intent;
            if (!userDataFile.exists()) {
                intent = new Intent(ExeScreenActivity.this, UserInformActivity.class);
            } else {
                intent = new Intent(ExeScreenActivity.this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        }, 1000);
    }
}
