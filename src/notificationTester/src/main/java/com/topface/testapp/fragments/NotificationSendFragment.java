package com.topface.testapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.topface.testapp.R;
import org.json.JSONObject;


public class NotificationSendFragment extends Fragment {

    public static final int GCM_TYPE_MESSAGE = 0;
    public static final int GCM_TYPE_SYMPATHY = 1;
    public static final int GCM_TYPE_LIKE = 2;
    public static final int GCM_TYPE_GUESTS = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.notification_test, container, false);
        initViews(root);
        return root;
    }

    public void initViews(View root) {
        Button likes = (Button) root.findViewById(R.id.simpathy);
        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(GCM_TYPE_LIKE);
            }
        });

        Button messages = (Button) root.findViewById(R.id.message);
        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(GCM_TYPE_MESSAGE);
            }
        });

        Button sympathy = (Button) root.findViewById(R.id.mutual);
        sympathy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(GCM_TYPE_SYMPATHY);
            }
        });

        Button visitors = (Button) root.findViewById(R.id.visitors);
        visitors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification(GCM_TYPE_GUESTS);
            }
        });
    }

    public void sendNotification(int id) {
        String title = "Topface";
        String text = "";
        switch (id) {
            case GCM_TYPE_GUESTS:
                text = getString(R.string.general_visitors_text);
                break;
            case GCM_TYPE_SYMPATHY:
                text = getString(R.string.general_mutual_text);
                break;
            case GCM_TYPE_LIKE:
                text = "У вас 1 симпатия";
                break;
            case GCM_TYPE_MESSAGE:
                text = getString(R.string.general_message_text);
        }
        Intent intent = new Intent("com.topface.topface.NOTIFY");
        intent.putExtra("title", title);
        intent.putExtra("text", text);
        intent.putExtra("type", Integer.toString(id));
        intent.putExtra("unread", "1");

        try {
            intent.putExtra("unread", new JSONObject().put("likes",0).put("sympaties",0).put("dialogs",0).put("visitors",0).toString());
            intent.putExtra("user", new JSONObject().put("id", "43945394").put("photo", new JSONObject().put("c128x128", "http://ii.cdn.tf/u42917754/r450x-/5:1n08oy8.jpg")).put("name", "Ilya").put("age", "21").toString());
            getActivity().sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
