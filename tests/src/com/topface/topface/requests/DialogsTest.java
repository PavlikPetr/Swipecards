package com.topface.topface.requests;

import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.handlers.ApiHandler;

public class DialogsTest extends FeedTest<FeedDialog> {

    private int mIdForDelete = 0;

    public void testFeedDialogsRequestExec() {
        runFeedTest("testFeedDialogsRequestExec");
    }

    @Override
    protected void runAdditionalItemAsserts(FeedDialog item) {
        assertTrue("Wrong dialog item type", item.type != FeedDialog.DEFAULT);
        if (mIdForDelete == 0) {
            //Этот id будем в дальнейшем удалять
            mIdForDelete = item.id;
        }
        if (item.type == FeedDialog.MESSAGE) {
            assertNotNull("Wrong dialog message text", item.text);
            assertTrue("Dialog message text is empty", item.text.length() > 0);
        }
    }

    public void testFeedDialogDeleteRequestExecWithWrongId() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {
                new DialogDeleteRequest(0, getContext())
                        .callback(new ApiHandler() {
                            @Override
                            public void success(ApiResponse response) {
                                assertTrue("This request must return error", false);
                                stopTest("testFeedDialogDeleteRequestExecWithWrongId");
                            }

                            @Override
                            public void fail(int codeError, ApiResponse response) {
                                assertEquals("Wrong error code", ApiResponse.MISSING_REQUIRE_PARAMETER, codeError);
                                stopTest("testFeedDialogDeleteRequestExecWithWrongId");
                            }
                        }).exec();
            }
        }, "testFeedDialogDeleteRequestExecWithWrongId");
    }

    @Override
    protected FeedListData<FeedDialog> getFeedList(ApiResponse response) {
        return new FeedListData<FeedDialog>(response.jsonResult, FeedDialog.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedType() {
        return FeedRequest.FeedService.DIALOGS;
    }
}
