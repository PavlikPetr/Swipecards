package com.topface.topface.ui.fragments.gift;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.IGiftSendListener;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.GiftsAdapter.ViewHolder;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;

import java.util.List;

public class PlainGiftsFragment<T extends List<Gift>> extends ProfileInnerFragment {
    protected TextView mTitle;
    protected View mGroupInfo;
    protected TextView mTextInfo;
    protected Button mBtnInfo;
    protected GiftsAdapter mGridAdapter;
    private GridView mGridView;
    private IGiftSendListener mGiftSendListener;
    private T mGiftsFirstPortion;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IGiftSendListener) {
            mGiftSendListener = (IGiftSendListener) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_grid, null);
        mGridView = (GridView) root.findViewById(R.id.usedGrid);
        mGridView.setAnimationCacheEnabled(false);
        mGridView.setScrollingCacheEnabled(true);
        mGridAdapter = new GiftsAdapter(getActivity().getApplicationContext(), new FeedList<FeedGift>(), getUpdaterCallback());
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnScrollListener(mGridAdapter);
        mTitle = (TextView) root.findViewById(R.id.usedTitle);
        mGroupInfo = root.findViewById(R.id.loInfo);
        mTextInfo = (TextView) mGroupInfo.findViewById(R.id.tvInfo);
        mBtnInfo = (Button) mGroupInfo.findViewById(R.id.btnInfo);
        return root;
    }

    public void addItem(FeedGift item) {
        mGridAdapter.add(item);
    }

    protected void onGiftClick(AdapterView<?> parent, View view, int position, long id) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            FeedGift item = (FeedGift) parent.getItemAtPosition(position);
            if (view.getTag() instanceof ViewHolder) {
                if (item != null
                        && item.gift.type != Gift.PROFILE
                        && item.gift.type != Gift.SEND_BTN
                        && mGiftSendListener != null) {
                    mGiftSendListener.onSendGift(item.gift);
                }
            }
        }
    }

    protected void initViews() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onGiftClick(parent, view, position, id);
            }
        });
    }

    protected int getMinItemsCount() {
        return 0;
    }

    protected void postGiftsLoadInfoUpdate(T gifts) {
        mGroupInfo.setVisibility(View.GONE);
        mTextInfo.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setGifts(mGiftsFirstPortion);
        mGiftsFirstPortion = null;
    }

    public void setGifts(final T gifts) {
        if (isAdded() && getView() != null) { // getView() to check that view is created
            FeedList<FeedGift> data = mGridAdapter.getData();
            if (data != null && gifts != null) {
                data.clear();
                for (Gift gift : gifts) {
                    FeedGift item = new FeedGift();
                    item.gift = gift;
                    data.add(item);
                }
            }
            postGiftsLoadInfoUpdate(gifts);

            mGridAdapter.notifyDataSetChanged();
            initViews();
        } else {
            mGiftsFirstPortion = gifts;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected FeedAdapter.Updater getUpdaterCallback() {
        return null;
    }

}
