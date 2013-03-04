package com.topface.topface.ui.blocks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.mad.ad.AdStaticView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Banner;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.VirusLike;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.VirusLikesRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.BaseApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.TopsFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.Utils;
import ru.ideast.adwired.AWView;
import ru.ideast.adwired.events.OnNoBannerListener;
import ru.ideast.adwired.events.OnStartListener;
import ru.ideast.adwired.events.OnStopListener;
import ru.wapstart.plus1.sdk.Plus1BannerAsker;
import ru.wapstart.plus1.sdk.Plus1BannerRequest;
import ru.wapstart.plus1.sdk.Plus1BannerView;

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
    public static final String VIRUS_LIKES_BANNER_PARAM = "viruslikes";

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

        mAdwiredMap.put(LikesFragment.class.toString(), '1');
        mAdwiredMap.put(MutualFragment.class.toString(), '2');
        mAdwiredMap.put(DialogsFragment.class.toString(), '3');
        mAdwiredMap.put(TopsFragment.class.toString(), '4');
        mAdwiredMap.put(VisitorsFragment.class.toString(), '5');
    }

    private void initBanner() {
        if (mFragment != null && mBannersMap != null) {
            String fragmentId = mFragment.getClass().toString();
            if (mBannersMap.containsKey(fragmentId)) {
//                String bannerType = CacheProfile.getOptions().pages.get(mBannersMap.get(fragmentId)).banner;
                String bannerType = Options.BANNER_ADWIRED;

                mBannerView = getBannerView(bannerType);
                if (mBannerView == null) {
                    return;
                }
                mBannerLayout.addView(mBannerView);
                if (bannerType.equals(Options.BANNER_TOPFACE)) {
                    if (isCorrectResolution() && mBannersMap.containsKey(fragmentId)) {
                        loadBanner();
                    }
                } else {
                    showBanner(null);
                }
            }
        }
    }

    private void removeBanner() {
        unbindDrawables(mBannerLayout);
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

    private View getBannerView(String bannerType) {
        if (bannerType.equals(Options.BANNER_TOPFACE)) {
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
        } else {
            return null;
        }
    }

    private void loadBanner() {
        BannerRequest bannerRequest = new BannerRequest(mFragment.getActivity().getApplicationContext());
        bannerRequest.place = mBannersMap.get(mFragment.getClass().toString());

        if (mFragment instanceof BaseFragment) {
            ((BaseFragment) mFragment).registerRequest(bannerRequest);
        }

        bannerRequest.callback(new BaseApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final Banner banner = Banner.parse(response);
                if (mBannerView != null) {
                    showBanner(banner);
                }
            }
        }).exec();
    }

    private void showBanner(final Banner banner) {
        if (mBannerView instanceof AdView) {
            mBannerView.setVisibility(View.VISIBLE);
            ((AdView) mBannerView).loadAd(new AdRequest());
        } else if (mBannerView instanceof AdfonicView) {
            Request request = new Request();
            request.setLanguage(Locale.getDefault().getLanguage());
            request.setSlotId(ADFONIC_SLOT_ID);
            request.setRefreshAd(false);
            ((AdfonicView) mBannerView).loadAd(request);
            ((AdfonicView) mBannerView).setAdListener(new AdListener() {
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
        } else if (mBannerView instanceof Plus1BannerView) {
            mBannerView.setVisibility(View.VISIBLE);
            mPLus1Asker = new Plus1BannerAsker(new Plus1BannerRequest().setApplicationId(PLUS1_ID),
                    ((Plus1BannerView) mBannerView).setAutorefreshEnabled(false));
            mPLus1Asker.setRemoveBannersOnPause(true);
            mPLus1Asker.setDisabledWebViewCorePausing(true);
        } else if (mBannerView instanceof AWView) {
            // request onResume
            ((AWView)mBannerView).setOnStartListener(new OnStartListener() {
                @Override
                public void onStart() {
                    Debug.log("Adwired: Start");
                }
            });

            ((AWView)mBannerView).setOnStopListener(new OnStopListener() {
                @Override
                public void onStop() {
                    Debug.log("Adwired: Start");
                }
            });

            ((AWView)mBannerView).setOnNoBannerListener(new OnNoBannerListener() {
                @Override
                public void onNoBanner() {
                    Debug.log("Adwired: No banner");
                }
            });
        } else if (mBannerView instanceof AdStaticView) {
            mBannerView.setVisibility(View.VISIBLE);
            Profile profile = CacheProfile.getProfile();
            com.mad.ad.AdRequest.Builder requestBuilder = new com.mad.ad.AdRequest.Builder();
//            requestBuilder.setEducation(com.mad.ad.AdRequest.Education.UNIVERSITY);
//            requestBuilder.setEthnicity(com.mad.ad.AdRequest.Ethnicity.WHITE);
            requestBuilder.setGender(profile.sex == Static.BOY ?
                    com.mad.ad.AdRequest.Gender.MALE : com.mad.ad.AdRequest.Gender.FEMALE);
            requestBuilder.setGenderInterest(profile.dating.sex == Static.BOY ?
                    com.mad.ad.AdRequest.GenderInterest.MALE : com.mad.ad.AdRequest.GenderInterest.FEMALE);
            //requestBuilder.setIncome(com.mad.ad.AdRequest.Income.FROM_15_TO_25);
            //requestBuilder.setMaritalStatus(com.mad.ad.AdRequest.MaritalStatus.MARRIED);

//            requestBuilder.setGps(location);
//            requestBuilder.setRegion(region);
//            requestBuilder.setCity(city);
//            requestBuilder.setZip(zip);
//            requestBuilder.setAreaCode(LAC);

//            requestBuilder.setPrimaryCategory("cars");
//            requestBuilder.setSecodaryCategory("sportcars");
//            requestBuilder.addKeyword("Ferrari");

            com.mad.ad.AdRequest request = requestBuilder.getRequest();
            ((AdStaticView)mBannerView).showBanners(request);
        } else if (mBannerView instanceof ImageView) {
            //Это нужно, что бы сбросить размеры баннера, для правильного расчета размера в ImageLoader
            ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mBannerView.setLayoutParams(params);
            //Убираем старый баннер
            ((ImageView) mBannerView).setImageDrawable(null);

            DefaultImageLoader.getInstance().displayImage(banner.url, (ImageView) mBannerView, new SimpleImageLoadingListener() {

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);

                    if (mBannerView != null) {
                        float deviceWidth = Device.getDisplayMetrics(mFragment.getActivity()).widthPixels;
                        float imageWidth = loadedImage.getWidth();
                        //Если ширина экрана больше, чем у нашего баннера, то пропорционально увеличиваем высоту imageView
                        if (deviceWidth > imageWidth) {
                            ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
                            params.height = (int) ((deviceWidth / imageWidth) * (float) loadedImage.getHeight());
                            mBannerView.setLayoutParams(params);
                            mBannerView.invalidate();
                        }
                    }
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    super.onLoadingFailed(imageUri, view, failReason);
                    Debug.log("LOAD FAILED::" + failReason.toString());
                }

            });
            sendStat(getBannerName(banner.url), "view");
            mBannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    if (banner.action.equals(Banner.ACTION_PAGE)) {
                        EasyTracker.getTracker().trackEvent("Purchase", "Banner", "", 0L);
                        intent = new Intent(mFragment.getActivity().getApplicationContext(), ContainerActivity.class);
                        if (banner.parameter.equals("VIP")) {
                            intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                        } else {
                            intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                        }
                    } else if (banner.action.equals(Banner.ACTION_URL)) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                    } else if (banner.action.equals(Banner.ACTION_METHOD)) {
                        invokeBannerMethod(banner.parameter);
                    }
                    sendStat(getBannerName(banner.url), "click");
                    if (intent != null) {
                        mFragment.startActivity(intent);
                    }
                }
            });
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
                                loadBanner();
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
                dialog.dismiss();
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

    public void onResume() {
        initBanner();
        if (mBannerView != null && mBannerView instanceof AdfonicView) mBannerView.invalidate();
        if (mBannerView != null && mBannerView instanceof AWView) {
            ((AWView)mBannerView).request('1');
        }
        if (mPLus1Asker != null) mPLus1Asker.onResume();
    }

    public void onPause() {
        if (mPLus1Asker != null) mPLus1Asker.onPause();
        removeBanner();
    }

}
