package com.topface.topface.ui.settings;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.Debug;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingsAboutFragment extends BaseFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_about, container, false);
        final FragmentActivity activity = getActivity();

        // Version
        TextView version = (TextView) root.findViewById(R.id.tvVersion);
        String versionNumber;

        try {
            PackageManager packageManager = activity.getPackageManager();
            String packageName = activity.getPackageName();
            versionNumber = packageManager.getPackageInfo(packageName, 0).versionName;

            if (App.DEBUG) {
                versionNumber += "\nBuild time: " + SimpleDateFormat.getInstance().format(
                        com.topface.topface.BuildConfig.BUILD_TIME
                );
                if (!TextUtils.isEmpty(com.topface.topface.BuildConfig.GIT_HEAD_SHA)) {
                    versionNumber += "\nCommit: " + com.topface.topface.BuildConfig.GIT_HEAD_SHA;
                }
            }
        } catch (Exception e) {
            versionNumber = "unknown";
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
        extra.setMovementMethod(LinkMovementMethod.getInstance());
        String extraText =
                getResources().getString(R.string.settings_extra) + "\n" +
                        getResources().getString(R.string.settings_topface_url);
        extra.setText(extraText);

        return root;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_about);
    }
}
