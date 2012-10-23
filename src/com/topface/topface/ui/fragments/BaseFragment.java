package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.http.IRequestClient;

import java.util.LinkedList;

public abstract class BaseFragment extends Fragment implements IRequestClient {

	protected NavigationBarController mNavBarController;
	
    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();

    public static final int F_PROFILE = 1001;
    public static final int F_DATING = 1002;
    public static final int F_LIKES = 1003;
    public static final int F_MUTUAL = 1004;
    public static final int F_DIALOGS = 1005;
    public static final int F_TOPS = 1006;
    public static final int F_SETTINGS = 1007;
    public static final int F_VISITORS = 1008;

    protected void onUpdateStart(boolean isFlyUpdating) {
    }

    protected void onUpdateSuccess(boolean isFlyUpdating) {
    }

    protected void onUpdateFail(boolean isFlyUpdating) {
    }

    protected void updateUI(Runnable action) {
        getActivity().runOnUiThread(action);
    }

    @Override
    public void onResume() {
    	if (mNavBarController != null) mNavBarController.refreshNotificators();
    	super.onResume();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        for (ApiRequest request : mRequests) {
            request.cancel();
        }
        mRequests.clear();
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
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        super.startActivityForResult(intent, requestCode);
    }
}
