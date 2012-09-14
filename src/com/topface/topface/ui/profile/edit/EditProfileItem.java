package com.topface.topface.ui.profile.edit;

import com.topface.topface.R;

import android.graphics.drawable.Drawable;

public abstract class EditProfileItem {	
	public enum Type {TOP, MIDDLE, BOTTOM, NONE};
	
	private Type mType = Type.MIDDLE;
	
	public String getTitle() {
		return null;
	}
	
	public String getText() {
		return null;
	}
	
	public Drawable getIcon() {
		return null;
	}
	
	public Type getType() {
		return mType;
	}
	
	public int getLayoutResId() {
		return R.layout.item_edit_profile_form;
	}
	
	public EditProfileItem setType(Type type) {
		mType = type;
		return this;
	}
		
	abstract void onClick();
}
