package com.topface.topface.requests.blacklist;

import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.FeedTest;

/**
 * Тест получения черного списка
 */
public class BlackListGetTest extends FeedTest<BlackListItem> {

    public void testBlackListGetRequestExec() {
        runFeedTest("testBlackListGetRequestExec");
    }

    @Override
    protected void runAdditionalItemAsserts(BlackListItem item) {
        //Дополнительно ничего не проверяем
    }

    @Override
    protected FeedListData<BlackListItem> getFeedList(ApiResponse response) {
        return new FeedListData<>(response.jsonResult, BlackListItem.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedType() {
        return FeedRequest.FeedService.BLACK_LIST;
    }
}
