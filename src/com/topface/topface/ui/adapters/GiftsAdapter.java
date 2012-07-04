package com.topface.topface.ui.adapters;

import com.topface.topface.data.Gift;
import com.topface.topface.utils.GiftGalleryGridManager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GiftsAdapter extends BaseAdapter{

	private Context mContext;
	
//	private LayoutInflater mInflater;
	private GiftGalleryGridManager<Gift> mGalleryManager;
	
	public GiftsAdapter(Context context, GiftGalleryGridManager<Gift> galleryManager) {
		mContext = context;
//		mInflater = LayoutInflater.from(context);
	    mGalleryManager = galleryManager;
	}
	
	@Override
	public int getCount() {
		return mGalleryManager.size();
	}	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null) {
			convertView = new ImageView(mContext);
	    }
		
	    mGalleryManager.getImage(position,(ImageView) convertView);
	    
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
}
