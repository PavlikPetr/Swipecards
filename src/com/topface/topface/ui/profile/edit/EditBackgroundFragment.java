package com.topface.topface.ui.profile.edit;

import java.util.LinkedList;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.CacheProfile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class EditBackgroundFragment extends Fragment {
	
	private SharedPreferences mPreferences;	
	private int mSelectedResId;
	private ListView mBackgroundImagesListView;	
	
	int[] backgrounds = new int[]{
			R.drawable.profile_background_1,
			R.drawable.profile_background_2,
			R.drawable.profile_background_3
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_with_listview, container, false);
		
		mPreferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
		mSelectedResId = CacheProfile.background_res_id;
		// Navigation bar		
		((TextView) root.findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
		TextView subTitle = (TextView) root.findViewById(R.id.tvNavigationSubtitle);
		subTitle.setVisibility(View.VISIBLE);
		subTitle.setText(R.string.edit_bg_photo);
		
		((Button)root.findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);		
		Button btnBack = (Button)root.findViewById(R.id.btnNavigationBackWithText);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setText(R.string.navigation_edit);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setChanges();
				getActivity().finish();				
			}
		});
		
		mBackgroundImagesListView = (ListView) root.findViewById(R.id.lvList);
		
		mBackgroundImagesListView.setAdapter(new BackgroundImagesAdapter(getActivity().getApplicationContext(), getBackgroundImagesList()));
		
		return root;
	}	
	
	private LinkedList<BackgroundItem> getBackgroundImagesList() {
		LinkedList<BackgroundItem> result = new LinkedList<BackgroundItem>();
				
		for (int i = 0; i < backgrounds.length; i++) {
			boolean selected = CacheProfile.background_res_id == backgrounds[i] ? true : false;
			result.add(new ResourceBackgroundItem(getResources(), backgrounds[i]).setSelected(selected));
		}
		
		return result;
	}
	
	class BackgroundImagesAdapter extends BaseAdapter {

		private LinkedList<BackgroundItem> mData;
		private LayoutInflater mInflater;
		private int mSelectedIndex;
		
		public BackgroundImagesAdapter(Context context, LinkedList<BackgroundItem> data) {
			mData = data;
			mInflater = LayoutInflater.from(context);			
		}		
		
		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public BackgroundItem getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			final BackgroundItem item = getItem(position);
			
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
			holder.mImageView.setImageBitmap(getItem(position).getBitmap());			
			
			if (item.isSelected()) {
				holder.mSelected.setVisibility(View.VISIBLE);
				convertView.setOnClickListener(null);
				mSelectedIndex = position;
			} else {
				holder.mSelected.setVisibility(View.GONE);
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mData.get(mSelectedIndex).setSelected(false);
						item.setSelected(true);						
						setSelectedBackground(item);						
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
	
	private void setSelectedBackground(BackgroundItem item) {
		if(item instanceof ResourceBackgroundItem) {			
			mSelectedResId = ((ResourceBackgroundItem)item).getResourceId();
		}
	}
	
	private void setChanges() {
		if (CacheProfile.background_res_id != mSelectedResId) {						
			CacheProfile.background_res_id = mSelectedResId;
			mPreferences.edit().putInt(Static.PREFERENCES_PROFILE_BACKGROUND_RES_ID, mSelectedResId).commit();
			getActivity().setResult(Activity.RESULT_OK);
		} else {
			getActivity().setResult(Activity.RESULT_CANCELED);
		}
	}
	
	
	@Override
	public void onDestroy() {
		setChanges();
		super.onDestroy();
	}
	
	interface BackgroundItem {
		public Bitmap getBitmap();
		public boolean isSelected();
		public BackgroundItem setSelected(boolean selected);
	}
	
	class ResourceBackgroundItem implements BackgroundItem{
		
		private Bitmap mBitmap;
		private boolean selected;
		private int mResId;		
		
		public ResourceBackgroundItem(Resources resources, int resId) {
			mBitmap = BitmapFactory.decodeResource(resources, resId);
			mResId = resId;
		}		
		
		public Bitmap getBitmap() {
			return mBitmap;
		}

		public boolean isSelected() {
			return selected;
		}

		public BackgroundItem setSelected(boolean selected) {
			this.selected = selected;
			return (BackgroundItem) this;
		}
		
		public int getResourceId() {
			return mResId;
		}
	}
	
	class BitmapBackgroundItem implements BackgroundItem{
		
		private Bitmap mBitmap;
		private boolean selected;
		
		public BitmapBackgroundItem(Bitmap bitmap) {
			mBitmap = bitmap;
		}
		
		public Bitmap getBitmap() {
			return mBitmap;
		}

		public boolean isSelected() {
			return selected;
		}

		public BackgroundItem setSelected(boolean selected) {
			this.selected = selected;
			return (BackgroundItem) this;
		}		
	}
}
