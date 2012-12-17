package com.topface.topface.ui;

import android.os.Bundle;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedActivity;

public class BanActivity extends TrackedActivity {
    public static final String BANNING_INTENT = "banning_intent";
    public static final String MESSAGE = "ban_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.ban);
        String message = getIntent().getStringExtra(BANNING_INTENT);
        TextView messageContainer = (TextView) findViewById(R.id.banned_message);
        messageContainer.setText(message);
    }
}
