package com.topface.topface.ui;

import android.os.Bundle;

import com.topface.topface.R;

public class RegistrationActivity extends NoAuthActivity {

    public static final int INTENT_REGISTRATION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_registration);
    }

}
