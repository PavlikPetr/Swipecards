package com.topface.topface.ui.debug;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.ui.dialogs.CitySearchPopup;

public class EmptyActivity extends AppCompatActivity {
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
                doSomething();
            }
        });
        ((ViewGroup) findViewById(R.id.loFrame)).addView(button);
    }

    private void doSomething() {
        CitySearchPopup popup1 = new CitySearchPopup();
        popup1.show(getSupportFragmentManager(), CitySearchPopup.class.getName());
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
