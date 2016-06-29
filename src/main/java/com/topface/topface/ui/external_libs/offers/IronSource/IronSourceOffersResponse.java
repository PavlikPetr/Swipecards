package com.topface.topface.ui.external_libs.offers.IronSource;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class IronSourceOffersResponse {

    @SerializedName("offers")
    private List<IronSourceOfferwallModel> mOffers = new ArrayList<>();

    public List<IronSourceOfferwallModel> getOffers() {
        return mOffers;
    }
}
