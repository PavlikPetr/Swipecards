package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedDialogFragment;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AboutAppDialog extends TrackedDialogFragment {
    private static final String DIALOG_TITLE = "dialog_title";
    private static final String ABOUT_TITLE = "about_title";
    private static final String ABOUT_URL = "about_url";

    private String mTitle;
    private String mAboutTitle;
    private String mAboutUrl;

    public static AboutAppDialog newInstance(String title, String aboutTitle, String aboutUrl) {
        AboutAppDialog dialog = new AboutAppDialog();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putString(ABOUT_TITLE, aboutTitle);
        args.putString(ABOUT_URL, aboutUrl);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DIALOG_TITLE, mTitle);
        outState.putString(ABOUT_TITLE, mAboutTitle);
        outState.putString(ABOUT_URL, mAboutUrl);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments() != null ? getArguments() : null;
        if (bundle != null) {
            mTitle = bundle.getString(DIALOG_TITLE);
            mAboutTitle = bundle.getString(ABOUT_TITLE, "");
            mAboutUrl = bundle.getString(ABOUT_URL, "");
        }
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_about, null);
        // Version
        TextView version = (TextView) view.findViewById(R.id.tvVersion);
        String versionNumber;

        try {
            PackageManager packageManager = getActivity().getPackageManager();
            String packageName = getActivity().getPackageName();
            versionNumber = BuildConfig.VERSION_NAME;

            //Дополнительную информацию показываем только в дебаг режиме
            if (BuildConfig.DEBUG) {
                versionNumber = setDebugData(versionNumber, packageManager, packageName);
            }

        } catch (Exception e) {
            versionNumber = "unknown";
            Debug.error(e);
        }

        version.setText(getString(R.string.settings_version).concat(" ").concat(versionNumber));

        // Copyright
        TextView copyright = (TextView) view.findViewById(R.id.tvCopyright);
        String copyrightText = getString(R.string.settings_copyright) +
                Calendar.getInstance().get(Calendar.YEAR) + " " +
                getString(R.string.settings_rights_reserved);
        copyright.setText(copyrightText);

        // Extra
        TextView extra = (TextView) view.findViewById(R.id.tvExtra);
        SpannableString title = new SpannableString(mAboutTitle);
        title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
        extra.setText(title);
        extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = Utils.getIntentToOpenUrl(mAboutUrl);
                if (i != null) {
                    getActivity().startActivity(i);
                }
            }
        });
        return new AlertDialog.Builder(getContext())
                .setTitle(mTitle).setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create();
    }

    /**
     * Добавляем время сборки версии и SHA коммита
     */
    private String setDebugData(String versionNumber, PackageManager packageManager, String packageName) {
        //Устанавливаем время сборки
        long buildTime = BuildConfig.BUILD_TIME;
        boolean fromConstant = true;
        //Если константа времени сборки не установлена, то пробуем получить из даты сборки пакета
        if (buildTime == 0) {
            buildTime = getBuildTimeFromPackage(packageManager, packageName);
            fromConstant = false;
        }

        versionNumber += "\nBuild time: ";

        if (buildTime > 0) {
            //Пишем время сборки. Если используем время сборки пакет1а, то добавляем тильду
            versionNumber += (fromConstant ? "" : "~~")
                    + SimpleDateFormat.getInstance().format(buildTime);
        } else {
            versionNumber += "Unknown";
        }

        //Устанавливаем номер сборки
        if (!TextUtils.isEmpty(BuildConfig.GIT_HEAD_SHA)) {
            versionNumber += "\nCommit: " + BuildConfig.GIT_HEAD_SHA;
        }

        return versionNumber;
    }

    /**
     * Получаем время сборки из косвенных данных - времени создания dex файла приложения
     *
     * @return timestamp времени сборки
     */
    private long getBuildTimeFromPackage(PackageManager packageManager, String packageName) {
        long time = 0;
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            time = ze.getTime();
            zf.close();
        } catch (Exception e) {
            Debug.error("BUILD_TIME access error", e);
        }
        return time;
    }
}
