package com.topface.testapp;

import android.*;
import android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import com.topface.testapp.fragments.StartFragment;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity {
    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().add(R.id.content, new StartFragment()).commit();
    }

    public void startFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(R.id.content, fragment).addToBackStack(null).commit();
    }

    public void cancelFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().remove(fragment);
    }
}
