package com.topface.topface.requests.blacklist;

import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.Debug;

abstract class BlackListHandler extends ApiHandler {

    abstract public void onBlackListResult(FeedListData<BlackListItem> list) throws Throwable;

    @Override
    public void success(IApiResponse response) {
        final FeedListData<BlackListItem> feedList =
                new FeedListData<>(response.getJsonResult(), BlackListItem.class);

        try {
            onBlackListResult(feedList);
        } catch (Throwable throwable) {
            Debug.error(throwable);
        }

    }
}
