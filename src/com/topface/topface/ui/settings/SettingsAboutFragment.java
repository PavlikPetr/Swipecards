package com.topface.topface.ui.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedFragment;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.Debug;

import java.util.Calendar;

public class SettingsAboutFragment extends BaseFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_about, container, false);
        final FragmentActivity activity = getActivity();

        // Navigation bar
        ActionBar actionBar = getActionBar(root);
        actionBar.showBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        actionBar.setTitleText(getString(R.string.settings_about));

        // Version
        TextView version = (TextView) root.findViewById(R.id.tvVersion);
        String versionNumber;

        try {
            versionNumber = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionNumber = "unknown";
            Debug.error(e);
        }

        try {
            PackageInfo pInfo;
            pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
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
