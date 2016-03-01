package com.topface.topface.ui.fragments.gift;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.UserGiftsRecyclerAdapter;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;

import java.util.ArrayList;

public class PlainGiftsFragment extends ProfileInnerFragment {

    public static final String DATA = "data";

    protected TextView mTitle;
    protected View mGroupInfo;
    protected TextView mTextInfo;
    protected Button mBtnInfo;
    protected UserGiftsRecyclerAdapter mGridAdapter;
    private Profile.Gifts mGiftsFirstPortion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGridAdapter = new UserGiftsRecyclerAdapter(getActivity(), getUpdaterCallback()) {
            @Override
            protected void handleOldViewHolder(GiftsAdapter.ViewHolder oldHolder, FeedGift feedGift) {
                if (feedGift.gift == null) {
                    return;
                }
                if (feedGift.gift.type == Gift.SEND_BTN) {
                    oldHolder.giftImage.setImageBitmap(null);
                    oldHolder.giftImage.setBackgroundResource(R.drawable.chat_gift_selector);
                } else {
                    oldHolder.giftImage.setRemoteSrc(feedGift.gift.link);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_grid, null);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.usedGiftsGrid);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), getResources().getInteger(R.integer.add_to_leader_column_count)));
        recyclerView.setAdapter(mGridAdapter);
        mTitle = (TextView) root.findViewById(R.id.usedTitle);
        mGroupInfo = root.findViewById(R.id.loInfo);
        mTextInfo = (TextView) mGroupInfo.findViewById(R.id.tvInfo);
        mBtnInfo = (Button) mGroupInfo.findViewById(R.id.btnInfo);
        return root;
    }

    public void addItem(FeedGift item) {
        mGridAdapter.add(item);
    }

    protected int getMinItemsCount() {
        return 0;
    }

    protected void postGiftsLoadInfoUpdate(Profile.Gifts gifts) {
        mGroupInfo.setVisibility(View.GONE);
        mTextInfo.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else if (mGiftsFirstPortion != null) {
            setGifts(mGiftsFirstPortion);
            mGiftsFirstPortion = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DATA, mGridAdapter.getData());
    }

    protected void restoreInstanceState(Bundle savedState) {
        ArrayList<Parcelable> gfts = savedState.getParcelableArrayList(DATA);
        ArrayList<FeedGift> g = new ArrayList<>(gfts.size());
        for (Parcelable p : gfts) {
            g.add((FeedGift) p);
        }
        mGridAdapter.setData(g, false);
        postGiftsLoadInfoUpdate(null);
        mGridAdapter.notifyDataSetChanged();
    }

    public void setGifts(final Profile.Gifts gifts) {
        if (isAdded() && getView() != null) { // getView() to check that view is created
            FeedList<FeedGift> data = mGridAdapter.getData();
            if (data != null && gifts != null) {
                data.clear();
                for (Gift gift : gifts.getGifts()) {
                    FeedGift item = new FeedGift();
                    item.gift = gift;
                    data.add(item);
                }
            }
            postGiftsLoadInfoUpdate(gifts);
            mGridAdapter.notifyDataSetChanged();
        } else {
            mGiftsFirstPortion = gifts;
        }
    }

    protected FeedAdapter.Updater getUpdaterCallback() {
        return null;
    }
}
