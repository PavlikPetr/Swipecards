package com.topface.topface.data;

import com.topface.topface.utils.Debug;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by kirussell on 27.03.14.
 */
public class GoogleProductsTest extends TestCase {
    public static final String TEST = "Test";

    public void testNullData() {
        try {
            JSONObject json = new JSONObject(jsonGoogleProductsResponse);
            incrementalJsonObjectsNulling(json, new IExcecutorWithJson() {

                @Override
                public void executeWith(JSONObject json) {
                    new GooglePlayProducts(json);
                    Debug.log(TEST, json.toString());
                }
            });

        } catch (Error er) {
            assertTrue("Error: " + er.toString(), false);
        } catch (JSONException e) {
            assertTrue("Json exception", false);
        } catch (Exception ex) {
            assertTrue("Exception: " + ex.toString(), false);
        }
    }

    private void incrementalJsonObjectsNulling(JSONObject json, IExcecutorWithJson executor) throws JSONException {
        executor.executeWith(json);
        recursiveJsonObjectsNull(json, json, executor);
    }

    private void recursiveJsonObjectsNull(JSONObject originalJson, Object currJsonObj,
                                          IExcecutorWithJson executor) throws JSONException {
        if (currJsonObj == null) {
            return;
        }
        if (currJsonObj instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) currJsonObj;
            for (int i = 0; i < jsonArr.length(); i++) {
                recursiveJsonObjectsNull(originalJson, jsonArr.get(i), executor);
                jsonArr.put(i, JSONObject.NULL);
                executor.executeWith(originalJson);
            }
        } else if (currJsonObj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) currJsonObj;
            Iterator<String> iterKeys = jsonObj.keys();
            while (iterKeys.hasNext()) {
                String key = iterKeys.next();
                recursiveJsonObjectsNull(originalJson, jsonObj.opt(key), executor);
                jsonObj.put(key, JSONObject.NULL);
                executor.executeWith(originalJson);
            }
        }
    }

    interface IExcecutorWithJson {
        void executeWith(JSONObject json);
    }

    private final static String jsonGoogleProductsResponse = "{" +
            "    \"coins\": [" +
            "        {" +
            "            \"amount\": 6," +
            "            \"id\": \"com.topface.topface.coins.6.1\"," +
            "            \"title\": \"6 coins\"," +
            "            \"price\": 100," +
            "            \"showType\": 0," +
            "            \"hint\": \"\"," +
            "            \"type\": \"coins\"," +
            "            \"discount\": 0" +
            "        }," +
            "        {" +
            "            \"amount\": 40," +
            "            \"id\": \"com.topface.topface.coins.40.5\"," +
            "            \"title\": \"40 coins\"," +
            "            \"price\": 500," +
            "            \"showType\": 0," +
            "            \"hint\": \"10 coins for free\"," +
            "            \"type\": \"coins\"," +
            "            \"discount\": 0" +
            "        }," +
            "        {" +
            "            \"amount\": 100," +
            "            \"id\": \"com.topface.topface.coins.100.10\"," +
            "            \"title\": \"100 coins\"," +
            "            \"price\": 1000," +
            "            \"showType\": 0," +
            "            \"hint\": \"40 coins for free\"," +
            "            \"type\": \"coins\"," +
            "            \"discount\": 0" +
            "        }," +
            "        {" +
            "            \"amount\": 300," +
            "            \"id\": \"com.topface.topface.coins.300.25\"," +
            "            \"title\": \"300 coins\"," +
            "            \"price\": 2500," +
            "            \"showType\": 1," +
            "            \"hint\": \"150 coins for free\"," +
            "            \"type\": \"coins\"," +
            "            \"discount\": 0" +
            "        }" +
            "    ]," +
            "    \"premium\": [" +
            "        {" +
            "            \"amount\": 1," +
            "            \"id\": \"com.topface.topface.sub.vip.month.10\"," +
            "            \"title\": \"1 month for 10.00 $\"," +
            "            \"price\": 1000," +
            "            \"showType\": 1," +
            "            \"hint\": \"\"," +
            "            \"type\": \"premium\"," +
            "            \"discount\": 0" +
            "        }," +
            "        {" +
            "            \"amount\": 12," +
            "            \"id\": \"com.topface.topface.sub.vip.year.90\"," +
            "            \"title\": \"12 months for 7.50 $ a month\"," +
            "            \"price\": 9000," +
            "            \"showType\": 0," +
            "            \"hint\": \"\"," +
            "            \"type\": \"premium\"," +
            "            \"discount\": 0" +
            "        }" +
            "    ]," +
            "    \"energy\": []," +
            "    \"coinsSubscription\": [" +
            "        {" +
            "            \"amount\": 40," +
            "            \"id\": \"com.topface.topface.sub.coins.month.40.5\"," +
            "            \"title\": \"40 coins\"," +
            "            \"price\": 500," +
            "            \"showType\": 1," +
            "            \"hint\": \"\"," +
            "            \"type\": \"coinsSubscription\"," +
            "            \"discount\": 10" +
            "        }," +
            "        {" +
            "            \"amount\": 100," +
            "            \"id\": \"com.topface.topface.sub.coins.month.100.10\"," +
            "            \"title\": \"100 coins\"," +
            "            \"price\": 1000," +
            "            \"showType\": 1," +
            "            \"hint\": \"\"," +
            "            \"type\": \"coinsSubscription\"," +
            "            \"discount\": 10" +
            "        }," +
            "        {" +
            "            \"amount\": 300," +
            "            \"id\": \"com.topface.topface.sub.coins.month.300.20\"," +
            "            \"title\": \"300 coins\"," +
            "            \"price\": 2500," +
            "            \"showType\": 1," +
            "            \"hint\": \"\"," +
            "            \"type\": \"coinsSubscription\"," +
            "            \"discount\": 10" +
            "        }" +
            "    ]," +
            "    \"likes\": [" +
            "        {" +
            "            \"amount\": 134," +
            "            \"id\": \"com.topface.topface.likes.100.3\"," +
            "            \"title\": \"134 likes\"," +
            "            \"price\": 300," +
            "            \"showType\": 1," +
            "            \"hint\": \"\"," +
            "            \"type\": \"likes\"," +
            "            \"discount\": 0" +
            "        }" +
            "    ]," +
            "    \"leader\": []," +
            "    \"info\": {" +
            "        \"coinsSubscription\": {" +
            "            \"hasSubscriptionButton\": {" +
            "                \"showType\": 1," +
            "                \"hint\": \"\"," +
            "                \"title\": \"Change your subscription\"," +
            "                \"discount\": 10" +
            "            }," +
            "            \"text\": \"When using the subscription every month, you will get more coins for the same amount of money\"," +
            "            \"months\": [" +
            "                {" +
            "                    \"amount\": \"+20%\"," +
            "                    \"title\": \"First month\"" +
            "                }," +
            "                {" +
            "                    \"amount\": \"+50%\"," +
            "                    \"title\": \"Second month\"" +
            "                }," +
            "                {" +
            "                    \"amount\": \"+100%\"," +
            "                    \"title\": \"Third month\"" +
            "                }" +
            "            ]," +
            "            \"status\": {" +
            "                \"products\": []" +
            "            }," +
            "            \"noSubscriptionButton\": {" +
            "                \"showType\": 1," +
            "                \"hint\": \"\"," +
            "                \"title\": \"Get more coins\"," +
            "                \"discount\": 10" +
            "            }" +
            "        }" +
            "    }" +
            "}";
}
