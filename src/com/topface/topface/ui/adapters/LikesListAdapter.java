package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.topface.topface.R;
import com.topface.topface.data.FeedLike;

public class LikesListAdapter extends FeedAdapter<FeedLike> {
	private int mSelectedForMutual = -1;
	private int mPrevSelectedForMutual = -1;
	
	private int T_SELETED_FOR_MUTUAL = 3;
    private int T_COUNT = 1;
	
    private OnMutualListener mMutualListener;    
    
    public interface OnMutualListener {
    	void onMutual(int userId,int rate,int mutualId);
    }
    
    public LikesListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);        
    }    
    
    @Override
    public int getItemViewType(int position) {  
    	if (mSelectedForMutual == position && !getItem(position).mutualed) {    		
    		return T_SELETED_FOR_MUTUAL;
    	}
    	else return super.getItemViewType(position);
    }
    
    @Override
    public int getViewTypeCount() {
    	
    	return (super.getViewTypeCount()+T_COUNT);
    }
    
    @Override
    protected View getContentView(final int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();
        final FeedLike like = getItem(position);

        holder.heart.setImageResource(like.mutualed ? R.drawable.im_item_dbl_mutual_heart : 
        	(like.highrate ? R.drawable.im_item_mutual_heart_top : R.drawable.im_item_mutual_heart));
                
        final ViewFlipper vf = holder.flipper;
        
        holder.heart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setSelectedForMutual(position);	
			}
		});
        
        if(position == mSelectedForMutual) {
        	vf.setInAnimation(getContext(),R.anim.slide_in_from_right);
	        vf.setOutAnimation(getContext(),android.R.anim.fade_out);
	        vf.setDisplayedChild(1);
			convertView.setActivated(true);
			holder.flippedBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mMutualListener != null) {
						mMutualListener.onMutual(getItem(position).uid, 9,like.id);
						setSelectedForMutual(-1);
						like.mutualed = true;
					}
				}
			});

        } else {
        	if (mPrevSelectedForMutual == position) {
        		vf.setInAnimation(getContext(),android.R.anim.fade_in);
            	vf.setOutAnimation(getContext(),R.anim.slide_out_right);
        		vf.setDisplayedChild(0);
        		convertView.setActivated(false);
        		mPrevSelectedForMutual = -1;
        	}
        }        
                
        return convertView;
    }

    public void setSelectedForMutual(int position) {
    	if(position != -1) {
	    	if(!getItem(position).mutualed) {
		    	mPrevSelectedForMutual = mSelectedForMutual;
				mSelectedForMutual = position;
				notifyDataSetChanged();
	    	}
    	} else {
    		mPrevSelectedForMutual = mSelectedForMutual;
			mSelectedForMutual = position;
			notifyDataSetChanged();
    	}
    }
        
    @Override
    protected FeedViewHolder getEmptyHolder(View convertView, FeedLike item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.heart = (ImageView) convertView.findViewById(R.id.ivHeart);
        return holder;
    }

    @Override
    protected FeedLike getNewItem(IListLoader.ItemType type) {
        return new FeedLike(type);
    }

    @Override
    protected int getItemLayout() {
        return R.layout.item_feed_like;
    }
    
    public void setOnMutualListener(OnMutualListener listener) {
    	mMutualListener = listener;
    }        

}
