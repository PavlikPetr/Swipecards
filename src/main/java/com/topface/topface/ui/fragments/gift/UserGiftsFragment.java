package com.topface.topface.ui.fragments.gift;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;

/**
 * Fragment to display user's gifts
 */
public class UserGiftsFragment extends UpdatableGiftsFragment {

    private UserProfileFragment.OnGiftReceivedListener mGiftReceivedListener;

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

    public void sendGift(UserProfileFragment.OnGiftReceivedListener listener) {
        this.mGiftReceivedListener = listener;
        sendGift();
    }

    public void sendGift() {
        getParentFragment().startActivityForResult(
                GiftsActivity.getSendGiftIntent(getActivity(), getProfileId(), true),
                GiftsActivity.INTENT_REQUEST_GIFT
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
                    String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);
                    FeedGift sended = new FeedGift();
                    sended.gift = new Gift(id, Gift.PROFILE_NEW, url, 0);
                    addGift(sended);
                    if (mGiftReceivedListener != null) {
                        mGiftReceivedListener.onReceived();
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void addGift(FeedGift sendedGift) {
        if (mGridAdapter.getData().size() == getMinItemsCount()) {
            mTitle.setVisibility(View.GONE);
        }
        mGridAdapter.add(getMinItemsCount(), sendedGift);
        mGridAdapter.notifyDataSetChanged();
        if (getActivity() != null) {
            Toast.makeText(getActivity(), R.string.chat_gift_out, Toast.LENGTH_LONG).show();
        }
    }
}
