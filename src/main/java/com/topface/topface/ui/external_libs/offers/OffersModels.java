package com.topface.topface.ui.external_libs.offers;

import java.util.Arrays;

public class OffersModels {

    public class OfferOpened {
        private String mOffers[];

        public OfferOpened(String offers[]) {
            mOffers = offers;
        }

        public String[] getOffersTypes() {
            return mOffers;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OfferOpened that = (OfferOpened) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(mOffers, that.mOffers);

        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(mOffers);
        }
    }
}
