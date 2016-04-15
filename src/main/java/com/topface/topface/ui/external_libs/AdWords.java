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
    private static final String APP_START_VALUE = "0.00";

    public void trackAppStart() {
        AdWordsConversionReporter.reportWithConversionId(App.getContext(),
                APP_START_CONVERSION_ID, APP_START_LABEL, APP_START_VALUE, false);
    }
}
