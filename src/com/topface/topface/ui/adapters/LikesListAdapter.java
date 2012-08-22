package com.topface.topface.ui.adapters;


import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LikesListAdapter extends LoadingListAdapter {
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
    Context mContext;
    
    // Constants    
    private static final int T_CITY = 3;
    private static final int T_COUNT = 1;
    
    //---------------------------------------------------------------------------
    public LikesListAdapter(Context context,AvatarManager<FeedLike> avatarManager) {
    	mContext = context;
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
    	return super.getViewTypeCount() + T_COUNT;
    }
    //---------------------------------------------------------------------------
    @Override
    public int getItemViewType(int position) {
    	int typeOfSuperMethod = super.getItemViewType(position); 
    	if (typeOfSuperMethod == T_NONE) {
    		return mAvatarManager.get(position).city_id == mOwnerCityID ? T_CITY : T_ALL;
    	} else {
    		return typeOfSuperMethod;
    	}
    }
    //---------------------------------------------------------------------------
    @Override
    public View getView(final int position,View convertView,ViewGroup parent) {
//    	long startTime = System.currentTimeMillis();
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

                convertView = mInflater.inflate(R.layout.item_likes_gallery, null, false);
//                convertView.setOnTouchListener(new View.OnTouchListener() {
//					float x;
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						switch (event.getAction()) {
//						case MotionEvent.ACTION_DOWN:
//							x = event.getX();
//							break;
//						case MotionEvent.ACTION_MOVE:
//							float x1 = event.getX();
//							if(x-x1>100)
//								Toast.makeText(mContext, "asd", Toast.LENGTH_LONG).show();
//							break;
//						case MotionEvent.ACTION_UP:
//							break;
//						
//						}
//						return true;
//					}
//					
//				});
                holder.mAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
                holder.mAvatarMask = (ImageView)convertView.findViewById(R.id.ivAvatarMask);
                holder.mName = (TextView)convertView.findViewById(R.id.tvName);
                holder.mCity = (TextView)convertView.findViewById(R.id.tvCity);
                holder.mTime = (TextView)convertView.findViewById(R.id.tvTime);
                holder.mHeart = (ImageView)convertView.findViewById(R.id.ivHeart);
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
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
        	
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
