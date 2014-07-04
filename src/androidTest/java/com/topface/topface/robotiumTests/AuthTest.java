package com.topface.topface.robotiumTests;


import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.TestUtils;
import java.io.File;


public class AuthTest extends ActivityInstrumentationTestCase2<NavigationActivity> {

    private Solo mSolo;

    @TargetApi(Build.VERSION_CODES.FROYO)
    public AuthTest() {
        super(NavigationActivity.class);
    }

    public void setUp() throws Exception {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        defaultPreferences.edit().clear().commit();
        TestUtils.deleteDir(new File(getInstrumentation().getTargetContext().getApplicationInfo().dataDir));
        mSolo = new Solo(getInstrumentation(), getActivity());
    }

    public void testAuthSt() throws Exception {

        // Так можно кликнуть по ID из UIAutomatorViewer
        mSolo.clickOnView(mSolo.getView(R.id.loSignIn));

        // Вводим текст в текстфилды по индексу
        mSolo.enterText(0, "aki@topface.com");
        mSolo.enterText(1, "123");

        // Клик по кнопке Войти после ввода логина и пароля.
        // Так можно кликнуть по стрингу текста независимо от локали
        // solo.clickOnButton(solo.getCurrentActivity().getString(R.string.sign_in));

        // Клик по кнопке Войти после ввода логина и пароля.
        // Так можно кликнуть по ID из UIAutomatorViewer
        mSolo.clickOnView(mSolo.getView(R.id.btnLogin));

        // Ждем появление на экране текста
        if (mSolo.waitForText("Внимание!")) {
            mSolo.clickOnButton("Удалить сообщения");
        }

        mSolo.clickOnMenuItem("Диалоги");
        mSolo.clickLongOnText("Галина");
        mSolo.clickOnView(mSolo.getView(R.id.add_to_black_list));
        // Засыпаем в миллисекундах (по идее, но не работает)
        mSolo.sleep(1000);
    }

    @Override
    protected void tearDown() throws Exception {
        mSolo.finishOpenedActivities();
    }

}