package com.topface.topface.ui.adapters;

import com.topface.topface.R;
import com.topface.topface.data.FeedSympathy;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SymphatyListAdapter extends BaseAdapter {
    //---------------------------------------------------------------------------
    // class ViewHolder
    //---------------------------------------------------------------------------
    static class ViewHolder {
        public RoundedImageView mAvatar;
        public TextView mName;
        public TextView mCity;
        public TextView mTime;
        public ImageView mHeart;
        public ImageView mArrow;
        public ImageView mOnline;
    }
    //---------------------------------------------------------------------------
    // Data
    private LayoutInflater mInflater;
    private AvatarManager<FeedSympathy> mAvatarManager;
    private int mOwnerCityID;
    // Constants
    private static final int T_ALL = 0;
    private static final int T_CITY = 1;
    private static final int T_COUNT = 2;
    //---------------------------------------------------------------------------
    public SymphatyListAdapter(Context context,AvatarManager<FeedSympathy> avatarManager) {
        mAvatarManager = avatarManager;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //mInflater = LayoutInflater.from(context);
        mOwnerCityID = CacheProfile.city_id;
    }
    //---------------------------------------------------------------------------
    @Override
    public int getCount() {
        return mAvatarManager.size();
    }
    //---------------------------------------------------------------------------
    @Override
    public FeedSympathy getItem(int position) {
        return mAvatarManager.get(position);
    }
    //---------------------------------------------------------------------------
    @Override
    public long getItemId(int position) {
        return position;
    }
    //---------------------------------------------------------------------------
    @Override
    public int getViewTypeCount() {
        return T_COUNT;
    }
    //---------------------------------------------------------------------------
    @Override
    public int getItemViewType(int position) {
        return mAvatarManager.get(position).city_id == mOwnerCityID ? T_CITY : T_ALL;
    }
    //---------------------------------------------------------------------------
    @Override
    public View getView(int position,View convertView,ViewGroup parent) {
        ViewHolder holder;

        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_symphaty_gallery, null, false);

            holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.ivAvatar);
            holder.mName = (TextView)convertView.findViewById(R.id.tvName);
            holder.mCity = (TextView)convertView.findViewById(R.id.tvCity);
            holder.mTime = (TextView)convertView.findViewById(R.id.tvTime);
            holder.mHeart = (ImageView)convertView.findViewById(R.id.ivArrow);
            holder.mArrow = (ImageView)convertView.findViewById(R.id.ivArrow);
            holder.mOnline = (ImageView)convertView.findViewById(R.id.ivOnline);

            /*switch(type) {
             * case T_ALL:
             * convertView.setBackgroundResource(R.drawable.item_all_selector);
             * break;
             * case T_CITY:
             * convertView.setBackgroundResource(R.drawable.item_city_selector);
             * break;
             * } */

            convertView.setTag(holder);
        } else
            holder = (ViewHolder)convertView.getTag();

        mAvatarManager.getImage(position, holder.mAvatar);

        FeedSympathy symphaty = getItem(position);

        holder.mName.setText(symphaty.first_name);
        holder.mCity.setText(symphaty.age + ", " + symphaty.city_name);

        if (symphaty.online)
            holder.mOnline.setVisibility(View.VISIBLE);
        else
            holder.mOnline.setVisibility(View.INVISIBLE);

        Utils.formatTime(holder.mTime, symphaty.created);
        //holder.mArrow.setImageResource(R.drawable.im_item_arrow);

        return convertView;
    }
    //---------------------------------------------------------------------------
    public void release() {
        mInflater = null;
        mAvatarManager = null;
    }
    //---------------------------------------------------------------------------
}
