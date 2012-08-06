package com.topface.topface.ui.adapters;

import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.utils.GiftGalleryManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GiftsAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	
	private GiftGalleryManager<Gift> mGalleryManager;
	
	public GiftsAdapter(Context context, GiftGalleryManager<Gift> galleryManager) {
		mInflater = LayoutInflater.from(context);
	    mGalleryManager = galleryManager;
	}
	
	@Override
	public int getCount() {
		return mGalleryManager.size();
	}	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
	    
	    if(convertView==null) {
	        convertView = (ViewGroup)mInflater.inflate(R.layout.item_gift, null, false);
            
	        holder = new ViewHolder();
            holder.mGiftImage = (ImageView)convertView.findViewById(R.id.giftImage);
            holder.mGiftMask = (ImageView)convertView.findViewById(R.id.giftMask);
            holder.mPriceText = (TextView) convertView.findViewById(R.id.giftPrice);

            convertView.setTag(holder);
	    } else {
	        holder = (ViewHolder)convertView.getTag();
	    }
		
//	    holder.mGiftImage.getLayoutParams().width = mGalleryManager.mBitmapWidth;
//	    holder.mGiftImage.getLayoutParams().height = mGalleryManager.mBitmapHeight;
//	    
//	    holder.mGiftImage.getLayoutParams().width = holder.mGiftImage.getLayoutParams().width;
//	    holder.mGiftMask.getLayoutParams().height = holder.mGiftImage.getLayoutParams().height;
	    	    
	    holder.mGift = ((Gift)mGalleryManager.get(position));
	    
	    mGalleryManager.getImage(position,(ImageView) holder.mGiftImage);
	    holder.mPriceText.setText(Integer.toString(holder.mGift.price));	    
	    
	    return convertView;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}	
	
	public class ViewHolder {
	    ImageView mGiftImage;
	    ImageView mGiftMask;
	    TextView mPriceText;
	    public Gift mGift;
	}
	
	public void release() {
	    mGalleryManager.release();
	}
}
