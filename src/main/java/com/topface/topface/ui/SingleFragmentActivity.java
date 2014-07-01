package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.topface.topface.R;

public abstract class SingleFragmentActivity extends CustomTitlesBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_fragment_frame);
        Fragment fragment = createFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.loFrame, fragment).commit();
    }

    protected abstract Fragment createFragment();
}
