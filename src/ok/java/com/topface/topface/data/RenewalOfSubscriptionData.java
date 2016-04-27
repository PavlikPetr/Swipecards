package com.topface.topface.data;

import java.util.ArrayList;

public class RenewalOfSubscriptionData {
    public ArrayList<SubscriptionData> renewals;

    public class SubscriptionData {
        public String orderId;
        public double amount;
        public String currency;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SubscriptionData)) return false;
            SubscriptionData subscriptionData = (SubscriptionData) o;
            return orderId.equals(subscriptionData.orderId)
                    && amount == subscriptionData.amount
                    && currency.equals(subscriptionData.currency);
        }

        @Override
        public int hashCode() {
            int result = orderId != null ? orderId.hashCode() : 0;
            result = 31 * result + (int) amount;
            result = 31 * result + (currency != null ? currency.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RenewalOfSubscriptionData && renewals.equals(((RenewalOfSubscriptionData) o).renewals);
    }

    @Override
    public int hashCode() {
        return renewals != null ? renewals.hashCode() : 0;
    }
}
