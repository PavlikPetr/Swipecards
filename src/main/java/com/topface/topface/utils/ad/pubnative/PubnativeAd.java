package com.topface.topface.utils.ad.pubnative;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.annotations.SerializedName;
import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.ad.NativeAd;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.http.HttpUtils;

/**
 * Pubnative ad data
 */
public class PubnativeAd extends NativeAd {

    public static final Creator<PubnativeAd> CREATOR = new Creator<PubnativeAd>() {
        @Override
        public PubnativeAd createFromParcel(Parcel source) {
            return new PubnativeAd(source);
        }

        @Override
        public PubnativeAd[] newArray(int size) {
            return new PubnativeAd[size];
        }
    };

    @SerializedName("title")
    private String mTitle;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("icon_url")
    private String mIconUrl;
    @SerializedName("click_url")
    private String mClickUrl;
    @SerializedName("beacons")
    private Beacon[] mBeacons;
    private boolean mIsShown;

    public PubnativeAd() {
    }

    protected PubnativeAd(Parcel in) {
        mTitle = in.readString();
        mDescription = in.readString();
        mIconUrl = in.readString();
        mClickUrl = in.readString();
        mIsShown = in.readByte() == 1;
    }

    @Override
    public void show(View view) {
        if (view == null) {
            return;
        }

        ImageViewRemote ivr = (ImageViewRemote) view.findViewById(R.id.ivIcon);
        if (ivr != null) {
            ivr.setRemoteSrc(mIconUrl);
        }
        TextView title = (TextView) view.findViewById(R.id.tvTitle);
        if (title != null) {
            title.setText(mDescription);
        }
        TextView description = (TextView) view.findViewById(R.id.tvDescription);
        if (description != null) {
            description.setText(mDescription);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mClickUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getContext().startActivity(intent);
            }
        });

        if (!mIsShown) {
            UserConfig userConfig = App.getUserConfig();
            userConfig.decreaseRemainedPubnativeShows();
            userConfig.saveConfig();

            if (!BuildConfig.DEBUG) {
                new BackgroundThread() {
                    @Override
                    public void execute() {
                        sendImpressionBeacon();
                    }
                };
            }

            mIsShown = true;
        }
    }

    private void sendImpressionBeacon() {
        for (Beacon beacon : mBeacons) {
            if (TextUtils.equals(beacon.getType(), Beacon.IMPRESSION)) {
                String response = null;
                for (int i = 0; i < ApiRequest.MAX_RESEND_CNT && response == null; i++) {
                    response = HttpUtils.httpGetRequest(beacon.getUrl());
                }
                break;
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeString(mIconUrl);
        dest.writeString(mClickUrl);
        dest.writeByte((byte) (mIsShown ? 1 : 0));
    }
}
