package com.topface.NotificationSender;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import org.json.JSONObject;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    public static final int GCM_TYPE_MESSAGE = 0;
    public static final int GCM_TYPE_SYMPATHY = 1;
    public static final int GCM_TYPE_LIKE = 2;
    public static final int GCM_TYPE_GUESTS = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initViews();
    }

    public void initViews() {
        Button likes = (Button) findViewById(R.id.simpathy);
        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(GCM_TYPE_LIKE);
            }
        });

        Button messages = (Button) findViewById(R.id.message);
        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(GCM_TYPE_MESSAGE);
            }
        });

        Button sympathy = (Button) findViewById(R.id.mutual);
        sympathy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(GCM_TYPE_SYMPATHY);
            }
        });

        Button visitors = (Button) findViewById(R.id.visitors);
        visitors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(GCM_TYPE_GUESTS);
            }
        });
    }

    public void sendNotification(int id) {
        String title = "";
        switch (id) {
            case GCM_TYPE_GUESTS:
                title = getString(R.string.general_visitors_text);
                break;
            case GCM_TYPE_SYMPATHY:
                title = getString(R.string.general_mutual_text);
                break;
            case GCM_TYPE_LIKE:
                title = getString(R.string.general_sympathy_text);
                break;
            case GCM_TYPE_MESSAGE:
                title = getString(R.string.general_message_text);
        }
        Intent intent = new Intent("com.topface.topface.NOTIFY");
        intent.putExtra("title", title);
        intent.putExtra("text", "");
        intent.putExtra("type", Integer.toString(id));
        intent.putExtra("unread", "1");

        try {
            intent.putExtra("counters", new JSONObject().put("unread_likes",0).put("unread_sympaties",0).put("unread_messages",0).put("unread_visitors",0).toString());
            intent.putExtra("user", new JSONObject().put("id", "43945394").put("photo", new JSONObject().put("c128x128", "http://ii.cdn.tf/u42917754/r450x-/sq119c.jpg")).put("name", "Ilya").put("age", "21").toString());
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
