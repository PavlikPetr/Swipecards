package com.topface.topface.ui.settings;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.Debug;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
            PackageManager packageManager = activity.getPackageManager();
            String packageName = activity.getPackageName();
            versionNumber = packageManager.getPackageInfo(packageName, 0).versionName;

            if (App.DEBUG) {
                ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
                ZipFile zf = new ZipFile(ai.sourceDir);
                ZipEntry ze = zf.getEntry("classes.dex");
                long time = ze.getTime();
                versionNumber += "\nBuild: " + SimpleDateFormat.getInstance().format(new java.util.Date(time));
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
}
