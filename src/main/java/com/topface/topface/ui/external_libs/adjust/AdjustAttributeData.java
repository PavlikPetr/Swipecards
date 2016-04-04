package com.topface.topface.ui.external_libs.adjust;

import android.text.TextUtils;

import com.adjust.sdk.AdjustAttribution;
import com.topface.topface.utils.Utils;

/**
 * Created by ppavlik on 04.04.16.
 * extended data class from AdjustAttribution
 * we need it to create and detect empty data
 */
public class AdjustAttributeData extends AdjustAttribution {

    public AdjustAttributeData(AdjustAttribution attribution) {
        this.trackerToken = attribution != null && attribution.trackerToken != null ? attribution.trackerToken : Utils.EMPTY;
        this.trackerName = attribution != null && attribution.trackerName != null ? attribution.trackerName : Utils.EMPTY;
        this.network = attribution != null && attribution.network != null ? attribution.network : Utils.EMPTY;
        this.adgroup = attribution != null && attribution.adgroup != null ? attribution.adgroup : Utils.EMPTY;
        this.campaign = attribution != null && attribution.campaign != null ? attribution.campaign : Utils.EMPTY;
        this.clickLabel = attribution != null && attribution.clickLabel != null ? attribution.clickLabel : Utils.EMPTY;
        this.creative = attribution != null && attribution.creative != null ? attribution.creative : Utils.EMPTY;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(trackerToken)
                && TextUtils.isEmpty(trackerName)
                && TextUtils.isEmpty(network)
                && TextUtils.isEmpty(adgroup)
                && TextUtils.isEmpty(campaign)
                && TextUtils.isEmpty(clickLabel)
                && TextUtils.isEmpty(creative);
    }
}
