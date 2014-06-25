package com.topface.topface.ui;

import android.os.Bundle;

import com.topface.topface.R;

public class PasswordRecoverActivity extends NoAuthActivity {

    public static final int INTENT_RECOVER_PASSWORD = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_recover_password);
    }
}
