package com.topface.topface.ui.adapters;

import org.w3c.dom.Text;

import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LikesListAdapter extends BaseAdapter {
    //---------------------------------------------------------------------------
    // class ViewHolder
    //---------------------------------------------------------------------------
    static class ViewHolder {
        public ImageView mAvatar;
        public ImageView mAvatarMask;
        public TextView mName;
        public TextView mCity;
        public TextView mTime;
        public ImageView mHeart;
        public ImageView mArrow;
        public ImageView mOnline;
//        public ProgressBar mProgressBar;
//        public TextView mRetryText;
    }
    //---------------------------------------------------------------------------
    // Data
    private LayoutInflater mInflater;
    private AvatarManager<FeedLike> mAvatarManager;
    private int mOwnerCityID;
    private View mLoaderRetrier;
    private TextView mLoaderRetrierText;
    private ProgressBar mLoaderRetrierProgress;
    
    // Constants
    private static final int T_ALL = 0;
    private static final int T_CITY = 1;
    private static final int T_LOADER = 2;
    private static final int T_RETRIER = 3;
    private static final int T_COUNT = 4;
    
    //---------------------------------------------------------------------------
    public LikesListAdapter(Context context,AvatarManager<FeedLike> avatarManager) {
        mAvatarManager = avatarManager;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public FeedLike getItem(int position) {
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
    	if (mAvatarManager.get(position).isListLoader) 
    		return T_LOADER;
    	if (mAvatarManager.get(position).isListLoaderRetry) 
    		return T_RETRIER;
        
    	return mAvatarManager.get(position).city_id == mOwnerCityID ? T_CITY : T_ALL;
    }
    //---------------------------------------------------------------------------
    @Override
    public View getView(int position,View convertView,ViewGroup parent) {
//    	long startTime = System.currentTimeMillis();
        ViewHolder holder;

        int type = getItemViewType(position);        
        
        if (type == T_LOADER) {        	        	        	
//        	holder.mAvatar.setVisibility(View.GONE);
//        	holder.mAvatarMask.setVisibility(View.GONE);
//            holder.mName.setVisibility(View.GONE);
//            holder.mCity.setVisibility(View.GONE);
//            holder.mTime.setVisibility(View.GONE);
//            holder.mHeart.setVisibility(View.GONE);
//            holder.mArrow.setVisibility(View.GONE);
//            holder.mOnline.setVisibility(View.GONE);
//            holder.mRetryText.setVisibility(View.GONE);
//        	holder.mProgressBar.setVisibility(View.VISIBLE);
        	mLoaderRetrierProgress.setVisibility(View.VISIBLE);
        	mLoaderRetrierText.setVisibility(View.INVISIBLE);
        	return mLoaderRetrier;
        } else if (type == T_RETRIER) {
//        	holder.mAvatar.setVisibility(View.GONE);
//        	holder.mAvatarMask.setVisibility(View.GONE);
//            holder.mName.setVisibility(View.GONE);
//            holder.mCity.setVisibility(View.GONE);
//            holder.mTime.setVisibility(View.GONE);
//            holder.mHeart.setVisibility(View.GONE);
//            holder.mArrow.setVisibility(View.GONE);
//            holder.mOnline.setVisibility(View.GONE);            
//            holder.mProgressBar.setVisibility(View.GONE);
//            holder.mRetryText.setVisibility(View.VISIBLE);
        	mLoaderRetrierProgress.setVisibility(View.INVISIBLE);
        	mLoaderRetrierText.setVisibility(View.VISIBLE);
        	return mLoaderRetrier;
        } else {        	
        	if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.item_likes_gallery, null, false);

                holder.mAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
                holder.mAvatarMask = (ImageView)convertView.findViewById(R.id.ivAvatarMask);
                holder.mName = (TextView)convertView.findViewById(R.id.tvName);
                holder.mCity = (TextView)convertView.findViewById(R.id.tvCity);
                holder.mTime = (TextView)convertView.findViewById(R.id.tvTime);
                holder.mHeart = (ImageView)convertView.findViewById(R.id.ivHeart);
                holder.mArrow = (ImageView)convertView.findViewById(R.id.ivArrow);
                holder.mOnline = (ImageView)convertView.findViewById(R.id.ivOnline);
//                holder.mRetryText = (TextView)convertView.findViewById(R.id.tvLoaderText);
//                holder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.prsLoader);

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
//        	holder.mAvatar.setVisibility(View.VISIBLE);
//        	holder.mAvatarMask.setVisibility(View.VISIBLE);
//            holder.mName.setVisibility(View.VISIBLE);
//            holder.mCity.setVisibility(View.VISIBLE);
//            holder.mTime.setVisibility(View.VISIBLE);
//            holder.mHeart.setVisibility(View.VISIBLE);
//            holder.mArrow.setVisibility(View.VISIBLE);  
//            holder.mRetryText.setVisibility(View.GONE);
//        	holder.mProgressBar.setVisibility(View.GONE);
        	
	        mAvatarManager.getImage(position, holder.mAvatar);
	
	        FeedLike likes = getItem(position);
	
	        holder.mName.setText(likes.first_name);
	        holder.mCity.setText(likes.age + ", " + likes.city_name);
	
	        if (likes.rate == 10)
	            holder.mHeart.setImageResource(R.drawable.im_item_mutual_heart_top);
	        else
	            holder.mHeart.setImageResource(R.drawable.im_item_mutual_heart);
	
	        if (likes.online)
	            holder.mOnline.setVisibility(View.VISIBLE);
	        else
	            holder.mOnline.setVisibility(View.INVISIBLE);
        }
        //Utils.formatTime(holder.mTime,likes.created);
        //holder.mArrow.setImageResource(R.drawable.im_item_arrow); // ??? зачем
        
//        if ((System.currentTimeMillis() - startTime) > 20) {
//        	Log.e("OLOLO", Long.toString((System.currentTimeMillis() - startTime)) + "   " + type);
//        } else {
//        	Log.d("OLOLO", Long.toString((System.currentTimeMillis() - startTime))+ "   " + type);
//        }
        
        return convertView;
    }
    //---------------------------------------------------------------------------
    @Override
    public boolean isEnabled(int position) {
    	if (getItemViewType(position) == T_LOADER)
    		return false;
    	return true;
    }
    //---------------------------------------------------------------------------
    public void release() {
        mInflater = null;
        mAvatarManager = null;
    }
    //---------------------------------------------------------------------------
}
