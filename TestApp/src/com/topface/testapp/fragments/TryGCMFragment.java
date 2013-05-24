package com.topface.testapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.testapp.R;
import com.topface.testapp.receivers.TestGCMReceiver;

import java.util.Set;

public class TryGCMFragment extends Fragment{

    private EditText email;
    BroadcastReceiver receiver;
    private TextView responceText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.test_gcm_layout, container, false);
        email = (EditText) root.findViewById(R.id.email);
        Button send = (Button) root.findViewById(R.id.sendBtn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        responceText = (TextView) root.findViewById(R.id.serverResponse);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Set<String> keys = extras.keySet();
                    String responce = "";
                    for (String key : keys) {
                        Object value = extras.get(key);
                        responce = responce + key + ": " + value.toString() + "\n";
                    }
                    responceText.setText(responce);
                } else {
                    responceText.setText("Ничего не пришло");
                }
            }
        };
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, new IntentFilter("com.topface.testapp.GCMTest"));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private void sendEmail() {
        if(email != null && email.getText() != null && email.getText().toString() != "") {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{email.getText().toString()});
            i.putExtra(Intent.EXTRA_SUBJECT, "Topface GCM проверка");
            i.putExtra(Intent.EXTRA_TEXT   , responceText.getText());
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
