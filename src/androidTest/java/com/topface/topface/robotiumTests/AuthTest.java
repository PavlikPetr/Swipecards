package com.topface.topface.robotiumTests;

/**
 * Created by aki on 02/07/14.
 */

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.LinearLayout;
import com.robotium.solo.Solo;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;
import java.io.File;


public class AuthTest extends ActivityInstrumentationTestCase2<NavigationActivity> {

    private Solo solo;

    @TargetApi(Build.VERSION_CODES.FROYO)
    public AuthTest() {
        super(NavigationActivity.class);
    }

    public static class Util {
        public static boolean deleteDir(File dir) {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        }
    }

    public void setUp() throws Exception {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        defaultPreferences.edit().clear().commit();
        Util.deleteDir(new File(getInstrumentation().getTargetContext().getApplicationInfo().dataDir));
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testAuthSt() throws Exception {

        // Так можно кликнуть по ID из UIAutomatorViewer
        solo.clickOnView(solo.getView(R.id.loSignIn));

        // Вводим текст в текстфилды по индексу
        solo.enterText(0, "aki@topface.com");
        solo.enterText(1, "123");

        // Клик по кнопке Войти после ввода логина и пароля.
        // Так можно кликнуть по стрингу текста независимо от локали
        // solo.clickOnButton(solo.getCurrentActivity().getString(R.string.sign_in));

        // Клик по кнопке Войти после ввода логина и пароля.
        // Так можно кликнуть по ID из UIAutomatorViewer
        solo.clickOnView(solo.getView(R.id.btnLogin));

        // Ждем появление на экране текста
        if (solo.waitForText("Внимание!")) {
            solo.clickOnButton("Удалить сообщения");
        }

        solo.clickOnMenuItem("Диалоги");
        solo.clickLongOnText("Галина");
        solo.clickOnView(solo.getView(R.id.add_to_black_list));
        // Засыпаем в миллисекундах (по идее, но не работает)
        solo.sleep(1000);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}