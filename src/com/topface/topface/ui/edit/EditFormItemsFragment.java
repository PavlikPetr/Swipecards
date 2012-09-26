package com.topface.topface.ui.edit;

import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.QuestionaryRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EditFormItemsFragment extends AbstractEditFragment {

	private static int mTitleId;
	private static int mDataId;
	private String mData;
	private FormInfo mFormInfo;
	private static int mSeletedDataId;
	private static Profile mProfile;	
	
	private ListView mListView;

	public EditFormItemsFragment(int titleId, int dataId, String data) {
		mTitleId = titleId;
		mDataId = dataId;
		mSeletedDataId = mDataId;
		mData = data;		
		mProfile = CacheProfile.getProfile();
	}	

	public EditFormItemsFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mFormInfo = new FormInfo(getActivity().getApplicationContext(), mProfile);

		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_with_listview, container,
				false);

		// Navigation bar
		((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
		TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
		subTitle.setVisibility(View.VISIBLE);

		String formItemTitle = mFormInfo.getFormTitle(mTitleId);
		subTitle.setText(formItemTitle);

		((Button) getActivity().findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);
		Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setText(R.string.navigation_edit);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();				
			}
		});
		
		mSaveButton = (Button) getActivity().findViewById(R.id.btnNavigationRightWithText);		
		mSaveButton.setText(getResources().getString(R.string.navigation_save));
		mSaveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveChanges();
			}
		});

		mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

		// List
		mListView = (ListView) root.findViewById(R.id.lvList);

		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.item_edit_profile_form_header,
				mListView, false);
		((TextView) header.findViewById(R.id.tvTitle)).setText(formItemTitle);
		mListView.addHeaderView(header);

		mListView.setAdapter(new FormCheckingDataAdapter(getActivity().getApplicationContext(),
				mFormInfo.getEntriesByTitleId(mTitleId, new String[] { mData }), 
				mFormInfo.getIdsByTitleId(mTitleId), mSeletedDataId));
		return root;
	}

	private void setSelectedId(int id) {
		mSeletedDataId = id;
		refreshSaveState();
	}

	@Override
	protected void saveChanges() {
		if (hasChanges()) {
			for (int i = 0; i < CacheProfile.forms.size(); i++) {
				if (CacheProfile.forms.get(i).titleId == mTitleId) {
					final FormItem item = CacheProfile.forms.get(i);
					FormItem newItem;
					newItem = new FormItem(item.titleId, mSeletedDataId, FormItem.DATA);						
					
					mFormInfo.fillFormItem(newItem);

					prepareRequestSend();
					QuestionaryRequest request = mFormInfo.getFormRequest(newItem);
					registerRequest(request);
					request.callback(new ApiHandler() {

						@Override
						public void success(ApiResponse response) throws NullPointerException {
							item.dataId = mSeletedDataId;							
							mFormInfo.fillFormItem(item);
							getActivity().setResult(Activity.RESULT_OK);							
							mDataId = mSeletedDataId;							
							finishRequestSend();
						}

						@Override
						public void fail(int codeError, ApiResponse response)
								throws NullPointerException {
							getActivity().setResult(Activity.RESULT_CANCELED);
							finishRequestSend();
						}
					}).exec();
					break;
				}
			}
		}
	}

	@Override
	public boolean hasChanges() {
		return mDataId != mSeletedDataId;
	}

	private class FormCheckingDataAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private String[] mListData;
		private int[] mIds;
		private int mLastSelected;		

		public FormCheckingDataAdapter(Context context, String[] data, int[] ids, int selectedId) {
			mInflater = LayoutInflater.from(context);
			mListData = data;
			mIds = ids;
			mLastSelected = getSelectedIndex(selectedId);
		}

		private int getSelectedIndex(int selectedId) {
			for (int i = 0; i < mIds.length; i++) {
				if (mIds[i] == selectedId) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getCount() {
			return mListData.length;
		}

		@Override
		public String getItem(int position) {
			return mListData[position];
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
				
				convertView = mInflater.inflate(R.layout.item_edit_form_check, null, false);
				holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);
				holder.mBackground = (ImageView) convertView.findViewById(R.id.ivEditBackground);
				holder.mCheck = (ImageView) convertView.findViewById(R.id.ivCheck);					
				
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

		
			if (position == 0) {
				holder.mBackground.setImageDrawable(getResources().getDrawable(
						R.drawable.edit_big_btn_top_selector));
			} else if (position == getCount() - 1) {
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
				
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mLastSelected = position;
					setSelectedId((int) getItemId(position));
					notifyDataSetChanged();
				}
			});
			
			return convertView;
		}

		class ViewHolder {
			TextView mTitle;	
			ImageView mBackground;
			ImageView mCheck;
		}
	}
}