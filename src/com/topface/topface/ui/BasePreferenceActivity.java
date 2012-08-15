package com.topface.topface.ui;

import java.util.LinkedList;

import android.preference.PreferenceActivity;

import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.http.IRequestClient;

public class BasePreferenceActivity extends PreferenceActivity implements IRequestClient{

	private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();
	
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