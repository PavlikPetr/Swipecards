package com.topface.topface.requests.blacklist;

import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;

abstract class BlackListHandler extends ApiHandler {

    abstract public void onBlackListResult(FeedListData<BlackListItem> list) throws Throwable;

    @Override
    public void success(ApiResponse response) {
        final FeedListData<BlackListItem> feedList =
                new FeedListData<BlackListItem>(response.jsonResult, BlackListItem.class);

//        assertNotNull("BlackList is null", feedList);
//        assertNotNull("BlackList items is null", feedList.items);
//        assertTrue("BlackList is empty", feedList.items.size() > 0);

        try {
            onBlackListResult(feedList);
        } catch (Throwable throwable) {
//            assertTrue(false);
        }

    }
}