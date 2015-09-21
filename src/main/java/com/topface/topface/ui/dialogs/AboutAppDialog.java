package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AboutAppDialog {

    private Context mContext;
    private String mAboutTitle;
    private String mAboutUrl;

    public AboutAppDialog(Context context, String title, String aboutTitle, String aboutUrl) {
        mAboutTitle = aboutTitle;
        mAboutUrl = aboutUrl;
        mContext = context;
        showAboutAppDialog(title);
    }

    private void showAboutAppDialog(String titleDialog) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_about, null);
        // Version
        TextView version = (TextView) view.findViewById(R.id.tvVersion);
        String versionNumber;

        try {
            PackageManager packageManager = mContext.getPackageManager();
            String packageName = mContext.getPackageName();
            versionNumber = BuildConfig.VERSION_NAME;

            //Дополнительную информацию показываем только в дебаг режиме
            if (BuildConfig.DEBUG) {
                versionNumber = setDebugData(versionNumber, packageManager, packageName);
            }

        } catch (Exception e) {
            versionNumber = "unknown";
            Debug.error(e);
        }

        version.setText(mContext.getResources().getString(R.string.settings_version) + " " + versionNumber);

        // Copyright
        TextView copyright = (TextView) view.findViewById(R.id.tvCopyright);
        String copyrightText = mContext.getResources().getString(R.string.settings_copyright) +
                Calendar.getInstance().get(Calendar.YEAR) + " " +
                mContext.getResources().getString(R.string.settings_rights_reserved);
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
                    mContext.startActivity(i);
                }
            }
        });
        new AlertDialog.Builder(mContext)
                .setTitle(titleDialog).setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
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
        } catch (Exception e) {
            Debug.error("BUILD_TIME access error", e);
        }
        return time;
    }
}
