package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;

public class MainActivity extends Activity {
    // Data

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_main);

        //startService(new Intent(getApplicationContext(), ConnectionService.class));

        if (Data.isSSID())
            startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
        else
            startActivity(new Intent(getApplicationContext(), AuthActivity.class));

        finish();
    }

    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

}