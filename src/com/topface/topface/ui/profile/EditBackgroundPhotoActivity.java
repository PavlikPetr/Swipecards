package com.topface.topface.ui.profile;

import java.util.LinkedList;

import com.topface.topface.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class EditBackgroundPhotoActivity extends Activity {
	
	ListView mBackgroundImagesListView;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_edit_background_photo);
		
		// Navigation bar		
		((TextView) findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
		TextView subTitle = (TextView) findViewById(R.id.tvNavigationSubtitle);
		subTitle.setVisibility(View.VISIBLE);
		subTitle.setText(R.string.edit_bg_photo);
		
		((Button)findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);		
		Button btnBack = (Button)findViewById(R.id.btnNavigationBackWithText);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setText(R.string.navigation_edit);		
		
		mBackgroundImagesListView = (ListView) findViewById(R.id.lvBackgroundImages);
		
		mBackgroundImagesListView.setAdapter(new BackgroundImagesAdapter(getApplicationContext(), getBackgroundImagesList()));
	}
	
	private LinkedList<Bitmap> getBackgroundImagesList() {
		LinkedList<Bitmap> result = new LinkedList<Bitmap>();
		result.add(BitmapFactory.decodeResource(getResources(), R.drawable.profile_background_1));
		
		result.add(BitmapFactory.decodeResource(getResources(), R.drawable.profile_background_2));
		
		result.add(BitmapFactory.decodeResource(getResources(), R.drawable.profile_background_3));
		return result;
	}
	
	class BackgroundImagesAdapter extends BaseAdapter {

		private LinkedList<Bitmap> mData;
		private int mSelectedIndex;
		private LayoutInflater mInflater;
		
		public BackgroundImagesAdapter(Context context, LinkedList<Bitmap> data) {
			mData = data;
			mInflater = LayoutInflater.from(context);			
		}
		
		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Bitmap getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
			if (convertView == null) {
				holder = new ViewHolder();
				
				convertView = mInflater.inflate(R.layout.item_edit_background_photo, null, false);
				holder.mImageView = (ImageView)convertView.findViewById(R.id.ivBackgroundImage);
				holder.mFrameImageView = (ImageView)convertView.findViewById(R.id.ivBackgroundImageFrame);
				holder.mSelected = (ViewGroup)convertView.findViewById(R.id.loSelected);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			LayoutParams params = holder.mImageView.getLayoutParams();
			params.height = holder.mFrameImageView.getDrawable().getIntrinsicHeight() - 2;
			params.width = holder.mFrameImageView.getDrawable().getIntrinsicWidth() - 2;
			holder.mImageView.setImageBitmap(getItem(position));
			
			final int currentPosition = position;
			
			if (position == mSelectedIndex) {
				holder.mSelected.setVisibility(View.VISIBLE);
				convertView.setOnClickListener(null);
			} else {
				holder.mSelected.setVisibility(View.GONE);
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mSelectedIndex = currentPosition;
						notifyDataSetChanged();
					}
				});
			}
			
			return convertView;
		}
		
		class ViewHolder {
			ImageView mImageView;
			ImageView mFrameImageView;
			ViewGroup mSelected;
		}
	}
}
