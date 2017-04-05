package com.topface.topface.ui;

import android.app.Application;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.data.Products;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GooglePlayProductsRequest;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
//import com.topface.topface.requests.PaymentNinjaProductsRequest;
//import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProductsList;
import com.topface.topface.utils.CacheProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppetr on 22.07.15.
 * empty parent for App
 */
public class ApplicationBase extends Application {

    public static List<IApiRequest> getProductsRequest() {
        List<IApiRequest> list = new ArrayList<>();
        GooglePlayProductsRequest gpProductRequest = new GooglePlayProductsRequest(App.getContext());
//        PaymentNinjaProductsRequest pnProductRequest = new PaymentNinjaProductsRequest(App.getContext());
//
        list.add(gpProductRequest);
//        list.add(pnProductRequest);
        gpProductRequest.callback(new DataApiHandler<Products>() {
            @Override
            protected void success(Products data, IApiResponse response) {

            }

            @Override
            protected Products parseResponse(ApiResponse response) {
                return new Products(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {

            }
        });
//        pnProductRequest.callback(new DataApiHandler<PaymentNinjaProductsList>() {
//
//            @Override
//            public void fail(int codeError, IApiResponse response) {
//            }
//
//            @Override
//            protected void success(PaymentNinjaProductsList data, IApiResponse response) {
//                CacheProfile.setPaymentNinjaProducts(data);
//            }
//
//            @Override
//            protected PaymentNinjaProductsList parseResponse(ApiResponse response) {
//                return JsonUtils.fromJson(response.toString(), PaymentNinjaProductsList.class);
//            }
//        });
        return list;
    }

}