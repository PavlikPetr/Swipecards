package com.topface.topface.ui.adapters;

import com.topface.topface.R;
import com.topface.topface.data.FeedSympathy;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MutualListAdapter extends LoadingListAdapter {
    //---------------------------------------------------------------------------
    // class ViewHolder
    //---------------------------------------------------------------------------
    static class ViewHolder {
        public ImageView mAvatar;
        public TextView mName;
        public TextView mCity;
        public TextView mTime;
        public ImageView mHeart;
        public ImageView mArrow;
        public ImageView mOnline;
    }
    //---------------------------------------------------------------------------
    // Data
//    private Context mContext;
    private LayoutInflater mInflater;
    private AvatarManager<FeedSympathy> mAvatarManager;
    private int mOwnerCityID;
    // Constants    
    private static final int T_CITY = 3;    
    private static final int T_COUNT = 1;
    //---------------------------------------------------------------------------
    public MutualListAdapter(Context context,AvatarManager<FeedSympathy> avatarManager) {
//        mContext = context;
        mAvatarManager = avatarManager;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //mInflater = LayoutInflater.from(context);
        mOwnerCityID = CacheProfile.city_id;
        
        mLoaderRetrier = mInflater.inflate(R.layout.item_list_loader_retrier, null, false);   
        mLoaderRetrierText = (TextView) mLoaderRetrier.findViewById(R.id.tvLoaderText);
        mLoaderRetrierProgress = (ProgressBar) mLoaderRetrier.findViewById(R.id.prsLoader);
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
        return super.getViewTypeCount() + T_COUNT;
    }
    //---------------------------------------------------------------------------
    @Override
    public int getItemViewType(int position) {
    	int typeOfSuperMethod = super.getItemViewType(position); 
    	if (typeOfSuperMethod == T_OTHER)
    		return mAvatarManager.get(position).city_id == mOwnerCityID ? T_CITY : T_OTHER;
    	else 
    		return typeOfSuperMethod; 
    }
    //---------------------------------------------------------------------------
    @Override
    public View getView(int position,View convertView,ViewGroup parent) {
        ViewHolder holder;

        int type = getItemViewType(position);

        if (type == T_LOADER) {        	        	        	
        	mLoaderRetrierProgress.setVisibility(View.VISIBLE);
        	mLoaderRetrierText.setVisibility(View.INVISIBLE);
        	return mLoaderRetrier;
        } else if (type == T_RETRIER) {
        	mLoaderRetrierProgress.setVisibility(View.INVISIBLE);
        	mLoaderRetrierText.setVisibility(View.VISIBLE);
        	return mLoaderRetrier;
        } else {
        	if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.item_symphaty_gallery, null, false);

                holder.mAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
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

            //holder.mTime.setText(Utils.formatTime(mContext, symphaty.created));
            //holder.mArrow.setImageResource(R.drawable.im_item_arrow);

            return convertView;
        }
    }
    //---------------------------------------------------------------------------
    public void release() {
        mInflater = null;
        mAvatarManager = null;
    }
    //---------------------------------------------------------------------------
}
