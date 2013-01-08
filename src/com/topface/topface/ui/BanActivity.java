package com.topface.topface.ui;

import android.os.Bundle;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedActivity;

public class BanActivity extends TrackedActivity {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_BAN = 1;
    public static final int TYPE_FLOOD = 2;

    public static final String INTENT_TYPE = "message_type";

    public static final String BANNING_TEXT_INTENT = "banning_intent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ban);
        TextView titleContainer = (TextView) findViewById(R.id.banned_title);
        TextView messageContainer = (TextView) findViewById(R.id.banned_message);

        int type = getIntent().getIntExtra(INTENT_TYPE,TYPE_UNKNOWN);

        String title = "";
        String message = "";
        switch(type) {
            case TYPE_BAN:
                title = getString(R.string.ban_title);
                message = getIntent().getStringExtra(BANNING_TEXT_INTENT);
                break;
            case TYPE_FLOOD:

                break;
            default:
                break;
        }

        titleContainer.setText(title);
        messageContainer.setText(message);
    }
}
