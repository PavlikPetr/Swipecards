package com.topface.topface.requests.blacklist;


import android.content.Context;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.AbstractThreadTest;
import com.topface.topface.requests.DeleteBlackListRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;

import java.util.ArrayList;

public class BlackListDeleteTest extends AbstractThreadTest {
    private String mUserIdForDelete;

    /**
     * Хелпер для получения черного списка
     *
     * @param handler специальный handler, проверяющий целостность списка и возвращаюший только корректный черный список
     */
    public static void runBlackListGetRequest(BlackListHandler handler, Context context) {
        new FeedRequest(FeedRequest.FeedService.BLACK_LIST, context)
                .callback(handler).exec();
    }

    public void testBlackListDeleteRequestExec() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                runBlackListGetRequest(new BlackListHandler() {
                    @Override
                    public void onBlackListResult(FeedListData<BlackListItem> list) throws Throwable {
                        //Если тестового юзера нет в черном списке, удаляем первого попавшегося
                        mUserIdForDelete = list.items.getFirst().id;

                        for (BlackListItem item : list.items) {
                            //Стараемся сперва удалить тестового юзера
                            if (TextUtils.equals(item.id, BlackListAddTest.TEST_USER_ID)) {
                                mUserIdForDelete = item.id;
                            }
                        }
                        sendDeleteRequest(mUserIdForDelete);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        assertTrue("ApiResponse error " + response, false);
                        stopTest("testBlackListDeleteRequestExec");
                    }
                }, getInstrumentation().getTargetContext());
            }
        }, "testBlackListDeleteRequestExec");
    }

    private void sendDeleteRequest(final String userId) throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> users = new ArrayList<>();
                users.add(userId);
                new DeleteBlackListRequest(users, getInstrumentation().getTargetContext())
                        .callback(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                //Удалось ли удалить элемент
                                assertTrue("BlackListDelete not completed", response.isCompleted());
                                //Проверяем что элемент удален
                                try {
                                    checkDeleteResult();
                                } catch (Throwable throwable) {
                                    Debug.error(throwable);
                                    assertTrue(false);
                                    stopTest("testBlackListDeleteRequestExec");
                                }

                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                assertTrue("BlackListDelete error " + response, false);
                                stopTest("testBlackListDeleteRequestExec");
                            }
                        }).exec();
            }
        });
    }

    private void checkDeleteResult() throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                runBlackListGetRequest(new BlackListHandler() {
                    @Override
                    public void onBlackListResult(FeedListData<BlackListItem> list) {

                        for (BlackListItem item : list.items) {
                            //Ищем удаленный элемент
                            if (item.id.equals(mUserIdForDelete)) {
                                //Если нашли, значит произошла ошибка удаления
                                assertTrue("BlackListDelete can't delete user " + mUserIdForDelete, false);

                            }
                        }
                        stopTest("testBlackListDeleteRequestExec");
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        assertTrue("ApiResponse error " + response, false);
                        stopTest("testBlackListDeleteRequestExec");
                    }
                }, getInstrumentation().getTargetContext());
            }
        });
    }


}
