package com.topface.topface.ui.fragments;

import java.util.LinkedList;

import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Banner;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.http.Http;
import com.topface.topface.utils.http.IRequestClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;

public abstract class BaseFragment extends Fragment implements IRequestClient{

    public boolean isFilled;
    public boolean isForcedUpdate;
    protected boolean mIsActive;
    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();
    private int mFragmentId;

    abstract public void fillLayout();
    abstract public void clearLayout();
    
    protected void onUpdateStart(boolean isFlyUpdating){ };
    protected void onUpdateSuccess(boolean isFlyUpdating){ };
    protected void onUpdateFail(boolean isFlyUpdating){ };

    protected void updateUI(Runnable action) {
        //if (mIsActive)
            getActivity().runOnUiThread(action);
    }
    
    public void setId(int fragmentId) {
        mFragmentId = fragmentId;
    }

    public void updateBanner(final ImageView bannerView,final String bannerRequestName) {
        if (Data.screen_width <= Device.W_240 || bannerView == null || bannerRequestName == null)
            return;

        BannerRequest bannerRequest = new BannerRequest(getActivity().getApplicationContext());
        registerRequest(bannerRequest);
        bannerRequest.place = bannerRequestName;
        bannerRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final Banner banner = Banner.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Http.bannerLoader(banner.url, bannerView);
                        bannerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = null;
                                if (banner.action.equals(Banner.ACTION_PAGE))
                                    intent = new Intent(getActivity(), BuyingActivity.class); // "parameter":"PURCHASE"
                                else if (banner.action.equals(Banner.ACTION_URL)) {
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                                }
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                Debug.log(this, "banner loading error");
            }
        }).exec();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        if(activity instanceof NavigationActivity) {
//            int lastFragmentId = activity.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE)
//                                         .getInt(Static.PREFERENCES_NAVIGATION_LAST_FRAGMENT, R.id.fragment_profile);
//            if(mFragmentId == lastFragmentId)
//            	((NavigationActivity)activity).setSelectedMenu(mFragmentId);
//        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(isForcedUpdate) {
            fillLayout();
            isForcedUpdate = false;
            isFilled = true;
        }
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
