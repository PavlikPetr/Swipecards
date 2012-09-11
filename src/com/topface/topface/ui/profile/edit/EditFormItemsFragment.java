package com.topface.topface.ui.profile.edit;

import java.text.Normalizer.Form;
import java.util.LinkedList;

import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class EditFormItemsFragment extends Fragment {
	
	private static int mTitleId;
	private static int mDataId;	
	private FormInfo mFormInfo;
	private static int mSeletedDataId;
	
	private ListView mListView;
	
	public EditFormItemsFragment(int titleId, int dataId) {
		mTitleId = titleId;
		mDataId = dataId;
		mSeletedDataId = mDataId;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_with_listview, container, false);
		
		mFormInfo = new FormInfo(getActivity().getApplicationContext(), CacheProfile.getProfile());
				
		
		mListView = (ListView) root.findViewById(R.id.lvList);
		mListView.setAdapter(new FormDataAdapter(getActivity().getApplicationContext(),
				mFormInfo.getEntriesByTitleId(mTitleId),
				mFormInfo.getIdsByTitleId(mTitleId),
				mSeletedDataId));
		
		return root;
	}	
	
	@Override
	public void onDestroy() {		
		super.onDestroy();
		setChanges();
	}
	
	private void setSelectedId(int id) {
		mSeletedDataId = id;
	}
	
	private void setChanges() {
		if (mDataId != mSeletedDataId) {
			for (FormItem item : CacheProfile.forms) {
				if (item.titleId == mTitleId) {
					item.dataId = mSeletedDataId;
					mFormInfo.fillFormItem(item);
					break;
				}
			}
		}
	}
	
	private class FormDataAdapter extends BaseAdapter {

		LayoutInflater mInflater;
		String[] mData;
		int[] mIds;
		int mLastSelected;
		
		public FormDataAdapter(Context context, String[] data,int[] ids, int selectedId) {
			mInflater = LayoutInflater.from(context);
			mData = data;
			mIds = ids;
			mLastSelected = getSelectedIndex(selectedId);
		}
		
		private int getSelectedIndex(int selectedId) {
			for (int i = 0; i < mIds.length; i++) {
				if(mIds[i] == selectedId) {
					return i;
				}
			}
			return -1;
		}
		
		@Override
		public int getCount() {
			return mData.length;
		}

		@Override
		public String getItem(int position) {
			return mData[position];
		}

		@Override
		public long getItemId(int position) {
			return mIds[position];
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;			
			
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.item_edit_profile_form, null, false);
				holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);				
				holder.mBackground = (ImageView) convertView.findViewById(R.id.ivEditBackground);
				holder.mCheck = (ImageView) convertView.findViewById(R.id.ivCheck);
				convertView.findViewById(R.id.ivArrow).setVisibility(View.GONE);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			if (position == 0) {
				holder.mBackground.setImageDrawable(getResources().getDrawable(
						R.drawable.edit_big_btn_top_selector));
			} else if (position == getCount()-1) {
				holder.mBackground.setImageDrawable(getResources().getDrawable(
						R.drawable.edit_big_btn_bottom_selector));
			} else {
				holder.mBackground.setImageDrawable(getResources().getDrawable(
						R.drawable.edit_big_btn_middle_selector));
			}
			
			if (mLastSelected == position) {
				holder.mCheck.setVisibility(View.VISIBLE);
			} else {
				holder.mCheck.setVisibility(View.INVISIBLE);
			}
			
			holder.mTitle.setText(getItem(position));
			
			holder.mBackground.setOnClickListener(new OnClickListener() {					
				@Override
				public void onClick(View v) {
					mLastSelected = position;
					setSelectedId((int)getItemId(position));
					notifyDataSetChanged();
				}
			});
			
			return convertView;
		}
		
		class ViewHolder {
			TextView mTitle;
			TextView mText;
			ImageView mBackground;
			ImageView mCheck;
		}
		
	}
	
}
