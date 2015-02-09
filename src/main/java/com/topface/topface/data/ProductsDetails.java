package com.topface.topface.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.SkuDetails;

import java.util.HashMap;
import java.util.Map;

/**
 * This class works with extended information about Google Play products
 * like currency and price.
 */
public class ProductsDetails {

    public static final double MICRO_AMOUNT = 1000000.0;

    // map of products details
    private Map<String, ProductDetail> mDetailsMap = new HashMap<>();

    /**
     * initialize this with cached information about products details
     *
     * @param json cached JSONArray of details
     */
    public ProductsDetails(JSONArray json) {
        int length = json.length();
        if (length > 0) {
            mDetailsMap = new HashMap<>();
            for (int i = 0; i < length; i++) {
                ProductDetail detail = new ProductDetail(json.optString(i));
                if (detail.isReady()) {
                    mDetailsMap.put(detail.id, detail);
                }
            }
        }
    }

    /**
     * returns information about specified product
     *
     * @param productId ID of product to search
     * @return stored product information, or "bad", empty product, if not found
     */
    public ProductDetail getProductDetail(String productId) {
        if (mDetailsMap.containsKey(productId)) {
            return mDetailsMap.get(productId);
        }
        return new ProductDetail();
    }

    /**
     * generates JSONArray in String to cache this whole list of details
     *
     * @return String containig JSONArray
     */
    public String getJson() {
        JSONArray resultArray = new JSONArray();
        for (String key : mDetailsMap.keySet()) {
            resultArray.put(mDetailsMap.get(key));
        }
        return resultArray.toString();
    }

    /**
     * creates details storage from Inventory received from OpenIab helper
     *
     * @param inventory Inventory from OpenIab helper
     * @return new details storage
     */
    public static ProductsDetails createFromInventory(Inventory inventory) {

        Map<String, SkuDetails> skuMap = inventory.getSkuMap();
        JSONArray json = new JSONArray();
        if (skuMap != null && !skuMap.isEmpty()) {
            for (String id : skuMap.keySet()) {
                json.put(ProductDetail.createFromGooglePlayJson(skuMap.get(id).getJson()).toJson().toString());
            }
        }
        return new ProductsDetails(json);
    }

    /**
     * Class for holding info about one product details
     * and work with it
     */
    public static class ProductDetail {
        /**
         * fields in JSON for internal use
         * and for initialization from GP
         */
        public static class Field {
            // internal fields
            public static final String PRICE = "price";
            public static final String CURRENCY = "currency";
            public static final String ID = "id";
            // GooglePlay fields
            public static final String GP_CURRENCY = "price_currency_code";
            public static final String GP_PRICE = "price_amount_micros";
            public static final String GP_ID = "productId";
        }

        // Price in micro-units, where 1,000,000 micro-units equal one unit of the currency.
        // For example, if price is "€7.99", price_amount_micros is "7990000".
        public long price;
        // ISO 4217 currency code for price.
        // For example, if price is specified in British pounds sterling, price_currency_code is "GBP".
        public String currency;
        // The product ID for the product.
        public String id;
        // was this product created normally?
        private boolean mIsReady = false;

        /**
         * default constructor - products was not created
         */
        public ProductDetail() {
            mIsReady = false;
        }

        /**
         * creates product from internal json format
         *
         * @param jsonSource internal JSONOBject to parse
         */
        public ProductDetail(String jsonSource) {
            try {
                JSONObject json = new JSONObject(jsonSource);
                this.currency = json.optString(Field.CURRENCY);
                this.price = json.optLong(Field.PRICE);
                this.id = json.optString(Field.ID);
                mIsReady = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * creates products from direct initial values
         * used for creation from GP details
         *
         * @param id       product ID
         * @param price    product price in micro-units
         * @param currency product cerrency code
         */
        public ProductDetail(String id, Long price, String currency) {
            this.price = price;
            this.currency = currency;
            this.id = id;
            this.mIsReady = true;
        }

        public boolean isReady() {
            return mIsReady;
        }

        /**
         * generates JSON of internal format
         *
         * @return internal formatted JSON, usualy for write to cache
         */
        public JSONObject toJson() {
            JSONObject result = new JSONObject();
            try {
                if (this.mIsReady) {
                    result.put(Field.CURRENCY, this.currency);
                    result.put(Field.PRICE, this.price);
                    result.put(Field.ID, this.id);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        /**
         * creates product detail from Google Play formatted JSON
         *
         * @param gpDetailSource String with GP JSON
         * @return new product detail
         */
        private static ProductDetail createFromGooglePlayJson(String gpDetailSource) {
            try {
                JSONObject json = new JSONObject(gpDetailSource);
                String currency = json.optString(Field.GP_CURRENCY);
                long price = json.optLong(Field.GP_PRICE);
                String id = json.optString(Field.GP_ID);
                return new ProductDetail(id, price, currency);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new ProductDetail();
        }
    }
}
