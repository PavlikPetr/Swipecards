package com.topface.topface.leak;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.ui.NavigationActivity;

import org.hamcrest.Matcher;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


/**
 * Открываем много чатов и профилей, смотрим не упадет ли из-за OOM
 */
public class ChatAndProfileLeakTest extends ActivityInstrumentationTestCase2<NavigationActivity> {

    @SuppressWarnings("UnusedDeclaration")
    public ChatAndProfileLeakTest(Class<NavigationActivity> activityClass) {
        super(activityClass);
    }

    private static ViewAction actionOpenDrawer() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(DrawerLayout.class);
            }

            @Override
            public String getDescription() {
                return "open drawer";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((DrawerLayout) view).openDrawer(GravityCompat.START);
            }
        };
    }


    public ChatAndProfileLeakTest() {
        super(NavigationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testOpenAndCloseDrawer() {
        // Drawer should not be open to start.
        onView(withId(R.id.loNavigationDrawer)).perform(actionOpenDrawer());
        onView(withText(R.string.settings_messages)).perform(click());
        DataInteraction dialogs = onData(allOf(is(instanceOf(FeedItem.class))))
                //Нам нужно обратиться к внутреннему View PullToRefresh
                .inAdapterView(withId(android.R.id.list));
        sleep(3500);
        for (int i = 0; i < 30; i++) {
            dialogs
                    .atPosition(i)
                    .perform(click());

            sleep(500);
            onView(withId(R.id.ivBarAvatar)).perform(click());
            pressBack();
            sleep(500);
            pressBack();
        }
        sleep(2500);

    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
