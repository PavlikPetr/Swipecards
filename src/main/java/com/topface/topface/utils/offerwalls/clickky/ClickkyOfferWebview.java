package com.topface.topface.utils.offerwalls.clickky;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ClickkyOfferWebview extends WebView implements LocationListener {
    final static String mUrl = "http://www.cpactions.com/api/app/offerwall.view";
    private final static String ANDROID = "android";
    private final static String SDK = "android.v1";
    private final static int FALSE = 0;
    private final static int TRUE = 1;

    private Timer mTimer;
    private LocationManager myMngr;
    private String mCountryCodeTel;
    private String mCountryCodeGeo;
    private Context mContext;

    private String mWebViewWidth;
    private String mWebViewHeight;
    private Location loc;
    private boolean lastLoc = false;
    private Double myLat, myLong;
    private String mCountry;
    private GeoPoint mPoint;
    private boolean state;

    //for tracking
    private byte[] trackerBytes;
    public static int responseCode;
    public static String response;
    private static int mSiteId;
    private static String mApiKey;
    private static String mUserId;
    private static String mSubId;
    private static boolean debug;


    @SuppressLint("SetJavaScriptEnabled")
    public ClickkyOfferWebview(Context context, int site_id, String api_key, String userId) {
        super(context);

        mContext = context;
        // for tracking
        mSiteId = site_id;
        mApiKey = api_key;
        mUserId = userId;
        mSubId = "";
        debug = false;
        responseCode = Integer.MIN_VALUE;
        response = "";

        WebSettings webSettings = this.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        myMngr = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
        myMngr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        // getting last known location
        loc = getLastCoord();

        state = false; // state for getting geo position once

        if (loc != null) {
            lastLoc = true;
            geoCoding();
            return;
        }

        // Get location via Sim Card
        TelephonyManager tm = (TelephonyManager) this.getContext().getSystemService(Activity.TELEPHONY_SERVICE);
        if ((tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE)
                && (loc == null)) {
            mCountryCodeTel = tm.getNetworkCountryIso();
            Log.d("CLICKKY:Telephony Manager", mCountryCodeTel);
            getSiteLink();
            return;
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                getSiteLink();
                mTimer.cancel();
            }
        }, 0, 5000);

    }

    ;

    public Location getLastCoord() {
        String[] providers = new String[]{LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER};
        Location loc = null;
        for (String provider : providers) {
            loc = myMngr.getLastKnownLocation(provider);
            if (loc != null)
                break;
        }
        return loc;
    }

    public void onLocationChanged(Location location) {
        if (!state) {
            myLat = location.getLatitude();
            myLong = location.getLongitude();
            mPoint = new GeoPoint((int) (Double.parseDouble(String
                    .valueOf(myLat)) * 1E6), ((int) (Double.parseDouble(String
                    .valueOf(myLong)) * 1E6)));

            geoCoding();
            state = true;
        }

    }

    public void geoCoding() { // GeoCoder converting GeoPoint into String
        Double mLat, mLong;
        if (!lastLoc) { // and getting country ISO code
            mLat = myLat;
            mLong = myLong;
        } else {
            mLat = loc.getLatitude();
            mLong = loc.getLongitude();
        }
        if ((mLat != null) && (mLong != null)) {
            Geocoder geoCoder = new Geocoder(this.getContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geoCoder.getFromLocation(mLat, mLong, 1);

                if (addresses.size() > 0) {
                    Address add = addresses.get(0);
                    mCountryCodeGeo = add.getCountryCode();
                    Log.d("CLICKKY:Country Name", mCountryCodeGeo);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("CLICKKY:IOException", e.toString());
            }
        }
        getSiteLink();
    }

    public void getSiteLink() {

        mWebViewWidth = String.valueOf(this.getWidth());
        mWebViewHeight = String.valueOf(this.getHeight());

        try {
            trackerBytes = trackEvent("offerwall").toString().getBytes();
            System.out.println(trackEvent("offerwall").toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
        }
        this.postUrl(mUrl, trackerBytes);
    }

    public JSONObject trackEvent(String name) throws JSONException {

        //md5(event={event}imei={imei}mac={mac}site_id={site_id}api_key={api_key})
        String hash = md5("event=" + name + ""
                + "imei=" + getImei(mContext) + ""
                + "mac=" + getMacAddress() + ""
                + "site_id=" + mSiteId + ""
                + "api_key=" + mApiKey + "");

        Log.d("CLICKKY:HASH", hash);

        JSONObject result = new JSONObject();
        result.put("event", name);
        result.put("site_id", mSiteId);
        result.put("hash", hash);

        result.put("user_id", mUserId);
        result.put("sub_id", mSubId);
        result.put("debug", String.valueOf(setDebugMode(debug)));

        result.put("imei", getImei(mContext));
        result.put("ip", getIp());
        result.put("mac", getMacAddress());

        result.put("os", ANDROID);
        result.put("osversion", getOsVersion());
        result.put("android_id", getAndroidId(mContext));
        result.put("sdk", SDK);

        result.put("width", mWebViewWidth);
        result.put("heihgt", mWebViewHeight);

        if ((mCountryCodeGeo == null) && (mCountryCodeTel == null)) {
            result.put("country", "RU");
            Log.d("CLICKKY:CountryCode", "Country not found,set RU");
        } else if (mCountryCodeGeo != null) {
            result.put("country", mCountryCodeGeo);
            Log.d("CLICKKY:CountryCode", "Geo");
        } else {
            result.put("country", mCountryCodeTel);
            Log.d("CLICKKY:CountryCode", "Tel");
        }
        return result;
    }

    public void setUserID(String user_id) {
        mUserId = user_id;
    }

    public void setSubID(String subid) {
        mSubId = subid;
    }

    public int setDebugMode(boolean debug) {
        if (debug) return TRUE;

        return FALSE;
    }

    private String getImei(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telManager.getDeviceId();
        if (imei == null) {
            return "";
        }
        return telManager.getDeviceId();
    }

    private String getAndroidId(Context context) {
        return Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);
    }

    private String getIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }

        return "";
    }

    private String getMacAddress() {
        WifiManager wfManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wfManager.isWifiEnabled())
            return wfManager.getConnectionInfo().getMacAddress().toLowerCase();
        return "";
    }

    private String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    private String md5(String s) {
        try {
            //create MD5 hash
            MessageDigest digester = java.security.MessageDigest.getInstance("MD5");
            digester.update(s.getBytes());
            byte[] messageDigest = digester.digest();

            //create hex string
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);

                while (h.length() < 2)
                    h = "0" + h;

                hexString.append(h);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void onProviderDisabled(String arg0) {
    }

    public void onProviderEnabled(String arg0) {
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

}
