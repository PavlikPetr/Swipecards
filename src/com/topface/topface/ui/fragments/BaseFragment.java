package com.topface.topface.ui.fragments;

import java.util.LinkedList;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.http.IRequestClient;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment implements IRequestClient {

    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();
    
    public static final int F_BASE     = 1000;
    public static final int F_PROFILE  = 1001;
    public static final int F_DATING   = 1002;
    public static final int F_LIKES    = 1003;
    public static final int F_MUTUAL   = 1004;
    public static final int F_DIALOGS  = 1005;
    public static final int F_TOPS     = 1006;
    public static final int F_SETTINGS = 1007;

    //abstract public void fillLayout();
    //abstract public void clearLayout();
    
    protected void onUpdateStart(boolean isFlyUpdating){ };
    protected void onUpdateSuccess(boolean isFlyUpdating){ };
    protected void onUpdateFail(boolean isFlyUpdating){ };

    protected void updateUI(Runnable action) {
        getActivity().runOnUiThread(action);
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

}
