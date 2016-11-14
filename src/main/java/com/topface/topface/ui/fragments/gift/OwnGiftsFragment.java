package com.topface.topface.ui.fragments.gift;

import android.os.Bundle;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;

/**
 * Fragment displaying your own gifts
 */
public class OwnGiftsFragment extends UpdatableGiftsFragment {

    private static final String PAGE_NAME = "profile.gifts";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public void onResume() {
        super.onResume();
        ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getString(R.string.profile_gifts)));
    }

    @Override
    protected void postGiftsLoadInfoUpdate(Profile.Gifts gifts) {
        if (mGridAdapter.getData().isEmpty()) {
            mGroupInfo.setVisibility(View.VISIBLE);
            mTextInfo.setText(R.string.you_dont_have_gifts_yet);
            mBtnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(PurchasesActivity.createBuyingIntent("ProfileGifts", App.from(getActivity()).getOptions().topfaceOfferwallRedirect));
                }
            });
        } else {
            super.postGiftsLoadInfoUpdate(gifts);
        }
    }
}
