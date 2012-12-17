package com.topface.topface.requests.blacklist;

import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.AbstractThreadTest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.utils.Debug;

/**
 * Тест функционала черного списка (добавление, получение списка, удаление)
 */
public class BlackListAddTest extends AbstractThreadTest {

    public static final int TEST_USER_ID = 5267129;

    public void testBlackListAddRequestExec() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                new BlackListAddRequest(TEST_USER_ID, getInstrumentation().getContext())
                        .callback(new ApiHandler() {
                            @Override
                            public void success(ApiResponse response) {
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
                            public void fail(int codeError, ApiResponse response) {
                                if (codeError == ApiResponse.PREMIUM_ACCESS_ONLY) {
                                    assertTrue("For add user to black list need premium", false);
                                }
                                else {
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
                            assertTrue("BlackList item id is wrong", item.id > 0);
                            if (item.id == TEST_USER_ID) {
                                userIsFound = true;
                            }
                        }
                        assertTrue(String.format("BlackList user %d is not found", TEST_USER_ID), userIsFound);
                        stopTest("checkUserIsAdded");
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        stopTest("checkUserIsAdded");
                    }
                }, getInstrumentation().getContext());
            }
        }, "checkUserIsAdded");
    }

}
