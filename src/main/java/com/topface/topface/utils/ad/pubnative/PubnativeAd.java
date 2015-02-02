package com.topface.topface.utils.ad.pubnative;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
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

    private String title;
    private String description;
    private String icon_url;
    private String click_url;
    private Beacon[] beacons;
    private boolean mIsShown;

    @SuppressWarnings("unused")
    public PubnativeAd() {
    }

    protected PubnativeAd(Parcel in) {
        title = in.readString();
        description = in.readString();
        icon_url = in.readString();
        click_url = in.readString();
        Parcelable[] beaconsParcelable = in.readParcelableArray(Beacon.class.getClassLoader());
        beacons = new Beacon[beaconsParcelable.length];
        for (int i = 0; i < beaconsParcelable.length; i++) {
            beacons[i] = (Beacon) beaconsParcelable[i];
        }
        mIsShown = in.readByte() == 1;
    }

    @Override
    public void show(View view) {
        if (view == null) {
            return;
        }

        ImageViewRemote ivr = (ImageViewRemote) view.findViewById(R.id.ifp_avatar_image);
        if (ivr != null) {
            ivr.setRemoteSrc(icon_url);
        }
        TextView title = (TextView) view.findViewById(R.id.ad_title);
        if (title != null) {
            title.setText(this.title);
        }
        TextView description = (TextView) view.findViewById(R.id.ad_description);
        if (description != null) {
            description.setText(this.description);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(click_url));
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

        Debug.log("NativeAd: ad shown\n" + this);
    }

    private void sendImpressionBeacon() {
        for (Beacon beacon : beacons) {
            if (TextUtils.equals(beacon.getType(), Beacon.IMPRESSION)) {
                String response = null;
                for (int i = 0; i < ApiRequest.MAX_RESEND_CNT && response == null; i++) {
                    response = HttpUtils.httpGetRequest(beacon.getUrl());
                }
                if (response != null) {
                    Debug.log("NativeAd: Impression beacon sent for pubnative ad " + title);
                }
                break;
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(icon_url);
        dest.writeString(click_url);
        dest.writeParcelableArray(beacons, flags);
        dest.writeByte((byte) (mIsShown ? 1 : 0));
    }

    @Override
    public int getPosition() {
        return CacheProfile.getOptions().feedNativeAd.getPosition();
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(title);
    }

    @Override
    public String toString() {
        return "{\ntitle: " + title + "\ndescription: " + description + "\nicon_url: " + icon_url + "\n}";
    }
}
