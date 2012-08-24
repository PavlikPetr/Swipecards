package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.topface.topface.R;
import com.topface.topface.data.Leaders;
import com.topface.topface.utils.SmartBitmapFactory;

import java.util.ArrayList;

/**
 * Адаптер для блока лидеров
 *
 * @see com.topface.topface.ui.blocks.LeadersBlock
 */
public class LeadersAdapter extends BaseAdapter {
    public static final int LEADER_AVATAR_SIZE = 50;
    private LayoutInflater mInflater;
    private ArrayList<Leaders.LeaderUser> mLeaders;

    public LeadersAdapter(Context context, Leaders leaders) {
        super();
        mInflater = LayoutInflater.from(context);
        mLeaders  = leaders.leaders;
    }

    @Override
    public int getCount() {
        return mLeaders.size();
    }

    @Override
    public Leaders.LeaderUser getItem(int i) {
        return mLeaders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return getLeader(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView avatar;

        if (view == null) {
            view = mInflater.inflate(R.layout.leaders_item, null);
            avatar = (ImageView) view.findViewById(R.id.leaderAvatar);
            view.setTag(avatar);
        }
        else {
            avatar = (ImageView) view.getTag();
        }

        avatar.setImageResource(0);

        setLeaderAvatar(i, avatar);

        return view;
    }

    private Leaders.LeaderUser getLeader(int i) {
        return mLeaders.get(i);
    }

    private void setLeaderAvatar(int i, ImageView avatar) {
        String url = getLeader(i).photo.links.get("origin");
        SmartBitmapFactory.getInstance()
                .setBitmapByUrl(url, avatar, LEADER_AVATAR_SIZE, LEADER_AVATAR_SIZE, true, null, Thread.MIN_PRIORITY);
    }
}
