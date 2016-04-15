package com.topface.topface.ui.external_libs;

import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.topface.topface.App;

/**
 * Created by ppavlik on 15.04.16.
 * Track Android app conversions
 */
public class AdWords {

    private static final String APP_START_CONVERSION_ID = "967366081";
    private static final String APP_START_LABEL = "uJf-CMnD52MQwaujzQM";
    private static final String PURCHASES_CONVERSION_ID = "967366081";
    private static final String PURCHASES_LABEL = "LR8lCLigy2UQwaujzQM";
    private static final String TRIAL_CONVERSION_ID = "967366081";
    private static final String TRIAL_LABEL = "RrvUCJ-d3WUQwaujzQM";
    private static final String FIRST_PURCHASE_CONVERSION_ID = "967366081";
    private static final String FIRST_PURCHASE_LABEL = "YLwECJuqy2UQwaujzQM";
    private static final String REPORT_VALUE = "1";

    public void trackInstall() {
        AdWordsConversionReporter.reportWithConversionId(App.getContext(),
                APP_START_CONVERSION_ID, APP_START_LABEL, REPORT_VALUE, false);
    }

    public void trackPurchase() {
        AdWordsConversionReporter.reportWithConversionId(App.getContext(),
                PURCHASES_CONVERSION_ID, PURCHASES_LABEL, REPORT_VALUE, false);
    }

    public void trackTrial() {
        AdWordsConversionReporter.reportWithConversionId(App.getContext(),
                TRIAL_CONVERSION_ID, TRIAL_LABEL, REPORT_VALUE, false);
    }

    public void trackFirstPurchase() {
        AdWordsConversionReporter.reportWithConversionId(App.getContext(),
                FIRST_PURCHASE_CONVERSION_ID, FIRST_PURCHASE_LABEL, REPORT_VALUE, false);
    }
}
