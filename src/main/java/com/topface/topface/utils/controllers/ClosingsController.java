package com.topface.topface.utils.controllers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.adapters.LeftMenuAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.CacheProfile;

import com.topface.topface.ui.fragments.BaseFragment.FragmentId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirussell on 12.11.13.
 */
public class ClosingsController implements View.OnClickListener {

    private Context mContext;

    private LeftMenuAdapter mAdapter;
    private View likesMenuItem;
    private View mutualsMenuItem;
    private ViewStub mViewStub;
    private List<TextView> mCounterBadges = new ArrayList<TextView>();

    private boolean isInflated = false;

    public ClosingsController(final Context context, ViewStub mHeaderViewStub, LeftMenuAdapter adapter) {
        mContext = context;
        mViewStub = mHeaderViewStub;
        mViewStub.setLayoutResource(R.layout.layout_left_menu_closings_widget);
        mAdapter = adapter;
    }

    public void inflate() {
        // can inflate ViewStub only once
        if (isInflated) return;
        isInflated = true;
        Options.Closing closings = CacheProfile.getOptions().closing;
        View closingsWidget = mViewStub.inflate();
        closingsWidget.findViewById(R.id.btnBuyVipFromClosingsWidget).setOnClickListener(this);

        likesMenuItem = closingsWidget.findViewById(R.id.itemLikesClosings);
        if (initMenuItem(likesMenuItem, R.string.general_likes, R.drawable.ic_likes_selector,
                closings.isLikesAvailable(), FragmentId.F_LIKES_CLOSINGS)) {
            mAdapter.hideItem(FragmentId.F_LIKES);
        }
        mutualsMenuItem = closingsWidget.findViewById(R.id.itemMutualsClosings);
        if (initMenuItem(mutualsMenuItem, R.string.general_mutual, R.drawable.ic_mutual_selector,
                closings.isMutualAvailable(), FragmentId.F_MUTUAL_CLOSINGS)) {
            mAdapter.hideItem(FragmentId.F_MUTUAL);
        }
        mAdapter.setEnabled(false);
        mAdapter.notifyDataSetChanged();
    }

    private boolean initMenuItem(View menuItem, int btnTextResId, int iconResId, boolean visible,
                                 FragmentId fragmentId) {
        menuItem.setVisibility(visible ? View.VISIBLE : View.GONE);
        Button menuButton = (Button) menuItem.findViewById(R.id.btnMenu);
        menuButton.setOnClickListener(this);
        menuButton.setText(btnTextResId);
        menuButton.setTag(fragmentId);
        menuButton.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
        TextView counterBadge = (TextView) menuItem.findViewById(R.id.tvCounterBadge);
        counterBadge.setTag(fragmentId);
        mCounterBadges.add(counterBadge);
        updateCounterBadge(counterBadge);
        return visible;
    }

    public void refreshCounterBadges() {
        for (TextView badge : mCounterBadges) {
            if (badge != null) {
                updateCounterBadge(badge);
            }
        }
    }

    private void updateCounterBadge(TextView badge) {
        int unread = CacheProfile.getUnreadCounterByFragmentId((FragmentId) badge.getTag());
        if (unread > 0) {
            badge.setText(Integer.toString(unread));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof FragmentId) {
            switch ((FragmentId) tag) {
                case F_LIKES_CLOSINGS:
                    MenuFragment.selectFragment(BaseFragment.FragmentId.F_LIKES_CLOSINGS);
                    break;
                case F_MUTUAL_CLOSINGS:
                    MenuFragment.selectFragment(BaseFragment.FragmentId.F_MUTUAL_CLOSINGS);
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.btnBuyVipFromClosingsWidget:
                    mContext.startActivity(ContainerActivity.getVipBuyIntent("Menu"));
                    break;
                default:
                    break;
            }
        }
    }


}
