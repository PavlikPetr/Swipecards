package com.topface.topface.data;

import java.util.ArrayList;

public class RenewalOfSubscriptionData {
    public ArrayList<SubscriptionData> renewals;

    public class SubscriptionData {
        public String orderId;
        public double amount;
        public String currency;
    }
}
