package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.topface.topface.R;
import com.topface.topface.data.Leader;
import com.topface.topface.ui.views.ImageViewRemote;

/**
 * Адаптер для блока лидеров
 *
 * @see com.topface.topface.ui.blocks.LeadersBlock
 */
public class LeadersAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private FeedList<Leader> mLeaders;

    public LeadersAdapter(Context context, FeedList<Leader> leaders) {
        super();
        mInflater = LayoutInflater.from(context);
        mLeaders = leaders;
    }

    @Override
    public int getCount() {
        return mLeaders.size();
    }

    @Override
    public Leader getItem(int i) {
        return mLeaders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageViewRemote avatar;
        Leader leader = getItem(i);

        if (view == null) {
            view = mInflater.inflate(R.layout.leaders_item, null);
            avatar = (ImageViewRemote) view.findViewById(R.id.leaderAvatar);
            view.setTag(avatar);
        } else {
            avatar = (ImageViewRemote) view.getTag();
        }

        avatar.setPhoto(leader.photo);

        return view;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LeadersAdapter && ((LeadersAdapter) o).mLeaders.equals(mLeaders);
    }
}
