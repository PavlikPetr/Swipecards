package com.topface.topface.ui;

import java.util.LinkedList;

import android.content.Intent;
import android.preference.PreferenceActivity;

import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.AuthorizationManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.IRequestClient;

public class BasePreferenceActivity extends PreferenceActivity implements IRequestClient{

	protected AuthorizationManager mAuthorizationManager;
	
	private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		mAuthorizationManager.onActivityResult(requestCode, resultCode, data);
		Debug.log(this, "onActivityResult");
	}
	
	@Override
	public void registerRequest(ApiRequest request) {
		if (!mRequests.contains(request)) {
			mRequests.add(request);
		}
	}

	@Override
	public void removeRequest(ApiRequest request) {
		mRequests.remove(request);
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		for (ApiRequest request : mRequests) {
			request.cancel();
		}
		mRequests.clear();
	}
}