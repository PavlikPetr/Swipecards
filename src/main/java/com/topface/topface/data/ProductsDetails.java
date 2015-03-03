package com.topface.topface.data;

import com.google.gson.JsonSyntaxException;
import com.topface.framework.JsonUtils;

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
     * initialize this with prepeared map of details
     *
     * @param detailsMap prepeared map of details
     */
    private ProductsDetails(HashMap<String, ProductDetail> detailsMap) {
        mDetailsMap = detailsMap;
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
        return null;
    }

    /**
     * creates details storage from Inventory received from OpenIab helper
     *
     * @param inventory Inventory from OpenIab helper
     * @return new details storage
     */
    public static ProductsDetails createFromInventory(Inventory inventory) {

        Map<String, SkuDetails> skuMap = inventory.getSkuMap();
        HashMap<String, ProductDetail> detailsMap = new HashMap<>();
        if (skuMap != null && !skuMap.isEmpty()) {
            for (String id : skuMap.keySet()) {
                ProductDetail productDetail = ProductDetail.createFromGooglePlayJson(skuMap.get(id).getJson());
                if (productDetail != null) {
                    detailsMap.put(productDetail.id, productDetail);
                }
            }
        }
        return new ProductsDetails(detailsMap);
    }

    /**
     * GooglePlay SKU details structure
     * need for Gson parsing
     */
    private class GPSkuDetail {
        public String productId;
        public String type;
        public String price;
        public long price_amount_micros;
        public String price_currency_code;
        public String title;
        public String description;
    }

    /**
     * Class for holding info about one product details
     * and work with it
     */
    public static class ProductDetail {
        // Price in micro-units, where 1,000,000 micro-units equal one unit of the currency.
        // For example, if price is "€7.99", price_amount_micros is "7990000".
        public long price;
        // ISO 4217 currency code for price.
        // For example, if price is specified in British pounds sterling, price_currency_code is "GBP".
        public String currency;
        // The product ID for the product.
        public String id;

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
        }

        /**
         * creates product detail from Google Play formatted JSON
         *
         * @param gpDetailSource String with GP JSON
         * @return new product detail
         */
        private static ProductDetail createFromGooglePlayJson(String gpDetailSource) {
            try {
                GPSkuDetail gpSkuDetail = JsonUtils.fromJson(gpDetailSource, GPSkuDetail.class);
                return new ProductDetail(gpSkuDetail.productId, gpSkuDetail.price_amount_micros, gpSkuDetail.price_currency_code);
            } catch (JsonSyntaxException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
}
