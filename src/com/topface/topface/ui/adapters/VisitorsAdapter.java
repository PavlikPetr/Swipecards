package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Visitor;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class VisitorsAdapter extends BaseAdapter {

    static class ViewHolder {
        public RoundedImageView mAvatar;
        public TextView  mName;
        public TextView  mText;
        public TextView  mTime;
        public ImageView mArrow;
    }

    private LayoutInflater mInflater;
    private AvatarManager<Visitor> mAvatarManager;
    private int mOwnerCityID;
    // Constants
    private static final int T_ALL   = 0;
    private static final int T_CITY  = 1;
    private static final int T_COUNT = 2;

    public VisitorsAdapter(Context context, AvatarManager<Visitor> avatarManager) {
        mAvatarManager = avatarManager;
        mInflater = LayoutInflater.from(context);
        mOwnerCityID = CacheProfile.city_id;
    }

    @Override
    public int getCount() {
        return mAvatarManager.size();
    }

    @Override
    public Visitor getItem(int position) {
        return mAvatarManager.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return T_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).city.id == mOwnerCityID ? T_CITY : T_ALL;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        int type = getItemViewType(position);

        if(convertView==null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_visitor, null, false);

            holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.ivAvatar);
            holder.mName   = (TextView)convertView.findViewById(R.id.tvName);
            holder.mText   = (TextView)convertView.findViewById(R.id.tvText);
            holder.mTime   = (TextView)convertView.findViewById(R.id.tvTime);
            holder.mArrow  = (ImageView)convertView.findViewById(R.id.ivArrow);

            switch(type) {
                case T_ALL:
                    convertView.setBackgroundResource(R.drawable.item_all_selector);
                    break;
                case T_CITY:
                    convertView.setBackgroundResource(R.drawable.item_city_selector);
                    break;
            }

            convertView.setTag(holder);
        } else
            holder = (ViewHolder)convertView.getTag();

        Visitor visitor = getItem(position);

        mAvatarManager.getImage(position, holder.mAvatar);
        holder.mName.setText(visitor.name + ", " + visitor.age + ", " + visitor.city.name);

        Utils.formatTime(holder.mTime, visitor.time);

        return convertView;
    }

    public void release() {
        mInflater=null;
        mAvatarManager=null;
    }
}
