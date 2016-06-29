package com.topface.topface.ui.external_libs.offers.IronSource;

import android.os.Build;

import com.topface.topface.App;
import com.topface.topface.ui.external_libs.offers.GoogleAdParams;
import com.topface.topface.ui.external_libs.offers.OffersUtils;
import com.topface.topface.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

public class IronSourceOffersRequest {
    private static final String BASE_IRON_SOURCE_LINK = "http://www.supersonicads.com/";
    private static final String GET_OFFERS_LINK = "delivery/mobilePanel.php";

    private static final String APPLICATION_KEY = "applicationKey";
    private static final String APPLICATION_USER_ID_KEY = "applicationUserId";
    private static final String FORMAT_KEY = "format";
    private static final String NATIVE_AD_KEY = "nativeAd";
    private static final String DEVICE_OS_KEY = "deviceOs";
    private static final String DEVICE_IDS_KEY = "deviceIds[AID]";
    private static final String DEVICE_OS_VERSION_KEY = "deviceOSVersion";
    private static final String IS_LIMITAD_TRACKING_ENABLED_KEY = "isLimitAdTrackingEnabled";
    private static final String PAGE_SIZE_KEY = "pageSize";

    private static final String APPLICATION_KEY_VALUE = "2cf0ad4d";
    private static final String PAGE_SIZE_VALUE = "30";
    private static final String FORMAT_VALUE = "json";
    private static final String NATIVE_AD_VALUE = "1";

    public Observable<IronSourceOffersResponse> getRequestObservable() {
        return OffersUtils.getGoogleAdParamsObservable().reduce(new HashMap<String, String>(), new Func2<Map<String, String>, GoogleAdParams, Map<String, String>>() {
            @Override
            public Map<String, String> call(Map<String, String> stringStringTreeMap, GoogleAdParams googleAdParams) {
                Map<String, String> params = new HashMap<>();
                params.put(APPLICATION_KEY, APPLICATION_KEY_VALUE);
                params.put(APPLICATION_USER_ID_KEY, String.valueOf(App.get().getProfile().uid));
                params.put(FORMAT_KEY, FORMAT_VALUE);
                params.put(NATIVE_AD_KEY, NATIVE_AD_VALUE);
                params.put(DEVICE_OS_KEY, Utils.PLATFORM.toLowerCase());
                params.put(DEVICE_OS_VERSION_KEY, String.valueOf(Build.VERSION.SDK_INT));
                params.put(PAGE_SIZE_KEY, PAGE_SIZE_VALUE);
                params.put(DEVICE_IDS_KEY, googleAdParams.getId());
                params.put(IS_LIMITAD_TRACKING_ENABLED_KEY, String.valueOf(googleAdParams.isLimitAdTrackingEnabled()));
                return params;
            }
        }).switchMap(new Func1<Map<String, String>, Observable<IronSourceOffersResponse>>() {
            @Override
            public Observable<IronSourceOffersResponse> call(Map<String, String> stringStringTreeMap) {
                Retrofit retr = new Retrofit.Builder().baseUrl(BASE_IRON_SOURCE_LINK).build();
                Call<ResponseBody> call = retr.create(OffersRequest1.class).listTest(stringStringTreeMap);
                Retrofit retrofit = OffersUtils.getRequestInstance(BASE_IRON_SOURCE_LINK);
                Observable<IronSourceOffersResponse> obs = retrofit.create(OffersRequest1.class).list(stringStringTreeMap);
                return obs;
//                return OffersUtils.getRequestInstance(BASE_IRON_SOURCE_LINK).create(OffersRequest.class).list(stringStringTreeMap);
            }
        });
    }

    private interface OffersRequest1 {
        @GET(GET_OFFERS_LINK)
        Observable<IronSourceOffersResponse> list(@QueryMap Map<String, String> params);

        @GET(GET_OFFERS_LINK)
        Call<ResponseBody> listTest(@QueryMap Map<String, String> params);
    }
}
