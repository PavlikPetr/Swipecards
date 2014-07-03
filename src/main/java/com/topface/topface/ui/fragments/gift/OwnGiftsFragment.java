package com.topface.topface.ui.fragments.gift;

import android.view.View;

import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.PurchasesActivity;

/**
 * Fragment displaying your own gifts
 */
public class OwnGiftsFragment extends UpdatableGiftsFragment {

    @Override
    protected void postGiftsLoadInfoUpdate(Profile.Gifts gifts) {
        if (mGridAdapter.getData().isEmpty()) {
            mGroupInfo.setVisibility(View.VISIBLE);
            mTextInfo.setText(R.string.you_dont_have_gifts_yet);
            mBtnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(PurchasesActivity.createBuyingIntent("ProfileGifts"));
                }
            });
        } else {
            super.postGiftsLoadInfoUpdate(gifts);
        }
    }
}
