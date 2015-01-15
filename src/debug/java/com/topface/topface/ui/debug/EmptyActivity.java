package com.topface.topface.ui.debug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;

/**
 * Пустая активити для дебага, пригождается например для поиска утечек памяти
 */
public class EmptyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Debug.error("EmptyActivity works only in debug mode");
            finish();
            return;
        }
        setContentView(R.layout.ac_fragment_frame);
        Button button = new Button(this);
        button.setText("This's empty view for debug");
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmptyActivity.this, NavigationActivity.class));
            }
        });
        ((ViewGroup) findViewById(R.id.loFrame)).addView(button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.gc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
    }
}
