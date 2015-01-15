package com.topface.topface.ui.fragments.gift;

import android.view.View;
import android.widget.AdapterView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;

/**
 * Fragment to display user's gifts
 */
public class UserGiftsFragment extends UpdatableGiftsFragment {

    @Override
    protected void onGiftClick(AdapterView<?> parent, View view, int position, long id) {
        if (view.getTag() instanceof GiftsAdapter.ViewHolder) {
            FeedGift item = (FeedGift) parent.getItemAtPosition(position);
            if (item != null && item.gift != null) {
                if (item.gift.type == Gift.SEND_BTN) {
                    sendGift();
                }
            }
        }
        super.onGiftClick(parent, view, position, id);
    }

    @Override
    protected void initViews() {
        if (mGridAdapter.getData().size() == getMinItemsCount()) {
            mTitle.setText(R.string.user_does_not_have_gifts);
            mTitle.setVisibility(View.VISIBLE);
        }
        super.initViews();
    }

    @Override
    protected int getMinItemsCount() {
        return 1;
    }

    @Override
    protected void postGiftsLoadInfoUpdate(Profile.Gifts gifts) {
        FeedList<FeedGift> data = mGridAdapter.getData();
        if (data.size() < getMinItemsCount() || data.get(0).gift.type != Gift.SEND_BTN) {
            data.add(0, FeedGift.getSendedGiftItem());
        }
        if (data.size() == getMinItemsCount()) {
            mTitle.setText(R.string.user_does_not_have_gifts);
            mTitle.setVisibility(View.VISIBLE);
        } else {
            super.postGiftsLoadInfoUpdate(gifts);
        }
    }

    public void sendGift() {
        getParentFragment().startActivityForResult(
                GiftsActivity.getSendGiftIntent(getActivity(), getProfileId()),
                GiftsActivity.INTENT_REQUEST_GIFT
        );
    }
}
