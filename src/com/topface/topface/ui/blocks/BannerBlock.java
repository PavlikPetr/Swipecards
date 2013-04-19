package com.topface.topface.ui.blocks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
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
import com.adfonic.android.AdListener;
import com.adfonic.android.AdfonicView;
import com.adfonic.android.api.Request;
import com.google.ads.Ad;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.mad.ad.AdStaticView;
import com.mopub.mobileads.MoPubErrorCode;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Banner;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.VirusLike;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.VirusLikesRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.BaseApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.TopsFragment;
import com.topface.topface.ui.fragments.feed.*;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.offerwalls.Offerwalls;
import ru.begun.adlib.Callback;
import ru.begun.adlib.RequestParam;
import ru.ideast.adwired.AWView;
import ru.ideast.adwired.events.OnNoBannerListener;
import ru.ideast.adwired.events.OnStartListener;
import ru.ideast.adwired.events.OnStopListener;
import ru.wapstart.plus1.sdk.Plus1BannerAsker;
import ru.wapstart.plus1.sdk.Plus1BannerDownloadListener;
import ru.wapstart.plus1.sdk.Plus1BannerRequest;
import ru.wapstart.plus1.sdk.Plus1BannerView;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Показываем баннер на нужных страницах
 */
public class BannerBlock {

    public static final String ADFONIC_SLOT_ID = "9f83e583-a247-4b78-94a0-bf2beb8775fc";
    public static final int PLUS1_ID = 7227;
    private static final String BEGUN_KEY = "pad_id:320304962|block_id:320308422";
    public static final String VIRUS_LIKES_BANNER_PARAM = "viruslikes";
    private static final String MOPUB_AD_UNIT_ID = "4ec8274ea73811e295fa123138070049";

    private LayoutInflater mInflater;
    ViewGroup mBannerLayout;
    private Fragment mFragment;
    private View mBannerView;
    private Plus1BannerAsker mPLus1Asker;
    private Map<String, String> mBannersMap = new HashMap<String, String>();

    private Map<String, Character> mAdwiredMap = new HashMap<String, Character>();

    public BannerBlock(Fragment fragment, ViewGroup layout) {
        super();
        mFragment = fragment;
        mInflater = (LayoutInflater) mFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBannerLayout = (ViewGroup) layout.findViewById(R.id.loBannerContainer);
        setBannersMap();
    }

    private void setBannersMap() {
        mBannersMap.put(LikesFragment.class.toString(), Options.PAGE_LIKES);
        mBannersMap.put(MutualFragment.class.toString(), Options.PAGE_MUTUAL);
        mBannersMap.put(DialogsFragment.class.toString(), Options.PAGE_DIALOGS);
        mBannersMap.put(TopsFragment.class.toString(), Options.PAGE_TOP);
        mBannersMap.put(VisitorsFragment.class.toString(), Options.PAGE_VISITORS);
        mBannersMap.put(BookmarksFragment.class.toString(), Options.PAGE_BOOKMARKS);
        mBannersMap.put(FansFragment.class.toString(), Options.PAGE_FANS);

        mAdwiredMap.put(LikesFragment.class.toString(), '1');
        mAdwiredMap.put(MutualFragment.class.toString(), '2');
        mAdwiredMap.put(DialogsFragment.class.toString(), '3');
        mAdwiredMap.put(TopsFragment.class.toString(), '4');
        mAdwiredMap.put(VisitorsFragment.class.toString(), '5');
        mAdwiredMap.put(BookmarksFragment.class.toString(), '6');
        mAdwiredMap.put(FansFragment.class.toString(), '7');
    }

    private void initBanner() {
        if (mFragment != null && mBannersMap != null) {
            String fragmentId = mFragment.getClass().toString();
            Options options = CacheProfile.getOptions();
            if (mBannersMap.containsKey(fragmentId) && options != null && options.pages != null) {
                String bannerType = Options.BANNER_MOPUB;//options.pages.get(mBannersMap.get(fragmentId)).banner;

                mBannerView = getBannerView(bannerType);
                if (mBannerView == null) {
                    return;
                }
                mBannerLayout.addView(mBannerView);
                if (bannerType.equals(Options.BANNER_TOPFACE)) {
                    if (isCorrectResolution() && mBannersMap.containsKey(fragmentId)) {
                        loadBanner(mBannersMap.get(mFragment.getClass().toString()));
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

    private View getBannerView(String bannerType) {
        try {
            if (bannerType.equals(Options.BANNER_TOPFACE) || bannerType.equals(Options.BANNER_GAG)) {
                return mInflater.inflate(R.layout.banner_topface, null);
            } else if (bannerType.equals(Options.BANNER_ADMOB)) {
                return mInflater.inflate(R.layout.banner_admob, null);
            } else if (bannerType.equals(Options.BANNER_ADFONIC)) {
                return mInflater.inflate(R.layout.banner_adfonic, null);
            } else if (bannerType.equals(Options.BANNER_WAPSTART)) {
                return mInflater.inflate(R.layout.banner_wapstart, null);
            } else if (bannerType.equals(Options.BANNER_ADWIRED)) {
                return mInflater.inflate(R.layout.banner_adwired, null);
            } else if (bannerType.equals(Options.BANNER_MADNET)) {
                return mInflater.inflate(R.layout.banner_madnet, null);
            } else if (bannerType.equals(Options.BANNER_BEGUN)) {
                return mInflater.inflate(R.layout.banner_begun, null);
            } else if (bannerType.equals(Options.BANNER_MOPUB)) {
                return mInflater.inflate(R.layout.banner_mopub, null);
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
        } else if (mBannerView instanceof AdfonicView) {
            showAdFonic();
        } else if (mBannerView instanceof Plus1BannerView) {
            showWapStart();
        } else if (mBannerView instanceof AWView) {
            showAdwired();
        } else if (mBannerView instanceof AdStaticView) {
            showMadnet();
        } else if (mBannerView instanceof ru.begun.adlib.AdView) {
            showBegun();
        } else if (mBannerView instanceof MoPubView) {
            showMopub();
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

    private void showBegun() {
        final ru.begun.adlib.AdView adView = ((ru.begun.adlib.AdView) mBannerView);
        adView.setOnApiListener(new Callback() {
            @Override
            public void init() {
                ArrayList<RequestParam> al = new ArrayList<RequestParam>();
                RequestParam rp = new RequestParam();
                rp.name = "environmentVars";
                rp.value = BEGUN_KEY;
                al.add(rp);
                adView.api("initAd", al);
            }

            @Override
            public void callback(String s, String s1) {
                if (s.equals("AdLoaded")) {
                    adView.api("startAd");
                }/* else if (s.equals("AdClickThru")) {

                } else if (s.equals("AdStopped")) {

                }*/
            }
        });
        adView.onDebug = App.DEBUG;
        adView.init();
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
                            params.height = (int) ((deviceWidth / imageWidth) * imageHeight);
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

    private void showMadnet() {
        mBannerView.setVisibility(View.VISIBLE);
        Profile profile = CacheProfile.getProfile();
        com.mad.ad.AdRequest.Builder requestBuilder = new com.mad.ad.AdRequest.Builder();
        requestBuilder.setGender(profile.sex == Static.BOY ?
                com.mad.ad.AdRequest.Gender.MALE : com.mad.ad.AdRequest.Gender.FEMALE);
        requestBuilder.setGenderInterest(profile.dating.sex == Static.BOY ?
                com.mad.ad.AdRequest.GenderInterest.MALE : com.mad.ad.AdRequest.GenderInterest.FEMALE);
        com.mad.ad.AdRequest request = requestBuilder.getRequest();
        ((AdStaticView) mBannerView).showBanners(request);
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

    private void showWapStart() {
        mBannerView.setVisibility(View.VISIBLE);
        mPLus1Asker = new Plus1BannerAsker(new Plus1BannerRequest().setApplicationId(PLUS1_ID),
                ((Plus1BannerView) mBannerView).setAutorefreshEnabled(false));
        mPLus1Asker.setRemoveBannersOnPause(true);
        mPLus1Asker.setDisabledWebViewCorePausing(true);
        mPLus1Asker.setDownloadListener(new Plus1BannerDownloadListener() {
            @Override
            public void onBannerLoaded() {

            }

            @Override
            public void onBannerLoadFailed(LoadError error) {
                requestBannerGag();
            }
        });
    }

    private void showAdFonic() {
        Request request = new Request();
        request.setLanguage(Locale.getDefault().getLanguage());
        request.setSlotId(ADFONIC_SLOT_ID);
        request.setRefreshAd(false);
        AdfonicView adfonicView = (AdfonicView) mBannerView;
        adfonicView.loadAd(request);
        adfonicView.setAdListener(new AdListener() {
            @Override
            public void onReceivedAd() {
                mBannerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPresentScreen() {
            }

            @Override
            public void onLeaveApplication() {
            }

            @Override
            public void onInvalidRequest() {
            }

            @Override
            public void onNetworkError() {
            }

            @Override
            public void onNoFill() {
                requestBannerGag();
            }

            @Override
            public void onInternalError() {
            }

            @Override
            public void onDismissScreen() {
            }

            @Override
            public void onClick() {
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

    // Не работает для MADNET, BEGUN: нет событий, чтобы "подвеситься"
    private void requestBannerGag() {
        removeBanner();
        mBannerView = getBannerView(Options.BANNER_TOPFACE);
        mBannerLayout.addView(mBannerView);
        loadBanner(Options.BANNER_GAG);
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
                                loadBanner(mBannersMap.get(mFragment.getClass().toString()));
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
            if (mBannerView instanceof AdfonicView) {
                mBannerView.invalidate();
            } else if (mBannerView instanceof AWView) {
                ((AWView) mBannerView).request(mAdwiredMap.get(mFragment.getClass().toString()));
            } else if (mBannerView instanceof ru.begun.adlib.AdView) {
                ((ru.begun.adlib.AdView) mBannerView).api("resumeAd");
            }
        }

        if (mPLus1Asker != null) mPLus1Asker.onResume();
    }

    public void onPause() {
    }

    public void onDestroy() {
        if (mPLus1Asker != null) mPLus1Asker.onPause();
        if (mBannerView instanceof MoPubView) ((MoPubView)mBannerView).destroy();
                removeBanner();
    }
}
