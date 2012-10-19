package com.topface.topface.data;

import android.content.Context;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.http.ProfileBackgrounds;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/* Класс профиля владельца устройства */
public class Profile extends AbstractDataWithPhotos {

    public int uid; // id пользователя в топфейсе
    public String first_name; // имя пользователя
    public int age; // возраст пользователя
    public int sex; // секс пользователя

    // Unread
    public int unread_rates; // количество непрочитанных оценок пользователя
    public int unread_likes; // количество непрочитанных “понравилось” пользователя
    public int unread_messages; // количество непрочитанных сообщений пользователя
    public int unread_mutual; // количество непрочитанных симпатий

    // City
    public int city_id; // идентификтаор города пользователя
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя

    // Resources
    public int money; // количество монет у пользователя
    public int power; // количество энергии пользователя
    public int average_rate; // средняя оценка текущего пользователя

    // Dating
    public int dating_sex; // пол пользователей для поиска
    public int dating_age_start; // начальный возраст для пользователей
    public int dating_age_end; // конечный возраст для пользователей
    public int dating_city_id; // идентификатор города для поиска пользователей
    public String dating_city_name; // наименование пользователя в русской локали
    public String dating_city_full; // полное наименование города

    public String status; // статус пользователя

    public LinkedList<FormItem> forms = new LinkedList<FormItem>();

    private static boolean mIsUserProfile;

    public LinkedList<Gift> gifts = new LinkedList<Gift>();

    public int background;

    //private static final String profileFileName = "profile.out";
    //private static final long serialVersionUID  = 2748391675222256671L;    

    public static Profile parse(ApiResponse response) {
        return parse(new Profile(), response.jsonResult);
    }

    protected static Profile parse(Profile profile, JSONObject resp) {
        try {
            profile.unread_rates = resp.optInt("unread_rates");
            profile.unread_likes = resp.optInt("unread_likes");
            profile.unread_messages = resp.optInt("unread_messages");
            profile.unread_mutual = resp.optInt("unread_symphaties");
            profile.average_rate = resp.optInt("average_rate");
            profile.money = resp.optInt("money");

            int power = resp.optInt("power");
            //if(power > 10000) power = 10000;
            profile.power = (int) (power * 0.01);

            profile.uid = resp.optInt("uid");
            profile.age = resp.optInt("age");
            profile.sex = resp.optInt("sex");
            profile.status = resp.optString("status");
            profile.first_name = resp.optString("first_name");

            // city
            if (!resp.isNull("city")) {
                JSONObject city = resp.getJSONObject("city");
                profile.city_id = city.optInt("id");
                profile.city_name = city.optString("name");
                profile.city_full = city.optString("full");
            }

            // dating filter
            if (!resp.isNull("dating")) {
                JSONObject dating = resp.getJSONObject("dating");
                profile.dating_sex = dating.optInt("sex");
                profile.dating_age_start = dating.optInt("age_start");
                profile.dating_age_end = dating.optInt("age_end");
                JSONObject datingCity = dating.getJSONObject("city");
                profile.dating_city_id = datingCity.optInt("id");
                profile.dating_city_name = datingCity.optString("name");
                profile.dating_city_full = datingCity.optString("full");
            }

            //gifts
            JSONArray arrGifts = resp.optJSONArray("gifts");
            for (int i = 0; i < arrGifts.length(); i++) {
                JSONObject itemGift = arrGifts.getJSONObject(i);
                Gift gift = new Gift();
                gift.id = itemGift.optInt("gift");
                gift.link = itemGift.optString("link");
                gift.type = Gift.PROFILE;
                gift.feedId = itemGift.optInt("id");
                profile.gifts.add(gift);
            }

            profile.background = resp.optInt("background", ProfileBackgrounds.DEFAULT_BACKGROUND_ID);

            Context context = App.getContext();

            // form
            if (!resp.isNull("form")) {
                JSONObject form = resp.getJSONObject("form");

                FormInfo formInfo = new FormInfo(context, profile);

                FormItem formItem;

                if (profile instanceof User) {
                    mIsUserProfile = true;
                    ((User) profile).formMatches = form.optInt("goodness");
                }

                //1 HEADER -= MAIN =-
                formItem = new FormItem(R.string.form_main, FormItem.HEADER);
                formInfo.fillFormItem(formItem);
                profile.forms.add(formItem);

                //2 character  position 0
                int position = 0;
                formItem = new FormItem(R.array.form_main_character, form.optInt("character_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("character_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //3 communication position 1
                formItem = new FormItem(R.array.form_main_communication, form.optInt("communication_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("communication_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //4 DIVIDER
                profile.forms.add(FormItem.getDivider());

                //5 HEADER -= PHYSIQUE =-
                formItem = new FormItem(R.string.form_physique, FormItem.HEADER);
                formInfo.fillFormItem(formItem);
                profile.forms.add(formItem);

                //6 fitness  position 2
                formItem = new FormItem(R.array.form_physique_fitness, form.optInt("fitness_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("fitness_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //7 height  position 3
                formItem = new FormItem(R.array.form_main_height, Integer.toString(form.optInt("height")), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("height_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //8 weight  position 4
                formItem = new FormItem(R.array.form_main_weight, Integer.toString(form.optInt("weight")), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("weight_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //9 hair  position 5
                formItem = new FormItem(R.array.form_physique_hairs, form.optInt("hair_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("hair_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //10 eye  position 6
                formItem = new FormItem(R.array.form_physique_eyes, form.optInt("eyes_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("eyes_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //11 DIVIDER                
                profile.forms.add(FormItem.getDivider());

                //12 HEADER -= SOCIAL =-
                formItem = new FormItem(R.string.form_social, FormItem.HEADER);
                formInfo.fillFormItem(formItem);
                profile.forms.add(formItem);

                //13 marriage position 7
                formItem = new FormItem(R.array.form_social_marriage, form.optInt("marriage_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("marriage_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //14 education  position 8
                formItem = new FormItem(R.array.form_social_education, form.optInt("education_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("education_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //15 finances  position 9
                formItem = new FormItem(R.array.form_social_finances, form.optInt("finances_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("finances_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //16 residence  position 10
                formItem = new FormItem(R.array.form_social_residence, form.optInt("residence_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("residence_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //17 car vs car_id  position 11
                formItem = new FormItem(R.array.form_social_car, form.optInt("car_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("car_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //18 DIVIDER
                profile.forms.add(FormItem.getDivider());

                //19 HEADER -= HABITS =-
                formItem = new FormItem(R.string.form_habits, FormItem.HEADER);
                formInfo.fillFormItem(formItem);
                profile.forms.add(formItem);

                //20 smoking  position 12
                formItem = new FormItem(R.array.form_habits_smoking, form.optInt("smoking_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("smoking_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //21 alcohol  position 13
                formItem = new FormItem(R.array.form_habits_alcohol, form.optInt("alcohol_id"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                if (mIsUserProfile) {
                    position++;
                    compareFormItemData(formItem, profile, form.optBoolean("alcohol_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                //22 restaurants  position 14
                formItem = new FormItem(R.array.form_habits_restaurants, form.optString("restaurants"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                profile.forms.add(formItem);

                //23 DIVIDER
                profile.forms.add(FormItem.getDivider());

                //24 HEADER -= DETAIL =-
                formItem = new FormItem(R.string.form_detail, FormItem.HEADER);
                formInfo.fillFormItem(formItem);
                profile.forms.add(formItem);

                //25 first_dating  position 15
                formItem = new FormItem(R.array.form_detail_about_dating, form.optString("first_dating"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                profile.forms.add(formItem);

                //26 achievements  position 16
                formItem = new FormItem(R.array.form_detail_archievements, form.optString("achievements"), FormItem.DATA);
                formInfo.fillFormItem(formItem);
                profile.forms.add(formItem);

                //27 DIVIDER
                profile.forms.add(FormItem.getDivider());

            }

            initPhotos(resp, profile);

        } catch (Exception e) {
            Debug.log("Profile.class", "Wrong response parsing: " + e);
        }

        return profile;
    }

    private static void compareFormItemData(FormItem item, Profile profile, boolean matches) {
        item.equal = matches;
        if (item.dataId > 0) {
            profile.forms.add(item);
        }
    }
}