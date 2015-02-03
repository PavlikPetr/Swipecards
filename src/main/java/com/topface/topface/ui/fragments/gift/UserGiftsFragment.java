package com.topface.topface.ui.fragments.gift;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;

import java.util.ArrayList;

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

    private UserProfileFragment getUserProfileFragment() {
            UserProfileFragment fragment = null;
            try {
            fragment = (UserProfileFragment) getParentFragment();
        } catch (Exception e) {
            Debug.error("Fragment not equals UserProfileFragment ", e);
        }
        return fragment;
    }

    private boolean clearNewGiftsArray() {
        UserProfileFragment fragment = getUserProfileFragment();
        if (fragment != null) {
            fragment.clearNewFeedGift();
            return true;
        }
        return false;
    }

    @Override
    protected void restoreInstanceState(Bundle savedState) {
        UserProfileFragment fragment = getUserProfileFragment();
        if (fragment != null) {
            ArrayList<FeedGift> newGifts = fragment.getNewGifts();
            if (newGifts.size() > 0) {
                ArrayList<FeedGift> gifts = savedState.getParcelableArrayList(PlainGiftsFragment.DATA);
                // find button SendGift and add new gifts after it
                gifts.addAll(getPastePosition(gifts), newGifts);
                // displace list position
                int position = savedState.getInt(PlainGiftsFragment.POSITION, 0) + newGifts.size();
                clearNewGiftsArray();
                savedState.putParcelableArrayList(DATA, gifts);
                savedState.putInt(POSITION, position);
            }
        }
        super.restoreInstanceState(savedState);
    }

    @Override
    public void onResume() {
        super.onResume();
        UserProfileFragment fragment = getUserProfileFragment();
        if (fragment != null) {
            ArrayList<FeedGift> newGifts = fragment.getNewGifts();
            if (newGifts != null && newGifts.size() > 0) {
                ArrayList<FeedGift> adapterGifts = mGridAdapter.getData();
                if (adapterGifts.size() == getMinItemsCount()) {
                    mTitle.setVisibility(View.GONE);
                }
                int pos = getPastePosition(adapterGifts);
                for (int i = 0; i < newGifts.size(); i++) {
                    mGridAdapter.add(pos + i, newGifts.get(i));
                }
                mGridAdapter.notifyDataSetChanged();
                clearNewGiftsArray();
            }
        }
    }

    private int getPastePosition(ArrayList<FeedGift> gifts) {
        for (int i = 0; i < gifts.size(); i++) {
            if (gifts.get(i).gift.type == Gift.SEND_BTN) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clearNewGiftsArray();
    }
}
