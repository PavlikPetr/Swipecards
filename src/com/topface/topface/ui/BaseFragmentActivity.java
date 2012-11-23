package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.http.IRequestClient;

import java.util.LinkedList;

public class BaseFragmentActivity extends FragmentActivity implements IRequestClient {

	public static final String INTENT_PREV_ENTITY = "prev_entity";
	
    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
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

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        super.startActivityForResult(intent, requestCode);
    }
}
