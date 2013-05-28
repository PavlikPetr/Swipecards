package com.topface.testapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.topface.testapp.MainActivity;
import com.topface.testapp.R;

public class StartFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main, container, false);

        Button gcmFrBtn = (Button) root.findViewById(R.id.testGCM);
        gcmFrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).startFragment(new TryGCMFragment());
            }
        });

        Button notificationSendBtn = (Button) root.findViewById(R.id.testNotifications);
        notificationSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).startFragment(new NotificationSendFragment());
            }
        });

        return root;
    }




}
