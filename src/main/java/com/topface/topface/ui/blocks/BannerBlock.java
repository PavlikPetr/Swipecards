package com.topface.topface.ui.blocks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Banner;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.feed.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.FansFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.adcamp.ads.AdsManager;
import ru.adcamp.ads.BannerAdView;
import ru.ideast.adwired.AWView;
import ru.ideast.adwired.events.OnNoBannerListener;
import ru.ideast.adwired.events.OnStartListener;
import ru.ideast.adwired.events.OnStopListener;

/**
 * Показываем баннер на нужных страницах
 */
public class BannerBlock {

    private static final String TAG = "BannerBlock";
    /**
     * Идентификаторы типов баннеров
     */
    public final static String BANNER_TOPFACE = "TOPFACE";
    public final static String BANNER_ADMOB = "ADMOB";
    public static final String BANNER_ADWIRED = "ADWIRED";
    public static final String BANNER_ADCAMP = "ADCAMP";
    public static final String BANNER_GAG = "GAG";
    public static final String BANNER_NONE = "NONE";
    public final static String[] BANNERS = new String[]{
            BANNER_TOPFACE,
            BANNER_ADMOB,
            BANNER_ADWIRED,
            BANNER_ADCAMP,
            BANNER_GAG,
            BANNER_NONE
    };

    private LayoutInflater mInflater;
    ViewGroup mBannerLayout;
    private Fragment mFragment;
    private View mBannerView;
    private static boolean mAdcampInitialized = false;

    private Map<String, Character> mAdwiredMap = new HashMap<>();

    public BannerBlock(Fragment fragment, ViewGroup layout) {
        super();
        mFragment = fragment;
        mInflater = (LayoutInflater) mFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBannerLayout = (ViewGroup) layout.findViewById(R.id.loBannerContainer);
        setBannersMap();
    }

    public static void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            if (CacheProfile.getOptions().containsBannerType(BANNER_ADCAMP)) {
                Context context = App.getContext();
                AdsManager.getInstance().initialize(
                        context,
                        context.getString(R.string.adcamp_app_id),
                        context.getString(R.string.adcamp_app_secret),
                        context.getResources().getBoolean(R.bool.adcamp_logging_enabled),
                        Log.VERBOSE
                );
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
        Debug.log("BannersBlock: init banner");
        Map<String, Options.Page> bannersMap = FloatBlock.getActivityMap();
        if (mFragment != null && bannersMap != null) {
            String fragmentId = ((Object) mFragment).getClass().toString();
            Options options = CacheProfile.getOptions();
            if (bannersMap.containsKey(fragmentId) && options != null && options.pages != null) {
                if (bannersMap.get(fragmentId) != null) {
                    String bannerType = bannersMap.get(fragmentId).banner;
                    Debug.log(TAG, bannerType);

                    //AdCamp uses only FROYO and above
                    if (bannerType.equals(BANNER_ADCAMP)) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO || !mAdcampInitialized) {
                            requestBannerGag();
                            return;
                        }
                    }

                    mBannerView = getBannerView(bannerType);
                    if (mBannerView == null) return;
                    mBannerLayout.addView(mBannerView);
                    if (bannerType.equals(BANNER_TOPFACE)) {
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

    @SuppressLint("InflateParams")
    private View getBannerView(String bannerType) {
        try {
            switch (bannerType) {
                case BANNER_TOPFACE:
                case BANNER_GAG:
                    return mInflater.inflate(R.layout.banner_topface, mBannerLayout, false);
                case BANNER_ADMOB:
                    return mInflater.inflate(R.layout.banner_admob, mBannerLayout, false);
                case BANNER_ADWIRED:
                    return mInflater.inflate(R.layout.banner_adwired, mBannerLayout, false);
                case BANNER_ADCAMP:
                    return mInflater.inflate(R.layout.banner_adcamp, mBannerLayout, false);
                default:
                    return null;
            }
        } catch (Exception e) {
            Debug.error(e);
            return null;
        }
    }

    private void loadBanner(String bannerPlace) {
        Debug.log("BannersBlock: load banner");
        BannerRequest bannerRequest = new BannerRequest(mFragment.getActivity());
        bannerRequest.place = bannerPlace;
        if (mFragment instanceof BaseFragment) {
            ((BaseFragment) mFragment).registerRequest(bannerRequest);
        }
        bannerRequest.callback(new DataApiHandler<Banner>() {

            @Override
            protected void success(Banner banner, IApiResponse response) {
                if (mBannerView != null) {
                    try {
                        showBanner(banner);
                    } catch (Exception e) {
                        Debug.error(e);
                    }
                }
            }

            @Override
            protected Banner parseResponse(ApiResponse response) {
                return new Banner(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
            }
        }).exec();
    }

    private void showBanner(final Banner banner) throws Exception {
        Debug.log("BannersBlock: try show banner " + mBannerView);
        if (mBannerView instanceof AdView) {
            showAdMob();
        } else if (mBannerView instanceof AWView) {
            showAdwired();
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
                                maxHeight = ((ImageViewRemote) mBannerView).getImageMaxHeight();
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
                switch (banner.action) {
                    case Banner.ACTION_PAGE:
                        EasyTracker.sendEvent("Purchase", "Banner", "", 0L);
                        intent = new Intent(mFragment.getActivity(), PurchasesActivity.class);
                        if (banner.parameter.equals("VIP")) {
                            intent.putExtra(Static.INTENT_REQUEST_KEY, PurchasesActivity.INTENT_BUY_VIP);
                        } else {
                            intent.putExtra(Static.INTENT_REQUEST_KEY, PurchasesActivity.INTENT_BUY);
                        }
                        intent.putExtra(OpenIabFragment.ARG_TAG_SOURCE, "Banner_" + banner.name);
                        break;
                    case Banner.ACTION_URL:
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                        break;
                    case Banner.ACTION_OFFERWALL:
                        switch (banner.parameter) {
                            case OfferwallsManager.SPONSORPAY:
                                OfferwallsManager.startSponsorpay(mFragment.getActivity());
                                break;
                            default:
                                OfferwallsManager.startOfferwall(mFragment.getActivity());
                                break;
                        }
                        break;
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

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                requestBannerGag();
            }

        });
        //Считаем год рождения юзера
        Calendar rightNow = Calendar.getInstance();
        int year = rightNow.get(Calendar.YEAR);
        rightNow.set(Calendar.YEAR, year - CacheProfile.getProfile().age);

        AdRequest.Builder adRequest = new AdRequest.Builder()
                .setGender(
                        CacheProfile.getProfile().sex == Static.BOY ?
                                AdRequest.GENDER_MALE :
                                AdRequest.GENDER_FEMALE
                )
                .setBirthday(rightNow.getTime());
        /*
        //Если нужно, то можно указать id девайса (например эмулятор) для запроса тестовой рекламы
        adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        //или id свего девайса
        adRequest.addTestDevice("hex id твоего девайса");
        */
        ((AdView) mBannerView).loadAd(adRequest.build());
    }

    private void showAdcamp() {
        ((BannerAdView) mBannerView).setBannerAdViewListener(new BannerAdView.BannerAdViewListener() {
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

        ((BannerAdView) mBannerView).showAd();
    }

    private void requestBannerGag() {
        // TODO: Запрос заглушки ведёт к закликиванию запроса банера https://tasks.verumnets.ru/issues/34334
        Debug.log("BannersBlock: request banner gag");
        removeBanner();
        /*String bannerType = CacheProfile.getOptions().gagTypeBanner;
        mBannerView = getBannerView(bannerType);
        if (mBannerView != null) {
            mBannerLayout.addView(mBannerView);
            if (bannerType.equals(BANNER_TOPFACE)) {
                loadBanner(BANNER_GAG);
            } else {
                try {
                    showBanner(null);
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        }*/
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
        EasyTracker.sendEvent("Banner", action, label, TextUtils.equals(label, "click") ? 1L : 0L);
    }

    private String getBannerName(String bannerUrl) {
        String name = null;
        Pattern pattern = Pattern.compile(".*/(.*)\\..+$");
        Matcher matcher = pattern.matcher(bannerUrl);
        if (matcher.find() && matcher.matches()) {
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
                ((AWView) mBannerView).request(mAdwiredMap.get(((Object) mFragment).getClass().toString()));
            }
        }
    }

    public void onPause() {
    }

    public void onDestroy() {
        removeBanner();
    }

    public void onResume() {

    }
}
