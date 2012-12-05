package com.topface.topface.requests;

import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;

public class DialogsTest extends FeedTest<FeedDialog> {

    public void testFeedDialogsRequestExec() {
        runFeedTest();
    }

    @Override
    protected void runAdditionalItemAsserts(FeedDialog item) {
        assertTrue("Wrong dialog item type", item.type != FeedDialog.DEFAULT);
        if (item.type == FeedDialog.MESSAGE) {
            assertNotNull("Wrong dialog message text", item.text);
            assertTrue("Dialog message text is empty", item.text.length() > 0);
        }
    }

    public void testFeedDialogDeleteRequestExec() throws Throwable {
        runAsyncTest(new Runnable() {
            @Override
            public void run() {

            }
        });
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
