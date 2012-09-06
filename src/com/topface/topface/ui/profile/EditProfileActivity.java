package com.topface.topface.ui.profile;

import java.util.LinkedList;

import com.topface.topface.R;
import com.topface.topface.ui.profile.EditProfileItem.Type;
import com.topface.topface.utils.CacheProfile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class EditProfileActivity extends Activity {

	private ListView mEditsList;
	private LinkedList<EditProfileItem> mEditItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_edit_profile);

		((TextView) findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);

		mEditItems = new LinkedList<EditProfileItem>();
		initEditItems();

		mEditsList = (ListView) findViewById(R.id.lvEdits);
		mEditsList.setAdapter(new EditsAdapter(getApplicationContext(), mEditItems));

	}

	private void initEditItems() {
		mEditItems.add((new EditStatus()).setType(Type.TOP));
		mEditItems.add((new EditBackPhoto()).setType(Type.BOTTOM));

		mEditItems.add((new EditHeader()));
		mEditItems.add((new EditPhotos()).setType(Type.TOP));
		mEditItems.add((new EditInterests()).setType(Type.BOTTOM));
		
		mEditItems.add((new EditHeader()).setText("Интелектуально-личностные"));
		mEditItems.add((new EditForm()).setTitle(R.string.profile_education).setText("бестолочь").setType(Type.TOP));
		mEditItems.add((new EditForm()).setTitle(R.string.profile_education).setText("бестолочь").setType(Type.MIDDLE));
		mEditItems.add((new EditForm()).setTitle(R.string.profile_education).setText("бестолочь").setType(Type.BOTTOM));
		
		mEditItems.add((new EditHeader()));
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
			EditProfileItem item = getItem(position);

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

			// background image			
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
			
			
			//text
			if (item instanceof EditHeader) {
				holder.mTitle.setText(item.getTitle());
			} else {
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
			}
			return convertView;
		}
		
		class ViewHolder {
			TextView mTitle;
			TextView mText;
			ImageView mBackground;
		}

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
					R.drawable.user_avatar_bg);
			BitmapDrawable resized = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
					original, 46, 35, true));

			return resized;
		}

		@Override
		void onClick() {
			// TODO
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

		private String mTitle = "";
		private String mText = "";

		@Override
		public String getTitle() {
			return mTitle;
		}

		@Override
		public String getText() {
			return mText;
		}

		public EditForm setTitle(String title) {
			mTitle = title;
			return this;
		}
		
		public EditForm setTitle(int resId) {
			mTitle = getResources().getString(resId);
			return this;
		}

		public EditForm setText(String text) {
			mText = text;
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
			return R.layout.item_edit_profile_header;
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
