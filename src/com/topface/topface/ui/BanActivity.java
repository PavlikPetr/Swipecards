package com.topface.topface.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.topface.topface.R;

/**
 * Created with IntelliJ IDEA.
 * User: Ilya Vorobiev
 * Date: 26.10.12
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class BanActivity extends Activity {
    public static final String BANNING_INTENT = "banning_intent";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.ban);
        String message = getIntent().getStringExtra(BANNING_INTENT);
        TextView messageContainer = (TextView)findViewById(R.id.banned_message);
        messageContainer.setText(message);
    }
}
