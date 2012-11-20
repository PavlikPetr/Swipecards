package com.topface.topface.ui.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;

import java.util.Calendar;

public class SettingsAboutFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_about, container, false);

        // Navigation bar
        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setText(R.string.settings_header_title);
        btnBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.settings_about);

        // Version
        TextView version = (TextView) root.findViewById(R.id.tvVersion);
        String versionNumber = "2.0.0";
        try {
            PackageInfo pInfo;
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionNumber = pInfo.versionName;
        } catch (NameNotFoundException e) {
            Debug.error(e);
        }
        version.setText(getResources().getString(R.string.settings_version) + " " + versionNumber);

        // Copyright
        TextView copyright = (TextView) root.findViewById(R.id.tvCopyright);
        String copyrightText = getResources().getString(R.string.settings_copyright) +
                Calendar.getInstance().get(Calendar.YEAR) + " " +
                getResources().getString(R.string.settings_rights_reserved);
        copyright.setText(copyrightText);

        // Extra
        TextView extra = (TextView) root.findViewById(R.id.tvExtra);
        String extraText = getResources().getString(R.string.settings_extra) + " " +
                getResources().getString(R.string.settings_topface_url);
        extra.setText(extraText);

        return root;
    }
}
