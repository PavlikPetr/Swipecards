package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.R;
import com.topface.topface.data.Visitor;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.VisitorsListAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: gildor
 * Date: 11.10.12
 * Time: 12:08
 * To change this template use File | Settings | File Templates.
 */
public class VisitorsFragment extends FeedFragment<Visitor> {
    @Override
    protected Drawable getBackIcon() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected int getTitle() {
        return R.string.dashbrd_btn_visitors;
    }

    @Override
    protected FeedAdapter<Visitor> getAdapter() {
        return new VisitorsListAdapter(getActivity(), getUpdaterCallback());
    }


    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.VISITORS;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.visitors_background_text;
    }
}
