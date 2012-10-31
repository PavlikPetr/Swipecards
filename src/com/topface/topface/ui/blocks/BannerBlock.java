package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adfonic.android.AdfonicView;
import com.adfonic.android.api.Request;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.android.apps.analytics.easytracking.EasyTracker;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Banner;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.BaseApiHandler;
import com.topface.topface.ui.InviteActivity;
import com.topface.topface.ui.fragments.TopsFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.utils.Device;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.wapstart.plus1.sdk.Plus1BannerAsker;
import ru.wapstart.plus1.sdk.Plus1BannerRequest;
import ru.wapstart.plus1.sdk.Plus1BannerView;

/**
 * Показываем баннер на нужных страницах
 */
public class BannerBlock {

	private String ADFONIC_SLOT_ID = "9f83e583-a247-4b78-94a0-bf2beb8775fc"; 
	private int PLUS1_ID = 7227;

    private Activity mActivity;
    private Fragment mFragment;
    private View mBannerView;
    private Plus1BannerAsker mPLus1Asker;
    private Map<String, String> mBannersMap = new HashMap<String, String>();    

    public BannerBlock(Activity activity, Fragment fragment, ViewGroup layout) {
        super();
        mActivity = activity;
        mFragment = fragment;
        setBannersMap();
        
        //TODO Banner initialization
//        if (true) {
////	        mBannerView = (AdView) layout.findViewById(R.id.adMobView);
//	        
////	        mBannerView = (AdfonicView) layout.findViewById(R.id.adFonicView);        
//						
////			mBannerView = (Plus1BannerView) layout.findViewById(R.id.adPlus1View);			
//			
//			showBanner(null);
//		} else {		
//			mBannerView = (ImageView) layout.findViewById(R.id.ivBanner);
//			if (isCorrectResolution() && mBannersMap.containsKey(mFragment.getClass().toString())) {
//				loadBanner();
//			}
//		}
    }

    private void setBannersMap() {
        mBannersMap.put(LikesFragment.class.toString(), BannerRequest.LIKE);
        mBannersMap.put(MutualFragment.class.toString(), BannerRequest.LIKE);
        mBannersMap.put(DialogsFragment.class.toString(), BannerRequest.INBOX);
        mBannersMap.put(TopsFragment.class.toString(), BannerRequest.TOP);
        mBannersMap.put(VisitorsFragment.class.toString(), BannerRequest.LIKE);
    }

    private void loadBanner() {
        BannerRequest bannerRequest = new BannerRequest(mActivity.getApplicationContext());
        bannerRequest.place = mBannersMap.get(mFragment.getClass().toString());
        bannerRequest.callback(new BaseApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final Banner banner = Banner.parse(response);
                if (mBannerView != null)
                    post(new Runnable() {
                        @Override
                        public void run() {
                            showBanner(banner);
                        }
                    });
            }
        }).exec();
    }

    private void showBanner(final Banner banner) {
    	if (mBannerView instanceof AdView) {
    		mBannerView.setVisibility(View.VISIBLE);
    		((AdView)mBannerView).loadAd(new AdRequest());    		
    	} else if (mBannerView instanceof AdfonicView) {
    		mBannerView.setVisibility(View.VISIBLE);
    		Request request = new Request();
    		request.setLanguage("en");
    		request.setSlotId(ADFONIC_SLOT_ID);
    		request.setTest(true);
    		((AdfonicView)mBannerView).loadAd(request);   		
    	}else if (mBannerView instanceof Plus1BannerView) {
    		mBannerView.setVisibility(View.VISIBLE);
    		mPLus1Asker = new Plus1BannerAsker(new Plus1BannerRequest().setApplicationId(PLUS1_ID), ((Plus1BannerView) mBannerView).enableCloseButton().enableAnimationFromBottom());
    	} else if (mBannerView instanceof ImageView) {
	        DefaultImageLoader.getInstance().displayImage(banner.url, (ImageView)mBannerView, new SimpleImageLoadingListener() {
	            @Override
	            public void onLoadingComplete(Bitmap loadedImage) {
	                super.onLoadingComplete(loadedImage);
	                float deviceWidth = Device.getDisplayMetrics(mActivity).widthPixels;
	                float imageWidth = loadedImage.getWidth();
	                //Если ширина экрана больше, чем у нашего баннера, то пропорционально увеличиваем высоту imageView
	                if (deviceWidth > imageWidth) {
	                    ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
	                    params.height = (int) ((deviceWidth / imageWidth) * (float) loadedImage.getHeight());
	                    mBannerView.setLayoutParams(params);                    
	                    mBannerView.invalidate();
	                }
	            }
	        });
	        sendStat(getBannerName(banner.url), "view");
	        mBannerView.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                Intent intent = null;
	                if (banner.action.equals(Banner.ACTION_PAGE)) {
	                    EasyTracker.getTracker().trackEvent("Purchase", "Banner", "", 0);
	                    intent = new Intent(mActivity, BuyingActivity.class); // "parameter":"PURCHASE"
	                } else if (banner.action.equals(Banner.INVITE_PAGE)) {
	                    EasyTracker.getTracker().trackEvent("Banner", "Invite", "", 0);
	                    intent = new Intent(mActivity, InviteActivity.class);
	                } else if (banner.action.equals(Banner.ACTION_URL)) {
	                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
	                }
	                sendStat(getBannerName(banner.url), "click");
	                mActivity.startActivity(intent);
	            }
	        });
        }
    }

    /**
     * Не показываем баннер на маленьких экранах
     */
    private boolean isCorrectResolution() {
        return Data.screen_width > Device.W_240;
    }

    private void sendStat(String action, String label) {
        action = action == null ? "" : action;
        label = label == null ? "" : label;
        EasyTracker.getTracker().trackEvent("Banner", action, label, 0);
    }

    private String getBannerName(String bannerUrl) {
        String name = null;
        Pattern pattern = Pattern.compile(".*\\/(.*)\\..+$");
        Matcher matcher = pattern.matcher(bannerUrl);
        matcher.find();
        if (matcher.matches()) {
            name = matcher.group(1);
        }
        return (name == null || name.length() < 1) ? bannerUrl : name;
    }
    
    public void onResume() {
    	if (mPLus1Asker != null) mPLus1Asker.onResume();
    }
    
    public void onPause() {
    	if (mPLus1Asker != null) mPLus1Asker.onPause();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (mBannerView instanceof Plus1BannerView) {
				if(((Plus1BannerView)mBannerView).canGoBack()) {
					((Plus1BannerView)mBannerView).goBack();
					return true;
				}
			}
		}
    	return false;
    }
}
