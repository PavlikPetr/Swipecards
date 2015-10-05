package com.topface.billing;

import android.content.Context;

import com.topface.topface.App;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PaymentwallProductsRequest;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.GooglePlay;

public class StoresManager {

    public static void addStores(Context context, OpenIabHelper.Options.Builder optsBuilder) {
        optsBuilder.addAvailableStores(new GooglePlay(context, null));
        optsBuilder.addPreferredStoreName(OpenIabHelper.NAME_GOOGLE);
    }

    public static ApiRequest getPaymentwallProductsRequest() {
        return new PaymentwallProductsRequest(App.getContext()).callback(new DataApiHandler<PaymentWallProducts>() {
            @Override
            protected void success(PaymentWallProducts data, IApiResponse response) {
                //ВНИМАНИЕ! Сюда возвращается только Direct продукты,
                //парсим и записываем в кэш мы их внутри конструктора PaymentWallProducts
            }

            @Override
            protected PaymentWallProducts parseResponse(ApiResponse response) {
                //При создании нового объекта продуктов, все данные о них записываются в кэш,
                //поэтому здесь просто создаются два объекта продуктов.
                new PaymentWallProducts(response, PaymentWallProducts.TYPE.MOBILE);
                return new PaymentWallProducts(response, PaymentWallProducts.TYPE.DIRECT);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {

            }
        });
    }
}
