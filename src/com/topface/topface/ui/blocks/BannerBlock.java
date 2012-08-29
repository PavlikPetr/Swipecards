package com.topface.topface.ui.blocks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Banner;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.BaseApiHandler;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.LikesActivity;
import com.topface.topface.ui.SymphatyActivity;
import com.topface.topface.ui.TopsActivity;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.Http;

import java.util.HashMap;
import java.util.Map;

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
        mBannersMap.put(LikesActivity.class.toString(), BannerRequest.LIKE);
        mBannersMap.put(SymphatyActivity.class.toString(), BannerRequest.LIKE);
        mBannersMap.put(ChatActivity.class.toString(), BannerRequest.MESSAGES);
        mBannersMap.put(TopsActivity.class.toString(), BannerRequest.TOP);
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
        Http.bannerLoader(banner.url, mBannerView);
        mBannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (banner.action.equals(Banner.ACTION_PAGE))
                    intent = new Intent(mActivity, BuyingActivity.class); // "parameter":"PURCHASE"
                else if (banner.action.equals(Banner.ACTION_URL)) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                }
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

}
