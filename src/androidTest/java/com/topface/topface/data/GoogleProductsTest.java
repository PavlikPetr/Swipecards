package com.topface.topface.data;

import com.topface.framework.utils.Debug;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by kirussell on 27.03.14.
 * Tests json parsing for google products data
 */
public class GoogleProductsTest extends TestCase {
    public static final String TEST = "Test";

    public void testNullData() {
        try {
            JSONObject json = new JSONObject(jsonGoogleProductsResponse);
            incrementalJsonObjectsNulling(json, new IExcecutorWithJson() {

                @Override
                public void executeWith(JSONObject json) {
                    new Products(json);
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
            @SuppressWarnings("unchecked") Iterator<String> iterKeys = jsonObj.keys();
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

    private final static String jsonGoogleProductsResponse = "{\n" +
            "  \"result\": {\n" +
            "    \"info\": {\n" +
            "      \"coinsSubscriptionMasked\": {\n" +
            "        \"status\": {\n" +
            "          \"products\": []\n" +
            "        },\n" +
            "        \"text\": \"\\\\u041f\\\\u0440\\\\u0438 \\\\u0438\\\\u0441\\\\u043f\\\\u043e\\\\u043b\\\\u044c\\\\u0437\\\\u043e\\\\u0432\\\\u0430\\\\u043d\\\\u0438\\\\u0438 \\\\u043f\\\\u043e\\\\u0434\\\\u043f\\\\u0438\\\\u0441\\\\u043e\\\\u043a \\\\u0441 \\\\u043a\\\\u0430\\\\u0436\\\\u0434\\\\u044b\\\\u043c \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\\\\u0435\\\\u043c \\\\u0432\\\\u044b \\\\u0431\\\\u0443\\\\u0434\\\\u0435\\\\u0442\\\\u0435 \\\\u043f\\\\u043e\\\\u043b\\\\u0443\\\\u0447\\\\u0430\\\\u0442\\\\u044c \\\\u0431\\\\u043e\\\\u043b\\\\u044c\\\\u0448\\\\u0435 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442 \\\\u0437\\\\u0430 \\\\u0442\\\\u0435 \\\\u0436\\\\u0435 \\\\u0434\\\\u0435\\\\u043d\\\\u044c\\\\u0433\\\\u0438\",\n" +
            "        \"hasSubscriptionButton\": {\n" +
            "          \"discount\": 10,\n" +
            "          \"hint\": \"\",\n" +
            "          \"showType\": 1,\n" +
            "          \"title\": \"\\\\u0418\\\\u0437\\\\u043c\\\\u0435\\\\u043d\\\\u0438\\\\u0442\\\\u044c \\\\u0443\\\\u0441\\\\u043b\\\\u043e\\\\u0432\\\\u0438\\\\u044f \\\\u043f\\\\u043e\\\\u0434\\\\u043f\\\\u0438\\\\u0441\\\\u043a\\\\u0438\"\n" +
            "        },\n" +
            "        \"months\": [\n" +
            "          {\n" +
            "            \"amount\": \"+20%\",\n" +
            "            \"title\": \"\\\\u041f\\\\u0435\\\\u0440\\\\u0432\\\\u044b\\\\u0439 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"amount\": \"+50%\",\n" +
            "            \"title\": \"\\\\u0412\\\\u0442\\\\u043e\\\\u0440\\\\u043e\\\\u0439 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"amount\": \"+100%\",\n" +
            "            \"title\": \"\\\\u0422\\\\u0440\\\\u0435\\\\u0442\\\\u0438\\\\u0439 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"noSubscriptionButton\": {\n" +
            "          \"discount\": 10,\n" +
            "          \"hint\": \"\",\n" +
            "          \"showType\": 1,\n" +
            "          \"title\": \"\\\\u041f\\\\u043e\\\\u043b\\\\u0443\\\\u0447\\\\u0438\\\\u0442\\\\u044c \\\\u0431\\\\u043e\\\\u043b\\\\u044c\\\\u0448\\\\u0435 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"coinsSubscription\": {\n" +
            "        \"status\": {\n" +
            "          \"products\": []\n" +
            "        },\n" +
            "        \"text\": \"\\\\u041f\\\\u0440\\\\u0438 \\\\u0438\\\\u0441\\\\u043f\\\\u043e\\\\u043b\\\\u044c\\\\u0437\\\\u043e\\\\u0432\\\\u0430\\\\u043d\\\\u0438\\\\u0438 \\\\u043f\\\\u043e\\\\u0434\\\\u043f\\\\u0438\\\\u0441\\\\u043e\\\\u043a \\\\u0441 \\\\u043a\\\\u0430\\\\u0436\\\\u0434\\\\u044b\\\\u043c \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\\\\u0435\\\\u043c \\\\u0432\\\\u044b \\\\u0431\\\\u0443\\\\u0434\\\\u0435\\\\u0442\\\\u0435 \\\\u043f\\\\u043e\\\\u043b\\\\u0443\\\\u0447\\\\u0430\\\\u0442\\\\u044c \\\\u0431\\\\u043e\\\\u043b\\\\u044c\\\\u0448\\\\u0435 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442 \\\\u0437\\\\u0430 \\\\u0442\\\\u0435 \\\\u0436\\\\u0435 \\\\u0434\\\\u0435\\\\u043d\\\\u044c\\\\u0433\\\\u0438\",\n" +
            "        \"hasSubscriptionButton\": {\n" +
            "          \"discount\": 10,\n" +
            "          \"hint\": \"\",\n" +
            "          \"showType\": 1,\n" +
            "          \"title\": \"\\\\u0418\\\\u0437\\\\u043c\\\\u0435\\\\u043d\\\\u0438\\\\u0442\\\\u044c \\\\u0443\\\\u0441\\\\u043b\\\\u043e\\\\u0432\\\\u0438\\\\u044f \\\\u043f\\\\u043e\\\\u0434\\\\u043f\\\\u0438\\\\u0441\\\\u043a\\\\u0438\"\n" +
            "        },\n" +
            "        \"months\": [\n" +
            "          {\n" +
            "            \"amount\": \"+20%\",\n" +
            "            \"title\": \"\\\\u041f\\\\u0435\\\\u0440\\\\u0432\\\\u044b\\\\u0439 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"amount\": \"+50%\",\n" +
            "            \"title\": \"\\\\u0412\\\\u0442\\\\u043e\\\\u0440\\\\u043e\\\\u0439 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"amount\": \"+100%\",\n" +
            "            \"title\": \"\\\\u0422\\\\u0440\\\\u0435\\\\u0442\\\\u0438\\\\u0439 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"noSubscriptionButton\": {\n" +
            "          \"discount\": 10,\n" +
            "          \"hint\": \"\",\n" +
            "          \"showType\": 1,\n" +
            "          \"title\": \"\\\\u041f\\\\u043e\\\\u043b\\\\u0443\\\\u0447\\\\u0438\\\\u0442\\\\u044c \\\\u0431\\\\u043e\\\\u043b\\\\u044c\\\\u0448\\\\u0435 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"premium\": [\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 1000,\n" +
            "        \"title\": \"\\\\u0411\\\\u0435\\\\u0441\\\\u043f\\\\u043b\\\\u0430\\\\u0442\\\\u043d\\\\u043e\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 1,\n" +
            "        \"type\": \"premium\",\n" +
            "        \"id\": \"com.topface.topface.sub.trial.vip.month.10\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 1000,\n" +
            "        \"title\": \"1 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446 \\\\u0437\\\\u0430 10.00 $\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 1,\n" +
            "        \"type\": \"premium\",\n" +
            "        \"id\": \"com.topface.topface.sub.vip.month.10\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 0,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 9000,\n" +
            "        \"title\": \"12 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\\\\u0435\\\\u0432 \\\\u043f\\\\u043e 7.50 $ \\\\u0432 \\\\u043c\\\\u0435\\\\u0441\\\\u044f\\\\u0446\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 12,\n" +
            "        \"type\": \"premium\",\n" +
            "        \"id\": \"com.topface.topface.sub.vip.year.90\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"energy\": [],\n" +
            "    \"coins\": [\n" +
            "      {\n" +
            "        \"showType\": 0,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 100,\n" +
            "        \"title\": \"6 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 6,\n" +
            "        \"type\": \"coins\",\n" +
            "        \"id\": \"com.topface.topface.coins.6.1\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 0,\n" +
            "        \"hint\": \"10 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442 \\\\u0431\\\\u0435\\\\u0441\\\\u043f\\\\u043b\\\\u0430\\\\u0442\\\\u043d\\\\u043e\",\n" +
            "        \"price\": 500,\n" +
            "        \"title\": \"40 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 40,\n" +
            "        \"type\": \"coins\",\n" +
            "        \"id\": \"com.topface.topface.coins.40.5\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 0,\n" +
            "        \"hint\": \"40 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442 \\\\u0431\\\\u0435\\\\u0441\\\\u043f\\\\u043b\\\\u0430\\\\u0442\\\\u043d\\\\u043e\",\n" +
            "        \"price\": 1000,\n" +
            "        \"title\": \"100 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 100,\n" +
            "        \"type\": \"coins\",\n" +
            "        \"id\": \"com.topface.topface.coins.100.10\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"150 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442 \\\\u0431\\\\u0435\\\\u0441\\\\u043f\\\\u043b\\\\u0430\\\\u0442\\\\u043d\\\\u043e\",\n" +
            "        \"price\": 2500,\n" +
            "        \"title\": \"300 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 300,\n" +
            "        \"type\": \"coins\",\n" +
            "        \"id\": \"com.topface.topface.coins.300.25\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"coinsSubscription\": [\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 100,\n" +
            "        \"title\": \"6 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 10,\n" +
            "        \"amount\": 6,\n" +
            "        \"type\": \"coinsSubscription\",\n" +
            "        \"id\": \"com.topface.topface.sub.coins.month.6.1\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 500,\n" +
            "        \"title\": \"40 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 10,\n" +
            "        \"amount\": 40,\n" +
            "        \"type\": \"coinsSubscription\",\n" +
            "        \"id\": \"com.topface.topface.sub.coins.month.40.5\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 1000,\n" +
            "        \"title\": \"100 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 10,\n" +
            "        \"amount\": 100,\n" +
            "        \"type\": \"coinsSubscription\",\n" +
            "        \"id\": \"com.topface.topface.sub.coins.month.100.10\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 2500,\n" +
            "        \"title\": \"300 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 10,\n" +
            "        \"amount\": 300,\n" +
            "        \"type\": \"coinsSubscription\",\n" +
            "        \"id\": \"com.topface.topface.sub.coins.month.300.20\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"likes\": [\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 300,\n" +
            "        \"title\": \"134 \\\\u0441\\\\u0438\\\\u043c\\\\u043f\\\\u0430\\\\u0442\\\\u0438\\\\u0438\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 134,\n" +
            "        \"type\": \"likes\",\n" +
            "        \"id\": \"com.topface.topface.likes.100.3\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"coinsSubscriptionMasked\": [\n" +
            "      {\n" +
            "        \"showType\": 0,\n" +
            "        \"hint\": \"\",\n" +
            "        \"price\": 100,\n" +
            "        \"title\": \"6 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 6,\n" +
            "        \"type\": \"coinsSubscription\",\n" +
            "        \"id\": \"com.topface.topface.sub.coins.month.6.1\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 0,\n" +
            "        \"hint\": \"10 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442 \\\\u0431\\\\u0435\\\\u0441\\\\u043f\\\\u043b\\\\u0430\\\\u0442\\\\u043d\\\\u043e\",\n" +
            "        \"price\": 500,\n" +
            "        \"title\": \"40 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 40,\n" +
            "        \"type\": \"coinsSubscription\",\n" +
            "        \"id\": \"com.topface.topface.sub.coins.month.40.5\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 0,\n" +
            "        \"hint\": \"40 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442 \\\\u0431\\\\u0435\\\\u0441\\\\u043f\\\\u043b\\\\u0430\\\\u0442\\\\u043d\\\\u043e\",\n" +
            "        \"price\": 1000,\n" +
            "        \"title\": \"100 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 100,\n" +
            "        \"type\": \"coinsSubscription\",\n" +
            "        \"id\": \"com.topface.topface.sub.coins.month.100.10\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"showType\": 1,\n" +
            "        \"hint\": \"150 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442 \\\\u0431\\\\u0435\\\\u0441\\\\u043f\\\\u043b\\\\u0430\\\\u0442\\\\u043d\\\\u043e\",\n" +
            "        \"price\": 2500,\n" +
            "        \"title\": \"300 \\\\u043c\\\\u043e\\\\u043d\\\\u0435\\\\u0442\",\n" +
            "        \"discount\": 0,\n" +
            "        \"amount\": 300,\n" +
            "        \"type\": \"coinsSubscription\",\n" +
            "        \"id\": \"com.topface.topface.sub.coins.month.300.20\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"leader\": []\n" +
            "  },\n" +
            "  \"balance\": {\n" +
            "    \"money\": 6,\n" +
            "    \"premium\": false,\n" +
            "    \"likes\": 0\n" +
            "  },\n" +
            "  \"unread\": {\n" +
            "    \"admirations\": 0,\n" +
            "    \"mutual\": 0,\n" +
            "    \"dialogs\": 0,\n" +
            "    \"peopleNearby\": 0,\n" +
            "    \"visitors\": 0,\n" +
            "    \"fans\": 0,\n" +
            "    \"likes\": 0\n" +
            "  },\n" +
            "  \"id\": \"\",\n" +
            "  \"method\": \"googleplay.getProducts\"\n" +
            "}";
}
