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
        
        ((TextView)findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
        
        mEditItems = new LinkedList<EditProfileItem>();
        initEditItems();
        
        mEditsList = (ListView) findViewById(R.id.lvEdits);
        mEditsList.setAdapter(new EditsAdapter(getApplicationContext(),mEditItems));
        
	}
	
	private void initEditItems() {		
		mEditItems.add((new EditStatus()).setType(Type.TOP));
		mEditItems.add((new EditBackPhoto()).setType(Type.BOTTOM));
		
		mEditItems.add((new EditHeader()));
		mEditItems.add((new EditPhotos()).setType(Type.TOP));
		mEditItems.add((new EditInterests()).setType(Type.BOTTOM));
		
		mEditItems.add((new EditHeader()));
		mEditItems.add((new EditHeader()).setText("Интелектуально-личностные"));
		mEditItems.add((new EditPhotos()).setType(Type.TOP));
		mEditItems.add((new EditInterests()).setType(Type.MIDDLE));
		mEditItems.add((new EditBackPhoto()).setType(Type.BOTTOM));
	}
	
	class EditsAdapter extends BaseAdapter {		
		
		private LayoutInflater mInflater;
		private LinkedList<EditProfileItem> mData; 
		
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
		
		//TODO create Types

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			EditProfileItem item = getItem(position);
			
			ViewGroup view = (ViewGroup)mInflater.inflate(item.getLayoutResId(),null,false);
			
			// background image
			ImageView background = (ImageView)view.findViewById(R.id.ivEditBackground);
			switch (item.getType()) {
			case TOP:
				background.setImageDrawable(getResources().getDrawable(R.drawable.edit_big_btn_top_selector));
				break;
			case MIDDLE:
				background.setImageDrawable(getResources().getDrawable(R.drawable.edit_big_btn_middle_selector));
				break;
			case BOTTOM:
				background.setImageDrawable(getResources().getDrawable(R.drawable.edit_big_btn_bottom_selector));
				break;
			case NONE:
				break;
			}
			
			// text
			TextView title = (TextView)view.findViewById(R.id.tvTitle);
			if (item instanceof EditStatus) {
				title.setText(item.getTitle());				
			} else if (item instanceof EditBackPhoto) {
				title.setText(item.getTitle());
				title.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
			} else if (item instanceof EditPhotos) {
				title.setText(item.getTitle());
				title.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
			} else if (item instanceof EditInterests) {
				title.setText(item.getTitle());
			} else if (item instanceof EditHeader) {
				title.setText(item.getTitle());
			}
			
			return view;
		}
		
	}
	
	class EditStatus extends EditProfileItem {

		@Override
		public String getTitle() {
			return CacheProfile.form_status;
		}
		
		@Override
		void onClick() {
			//TODO
		}
	}
	
	class EditBackPhoto extends EditProfileItem {

		@Override
		public String getTitle() {
			return getResources().getString(R.string.edit_bg_photo);
		}
		
		@Override
		public Drawable getIcon() {
			Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.user_avatar_bg); 
			BitmapDrawable resized = new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(original, 46, 35, true));
			
			return resized;
		}
		
		@Override
		void onClick() {
			//TODO
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
			//TODO
		}
	}
	
	class EditInterests extends EditProfileItem {

		@Override
		public String getTitle() {			
			return CacheProfile.forms_quantity + " " + getResources().getString(R.string.edit_interests);
		}		
		
		@Override
		void onClick() {
			//TODO
		}
	}
	
	class EditHeader extends EditProfileItem {

		private String mText = "";
		
		@Override
		public String getTitle() {
			return mText;
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
			mText = text;
			return this;
		}
	}
}
