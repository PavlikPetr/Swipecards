package com.topface.topface.requests.blacklist;


import android.content.Context;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.*;
import com.topface.topface.utils.Debug;

import java.util.ArrayList;

public class BlackListDeleteTest extends AbstractThreadTest {
    private int mUserIdForDelete;

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
                            if (item.id == BlackListAddTest.TEST_USER_ID) {
                                mUserIdForDelete = item.id;
                            }
                        }
                        sendDeleteRequest(mUserIdForDelete);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        assertTrue("ApiResponse error " + response, false);
                        stopTest("testBlackListDeleteRequestExec");
                    }
                }, getInstrumentation().getContext());
            }
        }, "testBlackListDeleteRequestExec");
    }

    private void sendDeleteRequest(final int userId) throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> users = new ArrayList<Integer>();
                users.add(userId);
                new BlackListDeleteRequest(users, getInstrumentation().getContext())
                        .callback(new ApiHandler() {
                            @Override
                            public void success(ApiResponse response) {
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
                            public void fail(int codeError, ApiResponse response) {
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
                            if (item.id == mUserIdForDelete) {
                                //Если нашли, значит произошла ошибка удаления
                                assertTrue("BlackListDelete can't delete user " + mUserIdForDelete, false);

                            }
                        }
                        stopTest("testBlackListDeleteRequestExec");
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        assertTrue("ApiResponse error " + response, false);
                        stopTest("testBlackListDeleteRequestExec");
                    }
                }, getInstrumentation().getContext());
            }
        });
    }

    /**
     * Хелпер для получения черного списка
     *
     * @param handler специальный handler, проверяющий целостность списка и возвращаюший только корректный черный список
     */
    public static void runBlackListGetRequest(BlackListHandler handler, Context context) {
        new FeedRequest(FeedRequest.FeedService.BLACK_LIST, context)
                .callback(handler).exec();
    }



}
