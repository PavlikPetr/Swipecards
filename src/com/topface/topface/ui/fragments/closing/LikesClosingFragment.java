package com.topface.topface.ui.fragments.closing;

import android.view.View;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedUser;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;

public class LikesClosingFragment extends ClosingFragment implements View.OnClickListener {

    public static boolean usersProcessed;

    private View mBtnSkipAll;

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
    public Class getItemsClass() {
        return FeedUser.class;
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
            case R.id.btnSkip:
                //TODO skipLike
                if (getCurrentUser() != null && getCurrentUser().feedItem != null) {
                    SkipClosedRequest request = new SkipClosedRequest(getActivity());
                    request.callback(new SimpleApiHandler(){
                        @Override
                        public void always(ApiResponse response) {
                            refreshActionBarTitles(getView());
                        }
                    });
                    registerRequest(request);
                    request.item = getCurrentUser().feedItem.id;
                    request.exec();
                }
                showNextUser();
                break;
            case R.id.btnSkipAll:
                skipAllRequests(SkipAllClosedRequest.LIKES);
                break;
            case R.id.btnMutual:
                getRateController().onRate(getCurrentUser().id, 10, RateRequest.DEFAULT_MUTUAL, new RateController.OnRateListener() {
                    @Override
                    public void onRateCompleted() {
                        refreshActionBarTitles(getView());
                    }

                    @Override
                    public void onRateFailed() {
                    }
                });
                showNextUser();
                break;
            case R.id.btnChat:
                showChat();
                break;
            case R.id.btnWatchAsList:
                showWatchAsListDialog(CacheProfile.unread_likes);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onUsersProcessed() {
        usersProcessed = true;
        super.onUsersProcessed();
    }

    @Override
    protected FeedRequest.FeedService getFeedType() {
        return FeedRequest.FeedService.LIKES;
    }


}
