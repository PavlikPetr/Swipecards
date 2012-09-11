package com.topface.topface.ui.profile;

import java.util.LinkedList;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.profile.EditProfileItem.Type;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class EditProfileActivity extends Activity implements OnClickListener{

	public int INTENT_EDIT_CHANGES = 323;
	
	private ListView mEditsListView;	
	private EditsAdapter mAdapter;
	private LinkedList<EditProfileItem> mEditItems;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_edit_profile);

		// Navigation bar
		((TextView) findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
		((Button) findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);
		Button btnBackToProfile = (Button) findViewById(R.id.btnNavigationBackWithText);
		btnBackToProfile.setText(R.string.navigation_back_profile);
		btnBackToProfile.setVisibility(View.VISIBLE);
		btnBackToProfile.setOnClickListener(this);		
		
		// ListView
		mEditItems = new LinkedList<EditProfileItem>();
		initEditItems();

		mEditsListView = (ListView) findViewById(R.id.lvEdits);		
		
		// Header 
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.item_edit_profile_header, mEditsListView, false);

		Button editName = (Button) header.findViewById(R.id.btnEditName);
		editName.setText(CacheProfile.first_name + ", " + CacheProfile.age);
		editName.setOnClickListener(this);
		Button editCity = (Button) header.findViewById(R.id.btnEditCity);
		editCity.setText(CacheProfile.city_name);
		editCity.setOnClickListener(this);
				
		mEditsListView.addHeaderView(header);
		mAdapter = new EditsAdapter(getApplicationContext(), mEditItems);
		mEditsListView.setAdapter(mAdapter);
		//TODO set avatar image by id ivProfilePhoto
	}

	private void initEditItems() {		
		mEditItems.add((new EditStatus()).setType(Type.TOP));
		mEditItems.add((new EditBackPhoto()).setType(Type.MIDDLE));		
		mEditItems.add((new EditPhotos()).setType(Type.BOTTOM));		
		
		// edit form items
		FormItem prevFormItem = null;
		for (int i=0;i<CacheProfile.forms.size();i++) {
			FormItem formItem = CacheProfile.forms.get(i);			 
			EditProfileItem item = null;
			
			// set text info
			if (formItem.type == FormItem.HEADER) {
				item = (new EditHeader()).setText(formItem.title);
			} else if (formItem.type == FormItem.DATA) {
				item = (new EditForm()).setFormItem(formItem);
			} else if (formItem.type == FormItem.DIVIDER) {
                continue;
            }
			
			// set position type info
			if (prevFormItem != null && prevFormItem.type == FormItem.HEADER) {
				item.setType(Type.TOP);
			} else if(i+1 < CacheProfile.forms.size()) {
			    int type = CacheProfile.forms.get(i+1).type; 
				if(type == FormItem.HEADER || type == FormItem.DIVIDER) {
					item.setType(Type.BOTTOM);
				}
			} else if (i == CacheProfile.forms.size()-1){
				item.setType(Type.BOTTOM);
			} else {
				item.setType(Type.MIDDLE);
			}
			
			mEditItems.add(item);
			prevFormItem = formItem;
		}			
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnEditName:
			//TODO edit name onClick()
			break;
		case R.id.btnEditCity:
			//TODO edit city onClick()
			break;
		case R.id.btnNavigationBackWithText:
			finish();
			break;
		}
	}
	
	class EditsAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private LinkedList<EditProfileItem> mData;

		private int T_HEADER = 0;
		private int T_EDIT_ITEM = 1;
		private int T_COUNT = 2;

		public EditsAdapter(Context context, LinkedList<EditProfileItem> data) {
			mInflater = LayoutInflater.from(context);
			mData = data;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public EditProfileItem getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			if (getItem(position) instanceof EditHeader) {
				return T_HEADER;
			}
			return T_EDIT_ITEM;
		}

		@Override
		public int getViewTypeCount() {			
			return T_COUNT;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final EditProfileItem item = getItem(position);

			// get holder
			ViewHolder holder = null;
			if(convertView == null) {
				holder = new ViewHolder();
				
				convertView = mInflater.inflate(item.getLayoutResId(), null, false);
				
				holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);
				holder.mText = (TextView) convertView.findViewById(R.id.tvText);
				holder.mBackground = (ImageView) convertView.findViewById(R.id.ivEditBackground);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}			

			// set background image			
			switch (item.getType()) {
			case TOP:
				holder.mBackground.setImageDrawable(getResources().getDrawable(
						R.drawable.edit_big_btn_top_selector));
				break;
			case MIDDLE:
				holder.mBackground.setImageDrawable(getResources().getDrawable(
						R.drawable.edit_big_btn_middle_selector));
				break;
			case BOTTOM:
				holder.mBackground.setImageDrawable(getResources().getDrawable(
						R.drawable.edit_big_btn_bottom_selector));
				break;
			case NONE:
				break;
			}			
			
			
			// set text
			if (item instanceof EditHeader) {				
				if(!item.getTitle().equals(Static.EMPTY)) {
					holder.mTitle.setText(item.getTitle());
					holder.mTitle.setVisibility(View.VISIBLE);
				} else {
					holder.mTitle.setVisibility(View.GONE);
				}
				
			} else {
				holder.mTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				holder.mText.setVisibility(View.GONE);
				if (item instanceof EditStatus) {
					holder.mTitle.setText(item.getTitle());
				} else if (item instanceof EditBackPhoto) {
					holder.mTitle.setText(item.getTitle());
					holder.mTitle.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
				} else if (item instanceof EditPhotos) {
					holder.mTitle.setText(item.getTitle());
					holder.mTitle.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
				} else if (item instanceof EditInterests) {
					holder.mTitle.setText(item.getTitle());				
				} else if (item instanceof EditForm) {
					holder.mTitle.setText(item.getTitle());				
					holder.mText.setVisibility(View.VISIBLE);
					holder.mText.setText(item.getText());
				}
				
				holder.mBackground.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						item.onClick();
					}
				});
			}
			
			
			return convertView;
		}
		
		class ViewHolder {
			TextView mTitle;
			TextView mText;
			ImageView mBackground;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == INTENT_EDIT_CHANGES) {
				mAdapter.notifyDataSetChanged();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	class EditStatus extends EditProfileItem {

		@Override
		public String getTitle() {
			return CacheProfile.status;
		}

		@Override
		void onClick() {
			// TODO
		}
	}

	class EditBackPhoto extends EditProfileItem {

		@Override
		public String getTitle() {
			return getResources().getString(R.string.edit_bg_photo);
		}

		@Override
		public Drawable getIcon() {
			Bitmap original = BitmapFactory.decodeResource(getResources(),
					CacheProfile.background_res_id);
			BitmapDrawable resized = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
					original, 46, 35, true));

			return resized;
		}

		@Override
		void onClick() {
			startActivityForResult(new Intent(getApplicationContext(), EditBackgroundPhotoActivity.class),INTENT_EDIT_CHANGES);
		}
	}

	class EditPhotos extends EditProfileItem {

		@Override
		public String getTitle() {
			int quantity = 0;
			if (CacheProfile.photoLinks != null) {
				quantity = CacheProfile.photoLinks.size();
			}
			return quantity + " " + getResources().getString(R.string.edit_album_photos);
		}

		@Override
		public Drawable getIcon() {
			return getResources().getDrawable(R.drawable.edit_icon_photo);
		}

		@Override
		void onClick() {
			// TODO
		}
	}

	class EditInterests extends EditProfileItem {

		@Override
		public String getTitle() {
			return 0 + " " + getResources().getString(R.string.edit_interests);
			// TODO interests number
		}

		@Override
		void onClick() {
			// TODO
		}
	}

	class EditForm extends EditProfileItem {

		private FormItem mFormItem;

		@Override
		public String getTitle() {
			return mFormItem.title;
		}

		@Override
		public String getText() {
			return mFormItem.value;
		}
		
		public EditForm setFormItem(FormItem item) {
			mFormItem = item;
			return this;
		}

		@Override
		void onClick() {
			// TODO
		}
	}

	class EditHeader extends EditProfileItem {

		private String mTitle = "";

		@Override
		public String getTitle() {
			return mTitle;
		}

		@Override
		void onClick() { }

		@Override
		public int getLayoutResId() {
			return R.layout.item_edit_profile_form_header;
		}

		@Override
		public Type getType() {
			return Type.NONE;
		}

		public EditProfileItem setText(String text) {
			mTitle = text;
			return this;
		}
		
		public EditProfileItem setText(int resId) {
			mTitle = getResources().getString(resId);
			return this;
		}
	}
}
