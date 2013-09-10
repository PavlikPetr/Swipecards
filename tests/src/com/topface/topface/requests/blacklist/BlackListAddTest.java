package com.topface.topface.requests.blacklist;

import android.text.TextUtils;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.AbstractThreadTest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

/**
 * Тест функционала черного списка (добавление, получение списка, удаление)
 */
public class BlackListAddTest extends AbstractThreadTest {

    public static final String TEST_USER_ID = "5267129";

    public void testBlackListAddRequestExec() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                new BlackListAddRequest(Integer.parseInt(TEST_USER_ID), getInstrumentation().getContext())
                        .callback(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                assertTrue("BlackListAdd response is't completed", response.isCompleted());
                                try {
                                    checkUserIsAdded();
                                } catch (Throwable throwable) {
                                    Debug.error(throwable);
                                    assertTrue(false);
                                }
                                stopTest("testBlackListAddRequestExec");
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                if (codeError == ApiResponse.PREMIUM_ACCESS_ONLY) {
                                    assertFalse("User has premium, but does not get premium", CacheProfile.premium);
                                } else {
                                    assertTrue("Request exec fail: " + codeError, false);
                                }
                                stopTest("testBlackListAddRequestExec");
                            }
                        })
                        .exec();
            }
        }, "testBlackListAddRequestExec");
    }

    public void checkUserIsAdded() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                BlackListDeleteTest.runBlackListGetRequest(new BlackListHandler() {
                    @Override
                    public void onBlackListResult(FeedListData<BlackListItem> list) {
                        boolean userIsFound = false;
                        for (BlackListItem item : list.items) {
                            assertNotNull("BlackList item user is null", item.user);
                            assertTrue("BlackList item id is wrong", !TextUtils.isEmpty(item.id));
                            if (TextUtils.equals(item.id, TEST_USER_ID)) {
                                userIsFound = true;
                            }
                        }
                        assertTrue(String.format("BlackList user %s is not found", TEST_USER_ID), userIsFound);
                        stopTest("checkUserIsAdded");
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        stopTest("checkUserIsAdded");
                    }
                }, getInstrumentation().getContext());
            }
        }, "checkUserIsAdded");
    }

}
