package com.topface.topface.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.http.ProfileBackgrounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

/* Класс профиля владельца устройства */
public class Profile extends AbstractDataWithPhotos {

    public final static int TYPE_OWN_PROFILE = 1;
    public final static int TYPE_USER_PROFILE = 2;
    private static String[] EMPTY_STATUSES = {Static.EMPTY, "-", "."};

    public int uid; // id пользователя в топфейсе
    public String firstName; // имя пользователя
    public int age; // возраст пользователя
    public int sex; // пол пользователя

    //Город текущего пользователя
    public City city;

    // Фильтр поиска
    public DatingFilter dating;

    // Premium
    public boolean premium;
    public boolean invisible;
    public boolean inBlackList;
    public LinkedList<FormItem> forms = new LinkedList<>();
    public Gifts gifts = new Gifts();
    public SparseArrayCompat<TopfaceNotifications> notifications = new SparseArrayCompat<>();
    public boolean email;
    public boolean emailGrabbed;
    public boolean emailConfirmed;
    public int xstatus;
    public int photosCount;
    // Идентификатор заднего фона в профиле
    public int background;
    // Платяший пользователь или нет
    public boolean paid;
    // Показывать рекламу или нет
    public boolean showAd;
    public boolean canInvite;
    protected String status; // статус пользователя
    // Флаг того, является ли пользоветль редактором
    private boolean mEditor;

    public Profile() {
        super();
    }

    public Profile(ApiResponse response) {
        this(response.getJsonResult());
    }

    public Profile(JSONObject jsonObject) {
        fillData(jsonObject);
    }

    protected void fillData(final JSONObject resp) {
        if (resp == null) {
            Debug.error(new IllegalArgumentException("JSON response for Profile is null"));
            return;
        }
        Profile profile = this;
        try {
            profile.uid = resp.optInt("id");
            profile.age = resp.optInt("age");
            profile.sex = resp.optInt("sex");
            profile.status = normilizeStatus(resp.optString("status"));
            profile.firstName = normalizeName(Utils.optString(resp, "firstName"));
            profile.city = new City(resp.optJSONObject("city"));
            profile.premium = resp.optBoolean("premium");
            profile.background = resp.optInt("bg", ProfileBackgrounds.DEFAULT_BACKGROUND_ID);
            profile.photosCount = resp.optInt("photosCount");
            profile.xstatus = resp.optInt("xstatus");

            //Дада, это ужасный косяк, когда мы наследуемся подобным способом,
            //поправим потом, с новой системой парсинга запросво
            //NOTE: Добавлять поля, нужные исключительно для профиля текущего юзера только в это условие!
            if (!(profile instanceof User)) {
                Novice.giveNoviceLikes = !resp.optBoolean("noviceLikes", true);
                profile.dating = new DatingFilter(resp.optJSONObject("dating"));
                profile.email = resp.optBoolean("email");
                profile.emailGrabbed = resp.optBoolean("emailGrabbed");
                profile.emailConfirmed = resp.optBoolean("emailConfirmed");
                profile.invisible = resp.optBoolean("invisible");
                profile.paid = resp.optBoolean("paid");
                profile.showAd = resp.optBoolean("showAd", true);
                profile.canInvite = resp.optBoolean("canInvite");

                new GCMUtils(App.getContext()).registerGCM(resp.optString("notificationToken"));
            }

            profile.setEditor(resp.optBoolean("editor", false));
            parseGifts(profile, resp);
            parseNotifications(profile, resp);
            parseForm(profile, resp, App.getContext());
            initPhotos(resp, profile);
        } catch (Exception e) {
            Debug.error("Profile Wrong response parsing: ", e);
        }
    }

    private static void parseForm(Profile profile, JSONObject resp, Context context) throws JSONException {
        if (!resp.isNull("form")) {
            JSONObject form = resp.getJSONObject("form");

            FormInfo formInfo = new FormInfo(context, profile.sex, profile.getType());

            FormItem headerItem;
            FormItem formItem;

            // 1 HEADER -= MAIN =-
            headerItem = new FormItem(R.string.form_main, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);

            // 1.2 xstatus position -1
            formItem = new FormItem(R.array.form_main_status, profile.xstatus,
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            formItem.setTextLimitInterface(new FormItem.DefaultTextLimiter());
            profile.forms.add(formItem);

            // 2 character position 0
            formItem = new FormItem(R.array.form_main_character, form.optInt("characterId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 3 communication position 1
            formItem = new FormItem(R.array.form_main_communication,
                    form.optInt("communicationId"), FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 5 HEADER -= PHYSIQUE =-
            headerItem = new FormItem(R.string.form_physique, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);

            // 11 breast position 7
            formItem = new FormItem(R.array.form_physique_breast, form.optInt("breastId"),
                    FormItem.DATA, headerItem);
            formItem.setOnlyForWomen(true);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 6 fitness position 2
            formItem = new FormItem(R.array.form_physique_fitness, form.optInt("fitnessId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // about status
            String as = form.optString("status");
            String aboutStatus = TextUtils.isEmpty(as.trim()) ? null : as;
            formItem = new FormItem(R.array.form_main_about_status, aboutStatus,
                    FormItem.DATA, headerItem);
            formItem.setTextLimitInterface(new FormItem.DefaultTextLimiter(App.getAppOptions().getUserAboutMeMaxLength()));
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 7 height position 3
            int h = form.optInt("height");
            String height = (h == 0) ? null : Integer.toString(form.optInt("height"));
            formItem = new FormItem(R.array.form_main_height, height, FormItem.DATA, headerItem);
            formItem.setValueLimitInterface(new FormItem.ValueLimitInterface() {
                @Override
                public int getMinValue() {
                    return App.getAppOptions().getUserHeightMin();
                }

                @Override
                public int getMaxValue() {
                    return App.getAppOptions().getUserHeightMax();
                }
            });
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 8 weight position 4
            int w = form.optInt("weight");
            String weight = w == 0 ? null : Integer.toString(form.optInt("weight"));
            formItem = new FormItem(R.array.form_main_weight, weight, FormItem.DATA, headerItem);
            formItem.setValueLimitInterface(new FormItem.ValueLimitInterface() {
                @Override
                public int getMinValue() {
                    return App.getAppOptions().getUserWeightMin();
                }

                @Override
                public int getMaxValue() {
                    return App.getAppOptions().getUserWeightMax();
                }
            });
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 9 hair position 5
            formItem = new FormItem(R.array.form_physique_hairs, form.optInt("hairId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 10 eye position 6
            formItem = new FormItem(R.array.form_physique_eyes, form.optInt("eyeId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 12 HEADER -= SOCIAL =-
            headerItem = new FormItem(R.string.form_social, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);

            // 13 marriage position 7
            formItem = new FormItem(R.array.form_social_marriage, form.optInt("marriageId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 14 education position 8
            formItem = new FormItem(R.array.form_social_education, form.optInt("educationId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 15 finances position 9
            formItem = new FormItem(R.array.form_social_finances, form.optInt("financesId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 16 residence position 10
            formItem = new FormItem(R.array.form_social_residence, form.optInt("residenceId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 17 car vs car_id position 11
            formItem = new FormItem(R.array.form_social_car, form.optInt("carId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 19 HEADER -= HABITS =-
            headerItem = new FormItem(R.string.form_habits, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);

            // 20 smoking position 12
            formItem = new FormItem(R.array.form_habits_smoking, form.optInt("smokingId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 21 alcohol position 13
            formItem = new FormItem(R.array.form_habits_alcohol, form.optInt("alcoholId"),
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            profile.forms.add(formItem);

            // 22 restaurants position 14
            String rest = form.optString("restaurants").trim();
            String restraunts = TextUtils.isEmpty(rest) ? null : rest;
            formItem = new FormItem(R.array.form_habits_restaurants, restraunts, FormItem.DATA,
                    headerItem);
            formInfo.fillFormItem(formItem);
            formItem.setTextLimitInterface(new FormItem.DefaultTextLimiter());
            profile.forms.add(formItem);

            // 24 HEADER -= DETAIL =-
            headerItem = new FormItem(R.string.form_detail, FormItem.HEADER);
            formInfo.fillFormItem(headerItem);

            // 25 first_dating position 15
            String dd = form.optString("firstDating").trim();
            String datingDetails = TextUtils.isEmpty(dd) ? null : dd;
            formItem = new FormItem(R.array.form_detail_about_dating, datingDetails,
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            formItem.setTextLimitInterface(new FormItem.DefaultTextLimiter());
            profile.forms.add(formItem);

            // 26 achievements position 16
            String ach = form.optString("achievements").trim();
            String achievments = TextUtils.isEmpty(ach) ? null : ach;
            formItem = new FormItem(R.array.form_detail_archievements, achievments,
                    FormItem.DATA, headerItem);
            formInfo.fillFormItem(formItem);
            formItem.setTextLimitInterface(new FormItem.DefaultTextLimiter());
            profile.forms.add(formItem);
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
        }
    }

    private static void parseGifts(Profile profile, JSONObject resp) throws JSONException {
        JSONObject jsonGifts = resp.optJSONObject("gifts");
        JSONArray arrGifts = jsonGifts.optJSONArray("items");
        profile.gifts.more = jsonGifts.optBoolean("more");
        profile.gifts.count = jsonGifts.optInt("count", -1);
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

    public static String normilizeStatus(String status) {
        if (status == null) {
            return Static.EMPTY;
        }
        String result = status.replaceAll("\n", " ").trim();
        for (String EMPTY_STATUS : EMPTY_STATUSES) {
            if (EMPTY_STATUS.equals(result)) {
                return Static.EMPTY;
            }
        }
        return result;
    }

    public static String normalizeName(String name) {
        if (name == null) {
            return Static.EMPTY;
        }
        return name.replaceAll("\n", " ").trim();
    }

    public String getNameAndAge() {
        return Utils.getNameAndAge(firstName, age);
    }

    public int getType() {
        return (this instanceof User) ? TYPE_USER_PROFILE : TYPE_OWN_PROFILE;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normilizeStatus(status);
    }

    public boolean isEmpty() {
        return uid <= 0;
    }

    public boolean isEditor() {
        return mEditor;
    }

    public void setEditor(boolean editor) {
        mEditor = editor;
    }

    /**
     *
     */
    public static class TopfaceNotifications implements Parcelable {

        public static final Parcelable.Creator<TopfaceNotifications> CREATOR = new Creator<TopfaceNotifications>() {
            @Override
            public TopfaceNotifications createFromParcel(Parcel source) {
                return new TopfaceNotifications(source);
            }

            @Override
            public TopfaceNotifications[] newArray(int size) {
                return new TopfaceNotifications[size];
            }
        };

        public boolean apns;
        public boolean mail;
        public int type;

        public TopfaceNotifications(boolean apns, boolean mail, int type) {
            this.apns = apns;
            this.mail = mail;
            this.type = type;
        }

        protected TopfaceNotifications(Parcel in) {
            apns = in.readByte() == 1;
            mail = in.readByte() == 1;
            type = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (apns ? 1 : 0));
            dest.writeByte((byte) (mail ? 1 : 0));
            dest.writeInt(type);
        }
    }

    public static class Gifts extends ArrayList<Gift> {
        public boolean more;
        public int count;

        public Gifts() {
            more = false;
        }
    }
}
