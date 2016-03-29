package com.topface.topface.ui.fragments.gift;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.ui.IGiftSendListener;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsListAdapter;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;

import java.util.ArrayList;

public class GiftsListFragment extends ProfileInnerFragment implements GiftsListAdapter.OnGridClickLIstener {

    private static final String DATA = "data";
    private GiftsListAdapter mGiftsListAdapter;
    private IGiftSendListener mGiftSendListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IGiftSendListener) {
            mGiftSendListener = (IGiftSendListener) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gifts_list, null);
        GridView gridView = (GridView) root.findViewById(R.id.giftsGrid);
        gridView.setAnimationCacheEnabled(false);
        gridView.setScrollingCacheEnabled(true);
        mGiftsListAdapter = new GiftsListAdapter(getActivity(), new FeedList<FeedGift>(), null);
        mGiftsListAdapter.setOnGridClickLIstener(this);
        gridView.setAdapter(mGiftsListAdapter);
        gridView.setOnScrollListener(mGiftsListAdapter);
        return root;
    }

    public void setGifts(ArrayList<Gift> gifts) {
        FeedList<FeedGift> data = mGiftsListAdapter.getData();
        for (Gift gift : gifts) {
            FeedGift item = new FeedGift();
            item.gift = gift;
            data.add(item);
        }
        mGiftsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGridClick(FeedGift item) {
        if (item == null) {
            return;
        }
        getGiftSendListener().onSendGift(item.gift);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(DATA, mGiftsListAdapter.getData());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ArrayList<Parcelable> gfts = savedInstanceState.getParcelableArrayList(DATA);
            ArrayList<FeedGift> g = new ArrayList<>(gfts.size());
            for (Parcelable p : gfts) {
                g.add((FeedGift) p);
            }
            mGiftsListAdapter.setData(g, false);
            mGiftsListAdapter.notifyDataSetChanged();
        }
    }

    public IGiftSendListener getGiftSendListener() {
        return mGiftSendListener;
    }
}
