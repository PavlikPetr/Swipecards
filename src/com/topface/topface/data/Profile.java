package com.topface.topface.data;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.*;
import com.topface.topface.utils.http.ProfileBackgrounds;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

/* Класс профиля владельца устройства */
public class Profile extends AbstractDataWithPhotos {

    private static String[] EMPTY_STATUSES = {Static.EMPTY, "-", "."};


    public int uid; // id пользователя в топфейсе
    public String first_name; // имя пользователя
    public int age; // возраст пользователя
    public int sex; // пол пользователя

    //Город текущего пользователя
    public City city;

    // Resources
    public int money; // количество монет у пользователя
    public int likes; // количество энергии пользователя

    public int average_rate; // средняя оценка текущего пользователя

    // Фильтр поиска
    public DatingFilter dating;

    // Premium
    public boolean premium;
    public boolean invisible;
    public boolean inBlackList;

    protected String status; // статус пользователя

    public LinkedList<FormItem> forms = new LinkedList<FormItem>();

    public ArrayList<Gift> gifts = new ArrayList<Gift>();
    public SparseArrayCompat<TopfaceNotifications> notifications = new SparseArrayCompat<TopfaceNotifications>();
    public boolean hasMail;
    public boolean email_grabbed;
    public boolean email_confirmed;
    public int xstatus;

    public int totalPhotos;
    // Идентификатор заднего фона в профиле
    public int background;
    // Платяший пользователь или нет
    public boolean paid;
    // Показывать рекламу или нет
    public boolean show_ad;
    public boolean isGcmSupported;
    /**
     * Флаг того, является ли пользоветль редактором
     */
    private boolean mEditor;
    // private static final String profileFileName = "profile.out";
    // private static final long serialVersionUID = 2748391675222256671L;
    public boolean canInvite;

    public static Profile parse(ApiResponse response) {
        return parse(new Profile(), response.jsonResult);
    }

    protected static Profile parse(Profile profile, JSONObject resp) {
        try {
            profile.average_rate = resp.optInt("average_rate");
            profile.money = resp.optInt("money");

            if (!(profile instanceof User)) {
                Novice.giveNoviceLikes = !resp.optBoolean("novice_likes", true);
            }

            profile.likes = resp.optInt("likes");

            profile.uid = resp.optInt("id");
            profile.age = resp.optInt("age");
            profile.sex = resp.optInt("sex");
            profile.status = normilizeStatus(resp.optString("status"));
            profile.first_name = resp.optString("first_name");
            profile.inBlackList = resp.optBoolean("in_blacklist");
            profile.city = new City(resp.optJSONObject("city"));
            profile.dating = new DatingFilter(resp.optJSONObject("dating"));
            profile.hasMail = resp.optBoolean("email");
            profile.premium = resp.optBoolean("premium");
            profile.invisible = resp.optBoolean("invisible");
            profile.background = resp.optInt("background", ProfileBackgrounds.DEFAULT_BACKGROUND_ID);
            profile.totalPhotos = resp.optInt("photos_count");
            profile.paid = resp.optBoolean("paid");
            profile.show_ad = resp.optBoolean("show_ad",true);
            profile.xstatus = resp.optInt("xstatus");
            profile.canInvite = resp.optBoolean("can_invite");
            profile.setEditor(resp.optBoolean("editor", false));

            parseGifts(profile, resp);
            parseNotifications(profile, resp);
            parseForm(profile, resp, App.getContext());
            initPhotos(resp, profile);
        } catch (Exception e) {
            Debug.error("Profile Wrong response parsing: ", e);
        }

        return profile;
    }

    private static void parseForm(Profile profile, JSONObject resp, Context context) throws JSONException {
        if (!resp.isNull("form")) {
            JSONObject form = resp.getJSONObject("form");

            FormInfo formInfo = new FormInfo(context, profile);

            FormItem headerItem;
            FormItem formItem;

            boolean isUserProfile = false;
            if (profile instanceof User) {
                isUserProfile = true;
                // ((User) profile).formMatches = form.optInt("goodness");
                ((User) profile).formMatches = 0;
            }

            // 1 HEADER -= MAIN =-
            headerItem = new FormItem(R.string.form_main, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);
            profile.forms.add(headerItem);

            if (!resp.isNull("email")) {
                profile.hasMail = resp.optBoolean("email");
            }

            if (!resp.isNull("email_grabbed")) {
                profile.email_grabbed = resp.optBoolean("email_grabbed");
            }

            if (!resp.isNull("email_confirmed")) {
                profile.email_confirmed = resp.optBoolean("email_confirmed");
            }

            // 1.2 xstatus position -1
            formItem = new FormItem(R.array.form_main_status, profile.xstatus,
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                formItem.equal = profile.xstatus == CacheProfile.xstatus;
                if (formItem.dataId > 0) {
                    profile.forms.add(formItem);
                    if (formItem.equal) {
                        ((User) profile).formMatches++;
                    }
                }
            } else {
                profile.forms.add(formItem);
            }

            // 2 character position 0
            formItem = new FormItem(R.array.form_main_character, form.optInt("character_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("character_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 3 communication position 1
            formItem = new FormItem(R.array.form_main_communication,
                    form.optInt("communication_id"), FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("communication_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 4 DIVIDER
            profile.forms.add(FormItem.getDivider());

            // 5 HEADER -= PHYSIQUE =-
            headerItem = new FormItem(R.string.form_physique, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);
            profile.forms.add(headerItem);

            // 11 breast position 7
            if (profile.sex == Static.GIRL) {
                formItem = new FormItem(R.array.form_physique_breast, form.optInt("breast_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    compareFormItemData(formItem, profile, false);
                } else {
                    profile.forms.add(formItem);
                }
            }

            // 6 fitness position 2
            formItem = new FormItem(R.array.form_physique_fitness, form.optInt("fitness_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("fitness_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // about status
            String as = form.optString("status");
            String aboutStatus = TextUtils.isEmpty(as.trim()) ? null : as;
            formItem = new FormItem(R.array.form_main_about_status, aboutStatus,
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                if (aboutStatus != null)
                    profile.forms.add(formItem);
            } else {
                profile.forms.add(formItem);
            }

            // 7 height position 3
            int h = form.optInt("height");
            String height = (h == 0) ? null : Integer.toString(form.optInt("height"));
            formItem = new FormItem(R.array.form_main_height, height, FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("height_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 8 weight position 4
            int w = form.optInt("weight");
            String weight = w == 0 ? null : Integer.toString(form.optInt("weight"));
            formItem = new FormItem(R.array.form_main_weight, weight, FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("weight_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 9 hair position 5
            formItem = new FormItem(R.array.form_physique_hairs, form.optInt("hair_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("hair_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 10 eye position 6
            formItem = new FormItem(R.array.form_physique_eyes, form.optInt("eye_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("eye_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 11 DIVIDER
            profile.forms.add(FormItem.getDivider());

            // 12 HEADER -= SOCIAL =-
            headerItem = new FormItem(R.string.form_social, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);
            profile.forms.add(headerItem);

            // 13 marriage position 7
            formItem = new FormItem(R.array.form_social_marriage, form.optInt("marriage_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("marriage_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 14 education position 8
            formItem = new FormItem(R.array.form_social_education, form.optInt("education_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("education_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 15 finances position 9
            formItem = new FormItem(R.array.form_social_finances, form.optInt("finances_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("finances_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 16 residence position 10
            formItem = new FormItem(R.array.form_social_residence, form.optInt("residence_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("residence_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 17 car vs car_id position 11
            formItem = new FormItem(R.array.form_social_car, form.optInt("car_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("car_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 18 DIVIDER
            profile.forms.add(FormItem.getDivider());

            // 19 HEADER -= HABITS =-
            headerItem = new FormItem(R.string.form_habits, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);
            profile.forms.add(headerItem);

            // 20 smoking position 12
            formItem = new FormItem(R.array.form_habits_smoking, form.optInt("smoking_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("smoking_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 21 alcohol position 13
            formItem = new FormItem(R.array.form_habits_alcohol, form.optInt("alcohol_id"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                compareFormItemData(formItem, profile,
                        form.optBoolean("alcohol_goodness", false));
            } else {
                profile.forms.add(formItem);
            }

            // 22 restaurants position 14
            String rest = form.optString("restaurants");
            String restraunts = TextUtils.isEmpty(rest.trim()) ? null : rest;
            formItem = new FormItem(R.array.form_habits_restaurants, restraunts, FormItem.DATA,
                    headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                if (restraunts != null)
                    profile.forms.add(formItem);
            } else {
                profile.forms.add(formItem);
            }

            // 23 DIVIDER
            profile.forms.add(FormItem.getDivider());

            // 24 HEADER -= DETAIL =-
            headerItem = new FormItem(R.string.form_detail, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);
            profile.forms.add(headerItem);

            // 25 first_dating position 15
            String dd = form.optString("first_dating");
            String datingDetails = TextUtils.isEmpty(dd.trim()) ? null : dd;
            formItem = new FormItem(R.array.form_detail_about_dating, datingDetails,
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                if (datingDetails != null)
                    profile.forms.add(formItem);
            } else {
                profile.forms.add(formItem);
            }

            // 26 achievements position 16
            String ach = form.optString("achievements");
            String achievments = TextUtils.isEmpty(ach.trim()) ? null : ach;
            formItem = new FormItem(R.array.form_detail_archievements, achievments,
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            if (isUserProfile) {
                if (achievments != null)
                    profile.forms.add(formItem);
            } else {
                profile.forms.add(formItem);
            }

            // 27 DIVIDER
            profile.forms.add(FormItem.getDivider());

        }
    }

    private static void parseNotifications(Profile profile, JSONObject resp) {
        if (!resp.isNull("notifications")) {
            JSONArray jsonNotifications = resp.optJSONArray("notifications");

            for (int i = 0; i < jsonNotifications.length(); i++) {
                JSONObject notification = jsonNotifications.optJSONObject(i);

                boolean mail = notification.optBoolean("mail");
                boolean apns = notification.optBoolean("apns");
                int type = notification.optInt("type");

                profile.notifications.put(type, new TopfaceNotifications(apns, mail, type));
            }

            initPhotos(resp, profile);

            if (!resp.isNull("photos_count")) {
                profile.totalPhotos = resp.optInt("photos_count");
            }
        }
    }

    private static void parseGifts(Profile profile, JSONObject resp) throws JSONException {
        JSONArray arrGifts = resp.optJSONArray("gifts");
        if (arrGifts == null) return;
        for (int i = 0; i < arrGifts.length(); i++) {
            JSONObject itemGift = arrGifts.getJSONObject(i);
            Gift gift = new Gift(
                    itemGift.optInt("gift"),
                    itemGift.optInt("id"),
                    Gift.PROFILE,
                    itemGift.optString("link")
            );
            profile.gifts.add(gift);
        }
    }

    private static void compareFormItemData(FormItem item, Profile profile,
                                            boolean matches) {
        item.equal = matches;
        if (item.dataId > 0) {
            profile.forms.add(item);
            if (item.equal) {
                ((User) profile).formMatches++;
            }
        }
    }

    public String getNameAndAge() {
        String result;
        if (first_name != null && first_name.length() > 0 && age > 0) {
            result = first_name + ", " + age;
        } else {
            result = first_name;
        }
        return result;
    }

    public void setEditor(boolean editor) {
        mEditor = editor;
    }

    public static class TopfaceNotifications {
        public boolean apns;
        public boolean mail;
        public int type;

        public TopfaceNotifications(boolean apns, boolean mail, int type) {
            this.apns = apns;
            this.mail = mail;
            this.type = type;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normilizeStatus(status);
    }

    public static String normilizeStatus(String status) {
        if (status == null) {
            return Static.EMPTY;
        }
        String result = status.trim();
        for (String EMPTY_STATUSE : EMPTY_STATUSES) {
            if (EMPTY_STATUSE.equals(result)) {
                return Static.EMPTY;
            }
        }
        return result;
    }

    public boolean isEmpty() {
        return uid <= 0;
    }

    public boolean isEditor() {
        return mEditor;
    }

}