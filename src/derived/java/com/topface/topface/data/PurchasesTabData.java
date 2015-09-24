package com.topface.topface.data;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PurchasesTabData {
    public static final String GPLAY = "google-play";
    public static final String AMAZON = "amazon";
    public static final String PWALL_MOBILE = "paymentwall-mobile";
    public static final String PWALL = "paymentwall-direct";
    public static final String BONUS = "bonus";

    /**
     * !!! IMPORTANT !!!
     * markets stores all available markets. Used to delete missing tabs on older client versions.
     * Add all new purchase tabs to markets.
     */
    public static Set<String> markets = new HashSet<>();

    static {
        markets.add(GPLAY);
        markets.add(AMAZON);
        markets.add(PWALL_MOBILE);
        markets.add(PWALL);
        markets.add(BONUS);
    }

    public String name;
    public String type;

    public PurchasesTabData(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getUpperCaseName() {
        return name.toUpperCase(Locale.getDefault());
    }
}