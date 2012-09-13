package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.topface.topface.ui.*;
import com.topface.topface.ui.fragments.LikesFragment;
import com.topface.topface.ui.fragments.MutualFragment;
import com.topface.topface.ui.fragments.TopsFragment;
import com.topface.topface.utils.Device;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Показываем баннер на нужных страницах
 */
public class BannerBlock {

    private Activity mActivity;
    private ImageView mBannerView;
    private Map<String, String> mBannersMap = new HashMap<String, String>();

    public BannerBlock(Activity activity) {
        super();
        mActivity = activity;
        mBannerView = (ImageView) mActivity.findViewById(R.id.ivBanner);
        setBannersMap();

        if (isCorrectResolution() &&
                mBannersMap.containsKey(mActivity.getClass().toString())) {
            loadBanner();
        }
    }

    private void setBannersMap() {
        //TODO: fix intents
        mBannersMap.put(LikesFragment.class.toString(), BannerRequest.LIKE);
        mBannersMap.put(MutualFragment.class.toString(), BannerRequest.LIKE);
        mBannersMap.put(ChatActivity.class.toString(), BannerRequest.INBOX);
        mBannersMap.put(TopsFragment.class.toString(), BannerRequest.TOP);
    }

    private void loadBanner() {
        BannerRequest bannerRequest = new BannerRequest(mActivity.getApplicationContext());
        bannerRequest.place = mBannersMap.get(mActivity.getClass().toString());
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
        DefaultImageLoader.getInstance().displayImage(banner.url, mBannerView, new SimpleImageLoadingListener() {
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
}
