package com.topface.topface.ui.blocks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.ads.Ad;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Banner;
import com.topface.topface.data.Options;
import com.topface.topface.data.VirusLike;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.VirusLikesRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.BaseApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.feed.*;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.offerwalls.Offerwalls;
import ru.adcamp.ads.AdsManager;
import ru.adcamp.ads.BannerAdView;
import ru.ideast.adwired.AWView;
import ru.ideast.adwired.events.OnNoBannerListener;
import ru.ideast.adwired.events.OnStartListener;
import ru.ideast.adwired.events.OnStopListener;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Показываем баннер на нужных страницах
 */
public class BannerBlock {

    public static final String VIRUS_LIKES_BANNER_PARAM = "viruslikes";
    private static final String MOPUB_AD_UNIT_ID = "4ec8274ea73811e295fa123138070049";

    private LayoutInflater mInflater;
    ViewGroup mBannerLayout;
    private Fragment mFragment;
    private View mBannerView;
    private static boolean mAdcampInitialized = false;

    private Map<String, Character> mAdwiredMap = new HashMap<String, Character>();

    public BannerBlock(Fragment fragment, ViewGroup layout) {
        super();
        mFragment = fragment;
        mInflater = (LayoutInflater) mFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBannerLayout = (ViewGroup) layout.findViewById(R.id.loBannerContainer);
        setBannersMap();
    }

    public static void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            if (CacheProfile.getOptions().containsBannerType(Options.BANNER_ADCAMP)) {
                AdsManager.getInstance().initialize(App.getContext());
                AdsManager.getInstance().addTestDevice("99000200906025");
                mAdcampInitialized = true;
            }
        }
    }

    private void setBannersMap() {
        mAdwiredMap.put(LikesFragment.class.toString(), '1');
        mAdwiredMap.put(MutualFragment.class.toString(), '2');
        mAdwiredMap.put(DialogsFragment.class.toString(), '3');
        mAdwiredMap.put(VisitorsFragment.class.toString(), '5');
        mAdwiredMap.put(BookmarksFragment.class.toString(), '6');
        mAdwiredMap.put(FansFragment.class.toString(), '7');
    }

    private void initBanner() {
        Map<String, Options.Page> bannersMap = FloatBlock.getActivityMap();
        if (mFragment != null && bannersMap != null) {
            String fragmentId = mFragment.getClass().toString();
            Options options = CacheProfile.getOptions();
            if (bannersMap.containsKey(fragmentId) && options != null && options.pages != null) {
                if (bannersMap.get(fragmentId) != null) {
                    String bannerType = bannersMap.get(fragmentId).banner;

                    //AdCamp uses only FROYO and above
                    if (bannerType.equals(Options.BANNER_ADCAMP)) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO || !mAdcampInitialized) {
                            requestBannerGag();
                            return;
                        }
                    }

                    mBannerView = getBannerView(bannerType);
                    if (mBannerView == null) return;
                    mBannerLayout.addView(mBannerView);
                    if (bannerType.equals(Options.BANNER_TOPFACE)) {
                        if (isCorrectResolution() && bannersMap.containsKey(fragmentId)) {
                            loadBanner(bannersMap.get(fragmentId).name);
                        }
                    } else {
                        try {
                            showBanner(null);
                        } catch (Exception e) {
                            Debug.error(e);
                        }
                    }
                }
            }
        }
    }

    private View getBannerView(String bannerType) {
        try {
            if (bannerType.equals(Options.BANNER_TOPFACE) || bannerType.equals(Options.BANNER_GAG)) {
                return mInflater.inflate(R.layout.banner_topface, mBannerLayout, false);
            } else if (bannerType.equals(Options.BANNER_ADMOB)) {
                return mInflater.inflate(R.layout.banner_admob, mBannerLayout, false);
            } else if (bannerType.equals(Options.BANNER_ADWIRED)) {
                return mInflater.inflate(R.layout.banner_adwired, mBannerLayout, false);
            } else if (bannerType.equals(Options.BANNER_MOPUB)) {
                return mInflater.inflate(R.layout.banner_mopub, mBannerLayout, false);
            } else if (bannerType.equals(Options.BANNER_ADCAMP)) {
                return mInflater.inflate(R.layout.banner_adcamp, mBannerLayout, false);
            } else {
                return null;
            }
        } catch (Exception e) {
            Debug.error(e);
            return null;
        }
    }

    private void loadBanner(String bannerPlace) {
        BannerRequest bannerRequest = new BannerRequest(mFragment.getActivity());
        bannerRequest.place = bannerPlace;
        if (mFragment instanceof BaseFragment) {
            ((BaseFragment) mFragment).registerRequest(bannerRequest);
        }
        bannerRequest.callback(new BaseApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final Banner banner = Banner.parse(response);

                if (mBannerView != null) {
                    try {
                        showBanner(banner);
                    } catch (Exception e) {
                        Debug.error(e);
                    }
                }
            }
        }).exec();
    }

    private void showBanner(final Banner banner) throws Exception {
        if (mBannerView instanceof AdView) {
            showAdMob();
        } else if (mBannerView instanceof AWView) {
            showAdwired();
        } else if (mBannerView instanceof MoPubView) {
            showMopub();
        } else if (mBannerView instanceof BannerAdView) {
            showAdcamp();
        } else if (mBannerView instanceof ImageView) {
            if (banner == null) {
                requestBannerGag();
            } else {
                showTopface(banner);
            }
        }
    }

    private void showMopub() {
        MoPubView adView = (MoPubView) mBannerView;
        adView.setAdUnitId(MOPUB_AD_UNIT_ID);
        adView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                requestBannerGag();
            }

            @Override
            public void onBannerClicked(MoPubView banner) {
            }

            @Override
            public void onBannerExpanded(MoPubView banner) {
            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {
            }
        });
        adView.loadAd();
    }

    private void showTopface(final Banner banner) {
        //Это нужно, что бы сбросить размеры баннера, для правильного расчета размера в ImageLoader
        ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mBannerView.setLayoutParams(params);

        //Убираем старый баннер
        ((ImageViewRemote) mBannerView).setImageDrawable(null);
        ((ImageViewRemote) mBannerView).setRemoteSrc(banner.url, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == ImageViewRemote.LOADING_COMPLETE && mFragment.isAdded()) {
                    if (mBannerView != null) {
                        float imageWidth = msg.arg1;
                        float imageHeight = msg.arg2;
                        float deviceWidth = Device.getDisplayMetrics(App.getContext()).widthPixels;
                        //Если ширина экрана больше, чем у нашего баннера, то пропорционально увеличиваем высоту imageView
                        if (deviceWidth > imageWidth) {
                            ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
                            int maxHeight = 0;
                            if (mBannerView instanceof ImageViewRemote) {
                                maxHeight = ((ImageViewRemote) mBannerView).getMaxHeight();
                            }
                            int scaledHeight = (int) ((deviceWidth / imageWidth) * imageHeight);
                            if (maxHeight > scaledHeight) {
                                params.height = scaledHeight;
                            } else {
                                params.height = maxHeight;
                                params.width = (int) ((maxHeight * imageWidth) / imageHeight);
                            }
                            mBannerView.setLayoutParams(params);
                            mBannerView.invalidate();
                        }
                    }
                }
            }
        });

        sendStat(getBannerName(banner.url), "view");
        mBannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (banner.action.equals(Banner.ACTION_PAGE)) {
                    EasyTracker.getTracker().trackEvent("Purchase", "Banner", "", 0L);
                    intent = new Intent(mFragment.getActivity(), ContainerActivity.class);
                    if (banner.parameter.equals("VIP")) {
                        intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                    } else {
                        intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                    }
                    intent.putExtra(BillingFragment.ARG_TAG_SOURCE, "Banner_" + banner.name);
                } else if (banner.action.equals(Banner.ACTION_URL)) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                } else if (banner.action.equals(Banner.ACTION_METHOD)) {
                    invokeBannerMethod(banner.parameter);
                } else if (banner.action.equals(Banner.ACTION_OFFERWALL)) {
                    if (banner.parameter.equals(Options.TAPJOY)) {
                        Offerwalls.startTapjoy();
                    } else if (banner.parameter.equals(Options.SPONSORPAY)) {
                        Offerwalls.startSponsorpay(mFragment.getActivity());
                    } else {
                        Offerwalls.startOfferwall(mFragment.getActivity());
                    }
                }

                sendStat(getBannerName(banner.url), "click");
                if (intent != null) {
                    mFragment.startActivity(intent);
                }
            }
        });
    }


    private void showAdwired() {
        // request onResume
        ((AWView) mBannerView).setOnStartListener(new OnStartListener() {
            @Override
            public void onStart() {
                Debug.log("Adwired: Start");
            }
        });
        ((AWView) mBannerView).setOnStopListener(new OnStopListener() {
            @Override
            public void onStop() {
                Debug.log("Adwired: Stop");
            }
        });
        ((AWView) mBannerView).setOnNoBannerListener(new OnNoBannerListener() {
            @Override
            public void onNoBanner() {
                Debug.log("Adwired: No banner");
                requestBannerGag();
            }
        });
    }

    private void showAdMob() {
        mBannerView.setVisibility(View.VISIBLE);
        AdView adView = (AdView) mBannerView;
        adView.setAdListener(new com.google.ads.AdListener() {
            @Override
            public void onReceiveAd(Ad ad) {
            }

            @Override
            public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode errorCode) {
                requestBannerGag();
            }

            @Override
            public void onPresentScreen(Ad ad) {
            }

            @Override
            public void onDismissScreen(Ad ad) {
            }

            @Override
            public void onLeaveApplication(Ad ad) {
            }
        });
        ((AdView) mBannerView).loadAd(new AdRequest());
    }

    private void showAdcamp() {
        ((BannerAdView)mBannerView).setBannerAdViewListener(new BannerAdView.BannerAdViewListener() {
            @Override
            public void onLoadingStarted(BannerAdView bannerAdView) {
            }

            @Override
            public void onLoadingFailed(BannerAdView bannerAdView, String s) {
                requestBannerGag();
            }

            @Override
            public void onBannerDisplayed(BannerAdView bannerAdView) {
//                ViewGroup.LayoutParams params = bannerAdView.getLayoutParams();
//                params.height = Utils.getPxFromDp(50);
//                params.width = Device.getDisplayMetrics(App.getContext()).widthPixels;
//                bannerAdView.setLayoutParams(params);
            }

            @Override
            public void onBannerClicked(BannerAdView bannerAdView, String s) {
            }
        });

        ((BannerAdView)mBannerView).showAd();
    }

    private void requestBannerGag() {
        removeBanner();
        String bannerType = CacheProfile.getOptions().gagType;
        mBannerView = getBannerView(bannerType);
        mBannerLayout.addView(mBannerView);
        if (bannerType.equals(Options.BANNER_TOPFACE)) {
            loadBanner(Options.BANNER_GAG);
        } else {
            try {
                showBanner(null);
            } catch (Exception e) {
                Debug.error(e);
            }
        }
    }

    private void invokeBannerMethod(String param) {
        if (TextUtils.equals(param, VIRUS_LIKES_BANNER_PARAM)) {
            sendVirusLikeRequest();
        }
    }

    private void sendVirusLikeRequest() {
        final ProgressDialog dialog = new ProgressDialog(mFragment.getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(mFragment.getString(R.string.general_dialog_loading));
        dialog.show();

        EasyTracker.getTracker().trackEvent("VirusLike", "Click", "Banner", 0L);

        new VirusLikesRequest(mFragment.getActivity()).callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                EasyTracker.getTracker().trackEvent("VirusLike", "Success", "Banner", 0L);
                //И предлагаем отправить пользователю запрос своим друзьям не из приложения
                new VirusLike(response).sendFacebookRequest(
                        "Banner",
                        mFragment.getActivity(),
                        new VirusLike.VirusLikeDialogListener(mFragment.getActivity()) {
                            @Override
                            public void onComplete(Bundle values) {
                                super.onComplete(values);
                                loadBanner(FloatBlock.getActivityMap().get(mFragment.getClass().toString()).name);
                            }
                        }
                );
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                EasyTracker.getTracker().trackEvent("VirusLike", "Fail", "Banner", 0L);

                if (response.isCodeEqual(ApiResponse.CODE_VIRUS_LIKES_ALREADY_RECEIVED)) {
                    Toast.makeText(getContext(), R.string.virus_error, Toast.LENGTH_LONG).show();
                } else {
                    Utils.showErrorMessage(getContext());
                }
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        }).exec();
    }

    /**
     * Показываем баннер на всех устройствах, кроме устройств с маленьким экраном
     */
    private boolean isCorrectResolution() {
        int screenSize = (mFragment.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK);
        return screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    private void sendStat(String action, String label) {
        action = action == null ? "" : action;
        label = label == null ? "" : label;
        EasyTracker.getTracker().trackEvent("Banner", action, label, TextUtils.equals(label, "click") ? 1L : 0L);
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

    private void removeBanner() {
        try {
            unbindDrawables(mBannerLayout);
            mBannerLayout.removeView(mBannerView);
        } catch (Exception ex) {
            Debug.error(ex);
        }
        mBannerView = null;
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        }
    }

    public void onCreate() {
        initBanner();
        if (mBannerView != null) {
            if (mBannerView instanceof AWView) {
                ((AWView) mBannerView).request(mAdwiredMap.get(mFragment.getClass().toString()));
            }
        }
    }

    public void onPause() {
    }

    public void onDestroy() {
        if (mBannerView instanceof MoPubView) ((MoPubView) mBannerView).destroy();
        removeBanner();
    }

    public void onResume() {
    }
}
