package com.topface.topface.ui.fragments;

import com.topface.topface.Data;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Banner;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.http.Http;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public abstract class BaseFragment extends Fragment {

    protected boolean mIsActive;

    abstract public void fillLayout();
    abstract public void clearLayout();

    protected void updateUI(Runnable action) {
        //if (mIsActive)
            getActivity().runOnUiThread(action);
    }

    public void updateBanner(final ImageView bannerView,final String bannerRequestName) {
        if (Data.screen_width <= Device.W_240 || bannerView == null || bannerRequestName == null)
            return;

        BannerRequest bannerRequest = new BannerRequest(getActivity().getApplicationContext());
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
        Debug.log("onAttach:1");
    }
    
    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        Debug.log("onCreate:2");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        Debug.log("onCreateView:3");
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Debug.log("onActivityCreated:4");
    }

    @Override
    public void onStart() {
        super.onStart();
        Debug.log("onStart:5");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Debug.log("onResume:6");
    }

    @Override
    public void onPause() {
        super.onPause();
        Debug.log("onPause:7");
    }
    
    @Override
    public void onStop() {
        super.onStop();
        Debug.log("onStop:8");
    }

    @Override
    public void onDestroyView() {
         super.onDestroyView();
         Debug.log("onDestroyView:9");
    }

    @Override
    public void onDestroy() {
         super.onDestroy();
         Debug.log("onDestroyView:10");
    }
    
    @Override
    public void onDetach() {
         super.onDetach();
         Debug.log("onDetach:11");
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {

    }

}
