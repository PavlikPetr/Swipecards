package com.topface.topface.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.topface.topface.R;
import com.topface.topface.data.Leaders;
import com.topface.topface.data.UserPhotos;
import com.topface.topface.ui.profile.ProfileActivity;
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
    private Context mContext;

    public LeadersAdapter(Context context, Leaders leaders) {
        super();
        mInflater = LayoutInflater.from(context);
        mLeaders  = leaders.leaders;
        mContext  = context;
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
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView avatar;
        Leaders.LeaderUser leader = getItem(i);

        if (view == null) {
            view = mInflater.inflate(R.layout.leaders_item, null);
            avatar = (ImageView) view.findViewById(R.id.leaderAvatar);
            view.setTag(avatar);
        }
        else {
            avatar = (ImageView) view.getTag();
        }

        avatar.setImageResource(R.drawable.dashbrd_chat_pressed);

        //setLeaderAvatar(leader.photo, avatar);

        return view;
    }

    private Leaders.LeaderUser getLeader(int i) {
        return mLeaders.get(i);
    }

    private void setLeaderAvatar(UserPhotos photo, ImageView avatar) {
        SmartBitmapFactory.getInstance()
                .setBitmapByUrl(
                        photo.links.get(UserPhotos.SIZE_ORIGINAL),
                        avatar,
                        LEADER_AVATAR_SIZE, LEADER_AVATAR_SIZE,
                        true, null, Thread.MIN_PRIORITY);
    }
}
