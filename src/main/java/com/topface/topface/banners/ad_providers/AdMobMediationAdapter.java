package com.topface.topface.banners.ad_providers;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.smaato.soma.AdDownloaderInterface;
import com.smaato.soma.AdListenerInterface;
import com.smaato.soma.AdSettings;
import com.smaato.soma.BannerStateListener;
import com.smaato.soma.BannerView;
import com.smaato.soma.BaseView;
import com.smaato.soma.ErrorCode;
import com.smaato.soma.ReceivedBannerInterface;
import com.smaato.soma.exception.AdReceiveFailed;
import com.smaato.soma.exception.ClosingLandingPageFailed;

// SMAATO SOMA Adapter
public class AdMobMediationAdapter implements CustomEventBanner {
	@Override
	public void requestBannerAd(final Context context,
			final CustomEventBannerListener listener, final String serverParameter,
			final AdSize size,final MediationAdRequest mediationAdRequest,
			final Bundle customEventExtras) {

		final BannerView mBannerView = new BannerView(context);
		int adHeight = size.getHeight();
		int adWidth = size.getWidth();
		String[] serverParams = serverParameter.split("&");
		String publisherId = serverParams[0].split("=")[1];
		String adSpaceId = serverParams[1].split("=")[1];
		AdSettings adSettings = mBannerView.getAdSettings();
		adSettings.setAdspaceId(Integer.parseInt(adSpaceId));
		adSettings.setPublisherId(Integer.parseInt(publisherId));
		adSettings.setBannerHeight(adHeight);
		adSettings.setBannerWidth(adWidth);

		mBannerView.setAdSettings(adSettings);
		mBannerView.addAdListener(new AdListenerInterface() {

			@Override
			public void onReceiveAd(AdDownloaderInterface arg0,
					ReceivedBannerInterface arg1) throws AdReceiveFailed {
				if (arg1.getErrorCode() == ErrorCode.NO_ERROR) {
					if (mBannerView.getParent() != null) {
						((ViewGroup) mBannerView.getParent())
								.removeView(mBannerView);
					}
					
					listener.onAdLoaded(mBannerView);
				} else if (arg1.getErrorCode() == ErrorCode.NO_AD_AVAILABLE) {
					listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
				} else if (arg1.getErrorCode() == ErrorCode.NO_CONNECTION_ERROR) {
					listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
				} else {
					listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
				}
			}
		});
		mBannerView.setBannerStateListener(new BannerStateListener() {

			@Override
			public void onWillOpenLandingPage(BaseView arg0) {
				listener.onAdClicked();
				listener.onAdOpened();
			}

			@Override
			public void onWillCloseLandingPage(BaseView arg0)
					throws ClosingLandingPageFailed {
				listener.onAdClosed();
			}
		});
		mBannerView.asyncLoadNewBanner();
	
	}
	
	@Override
	public void onDestroy() {
		
	}

	@Override
	public void onPause() {
		
	}

	@Override
	public void onResume() {

	}

}
