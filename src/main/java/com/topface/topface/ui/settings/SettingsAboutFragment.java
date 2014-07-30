package com.topface.topface.ui.settings;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
            versionNumber = BuildConfig.VERSION_NAME;

            //Дополнительную информацию показываем только в дебаг режиме
            if (BuildConfig.DEBUG) {
                versionNumber = setDebugData(versionNumber, packageManager, packageName);
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

    @Override
    protected String getTitle() {
        return getString(R.string.settings_about);
    }
}
