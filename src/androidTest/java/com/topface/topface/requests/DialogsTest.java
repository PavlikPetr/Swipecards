package com.topface.topface.requests;

import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;

public class DialogsTest extends FeedTest<FeedDialog> {

    private String mIdForDelete;

    public void testFeedDialogsRequestExec() {
        runFeedTest("testFeedDialogsRequestExec");
    }

    @Override
    protected void runAdditionalItemAsserts(FeedDialog item) {
        assertTrue("Wrong dialog item type", item.type != FeedDialog.DEFAULT);
        if (mIdForDelete != null) {
            //Этот id будем в дальнейшем удалять
            mIdForDelete = item.id;
        }
        if (item.type == FeedDialog.MESSAGE) {
            assertNotNull("Wrong dialog message text", item.text);
            assertTrue("Dialog message text is empty", item.text.length() > 0);
        }
    }

    @Override
    protected FeedListData<FeedDialog> getFeedList(ApiResponse response) {
        return new FeedListData<>(response.jsonResult, FeedDialog.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedType() {
        return FeedRequest.FeedService.DIALOGS;
    }
}
