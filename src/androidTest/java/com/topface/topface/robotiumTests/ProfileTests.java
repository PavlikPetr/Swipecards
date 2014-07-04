package com.topface.topface.robotiumTests;

import android.annotation.TargetApi;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;
import com.topface.topface.ui.NavigationActivity;

@TargetApi(Build.VERSION_CODES.FROYO)
public class ProfileTests extends ActivityInstrumentationTestCase2<NavigationActivity> {

    private Solo solo;

    @TargetApi(Build.VERSION_CODES.FROYO)
    public ProfileTests() {
        super(NavigationActivity.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testProfile() throws Exception {
        assertTrue(true);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
