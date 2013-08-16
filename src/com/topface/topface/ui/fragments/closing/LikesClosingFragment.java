package com.topface.topface.ui.fragments.closing;

import android.view.View;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.requests.SkipAllClosedRequest;
import com.topface.topface.utils.*;

public class LikesClosingFragment extends ClosingFragment implements View.OnClickListener {

    public static boolean usersProcessed;

    private View mBtnSkipAll;

    @Override
    protected void initActionBarControls(ActionBar actionbar) {
    }

    @Override
    protected String getTitle() {
        return getString(R.string.sympathies);
    }

    @Override
    protected String getSubtitle() {
        return Utils.getQuantityString(
                R.plurals.number_of_sympathies,
                CacheProfile.unread_likes,
                CacheProfile.unread_likes
        );
    }

    @Override
    public Integer getTopPanelLayoutResId() {
        return R.layout.controls_closing_top_panel;
    }

    @Override
    protected Integer getControlsLayoutResId() {
        return R.layout.controls_closed_likes;
    }

    @Override
    protected void initTopPanel(View topPanelView) {
        topPanelView.findViewById(R.id.btnWatchAsList).setOnClickListener(this);
    }

    @Override
    protected void initControls(View controlsView) {
        controlsView.findViewById(R.id.btnSkip).setOnClickListener(this);
        controlsView.findViewById(R.id.btnSkipAll).setOnClickListener(this);
        mBtnSkipAll = controlsView.findViewById(R.id.btnSkipAll);
        mBtnSkipAll.setOnClickListener(this);
        if (CacheProfile.unread_likes > CacheProfile.getOptions().closing.limitSympathies) {
            mBtnSkipAll.setVisibility(View.VISIBLE);
        }
        controlsView.findViewById(R.id.btnMutual).setOnClickListener(this);
        controlsView.findViewById(R.id.btnChat).setOnClickListener(this);
    }

    @Override
    protected void lockControls() {
        //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void unlockControls() {
        //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onShowUser() {
        //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class getFeedUserContainerClass() {
        return FeedLike.class;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMutual:
                RateController rateController = getRateController();
                if (rateController != null) {
                    rateController.onRate(getCurrentUser().id, 9, RateRequest.DEFAULT_MUTUAL, new RateController.OnRateListener() {
                        @Override
                        public void onRateCompleted() {
                            if (isAdded()) refreshActionBarTitles(getView());
                        }

                        @Override
                        public void onRateFailed() {
                        }
                    });
                }
                showNextUser();
                break;
            default:
                super.onClick(v);
        }
    }

    @Override
    protected void onUsersProcessed() {
        usersProcessed = true;
        CountersManager.getInstance(getActivity()).setCounter(CountersManager.LIKES,0,true);
        CacheProfile.getOptions().closing.onStopLikesClosings();
        super.onUsersProcessed();
    }

    @Override
    protected int getSkipAllRequestType() {
        return SkipAllClosedRequest.LIKES;
    }

    @Override
    protected FeedRequest.FeedService getFeedType() {
        return FeedRequest.FeedService.LIKES;
    }
}
