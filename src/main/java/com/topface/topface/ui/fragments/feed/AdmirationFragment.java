package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class AdmirationFragment extends LikesFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return super.onCreateView(inflater, container, saved);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_admirations);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEmptyFeedView != null) {
            initEmptyFeedView(mEmptyFeedView);
        }
    }

    @Override
    protected Drawable getBackIcon() {
        return getActivity().getResources().getDrawable(R.drawable.background_admiration);
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        if (mEmptyFeedView == null) mEmptyFeedView = inflated;
        if (CacheProfile.premium) {
            ((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews)).setDisplayedChild(0);
            inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ContainerActivity.getBuyingIntent("EmptyAdmirations"));
                }
            });
        } else {
            if (CacheProfile.unread_admirations > 0) {
                ((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews)).setDisplayedChild(1);
                int curCounter = CacheProfile.unread_admirations;
                if (curCounter == 0) {
                    curCounter = CacheProfile.getOptions().premiumAdmirations.getCount();
                }

                String title = Utils.getQuantityString(R.plurals.popup_vip_admirations, curCounter, curCounter);
                ((TextView) inflated.findViewById(R.id.tvTitle)).setText(title);
                inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = ContainerActivity.getVipBuyIntent(null, "Admirations");
                        startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                    }
                });
                ((ImageViewRemote) inflated.findViewById(R.id.ivOne))
                        .setResourceSrc(CacheProfile.dating.sex == Static.GIRL ? R.drawable.likes_male_one : R.drawable.likes_female_one);
                ((ImageViewRemote) inflated.findViewById(R.id.ivTwo))
                        .setResourceSrc(CacheProfile.dating.sex == Static.GIRL ? R.drawable.likes_male_two : R.drawable.likes_female_two);
                ((ImageViewRemote) inflated.findViewById(R.id.ivThree))
                        .setResourceSrc(CacheProfile.dating.sex == Static.GIRL ? R.drawable.likes_male_three : R.drawable.likes_female_three);
            } else {
                ((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews)).setDisplayedChild(0);
                inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(ContainerActivity.getBuyingIntent("EmptyAdmirations"));
                    }
                });
            }
        }
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.ADMIRATIONS;
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_admirations;
    }
}
