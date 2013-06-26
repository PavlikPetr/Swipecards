package com.topface.topface.robotiumTests;

import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;

public class ProfileTests extends ActivityInstrumentationTestCase2<NavigationActivity> {

    private Solo solo;

    public ProfileTests() {
        super(NavigationActivity.class);
    }

    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testProfile() throws Exception{
//        solo.clickOnImageButton(R.id.btnDatingSwitchNext);
//        solo.clickOnImageButton(R.id.btnDatingProfile);
        assertTrue(true);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
