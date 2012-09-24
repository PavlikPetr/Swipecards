package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;

public interface SocialManager {
	public final static int AUTHORIZATION_FAILED = 0;
	public final static int TOKEN_RECEIVED = 1;
	public final static int DIALOG_COMPLETED = 2;
	
	public void authorize(Activity activity);
	public void onActivityResult(int requestCode, int resultCode, Intent data);
}
