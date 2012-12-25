package com.topface.topface.data;

import android.content.Context;
import android.text.TextUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.http.ProfileBackgrounds;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/* Класс профиля владельца устройства */
public class Profile extends AbstractDataWithPhotos {

    public int uid; // id пользователя в топфейсе
    public String first_name; // имя пользователя
    public int age; // возраст пользователя
    public int sex; // пол пользователя

    // Unread
    public int unread_rates; // количество непрочитанных оценок пользователя
    public int unread_likes; // количество непрочитанных “понравилось”
    // пользователя
    public int unread_messages; // количество непрочитанных сообщений
    // пользователя
    public int unread_mutual; // количество непрочитанных симпатий
    public int unread_visitors; // количество непрочитанных гостей

    // City
    public int city_id; // идентификтаор города пользователя
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя

    // Resources
    public int money; // количество монет у пользователя
    public int likes; // количество энергии пользователя

    public int average_rate; // средняя оценка текущего пользователя

    // Dating
    public int dating_sex; // пол пользователей для поиска
    public int dating_age_start; // начальный возраст для пользователей
    public int dating_age_end; // конечный возраст для пользователей
    public int dating_city_id; // идентификатор города для поиска пользователей
    public String dating_city_name; // наименование пользователя в русской
    // локали
    public String dating_city_full; // полное наименование города

    // Premium
    public boolean premium;
    public boolean invisible;
    public boolean inBlackList;

    public String status; // статус пользователя

    public LinkedList<FormItem> forms = new LinkedList<FormItem>();

    public ArrayList<Gift> gifts = new ArrayList<Gift>();
    public HashMap<Integer, TopfaceNotifications> notifications = new HashMap<Integer, TopfaceNotifications>();
    public boolean hasMail;

    public int background;

    // private static final String profileFileName = "profile.out";
    // private static final long serialVersionUID = 2748391675222256671L;

    public static Profile parse(ApiResponse response) {
        return parse(new Profile(), response.jsonResult);
    }

    protected static Profile parse(Profile profile, JSONObject resp) {
        try {
            profile.unread_rates = resp.optInt("unread_rates");
            profile.unread_likes = resp.optInt("unread_likes");
            profile.unread_messages = resp.optInt("unread_messages");
            profile.unread_mutual = resp.optInt("unread_symphaties");
            profile.unread_visitors = resp.optInt("unread_visitors");
            profile.average_rate = resp.optInt("average_rate");
            profile.money = resp.optInt("money");
            Novice.giveNoviceLikes = !resp.optBoolean("novice_likes",true);

            profile.likes = resp.optInt("likes");

            profile.uid = resp.optInt("id");
            profile.age = resp.optInt("age");
            profile.sex = resp.optInt("sex");
            profile.status = resp.optString("status");
            profile.first_name = resp.optString("first_name");

            if(!resp.isNull("in_blacklist")) {
                profile.inBlackList = resp.getBoolean("in_blacklist");
            }

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

            // gifts
            JSONArray arrGifts = resp.optJSONArray("gifts");
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

            if (!resp.isNull("notifications")) {
                JSONArray jsonNotifications = resp.optJSONArray("notifications");

                for (int i = 0; i < jsonNotifications.length(); i++) {
                    JSONObject notification = jsonNotifications.getJSONObject(i);

                    boolean mail = notification.optBoolean("mail");
                    boolean apns = notification.optBoolean("apns");
                    int type = notification.optInt("type");

                    profile.notifications.put(type, new TopfaceNotifications(apns, mail, type));
                }
            }

            if (!resp.isNull("email")) {
                profile.hasMail = resp.optBoolean("email");
            }

            if (!resp.isNull("premium")) {
                profile.premium = resp.optBoolean("premium", false);
            }

            if (!resp.isNull("invisible")) {
                profile.invisible = resp.optBoolean("invisible", false);
            }

            profile.background = resp
                    .optInt("background", ProfileBackgrounds.DEFAULT_BACKGROUND_ID);

            Context context = App.getContext();

            // form
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

                int position = 0;

//				formItem = new FormItem(R.array.form_main_status, form.optInt("status_id"),
//						FormItem.DATA, headerItem);
//				formInfo.fillFormItem(formItem);
//				if (mIsUserProfile) {
//					position++;
//					compareFormItemData(formItem, position, profile,
//							form.optBoolean("status_goodness", false));
//				} else {
//					profile.forms.add(formItem);
//				}

                // personal status
                String status = profile.status;
                if (status != null) {
                    if (isUserProfile && status.trim().length() == 0) {
                        status = null;
                    }
                }
                formItem = new FormItem(R.array.form_main_personal_status, status,
                        isUserProfile ? FormItem.DATA : FormItem.STATUS, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    if (status != null)
                        profile.forms.add(formItem);
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

                // 2 character position 0
                formItem = new FormItem(R.array.form_main_character, form.optInt("character_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("character_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 3 communication position 1
                formItem = new FormItem(R.array.form_main_communication,
                        form.optInt("communication_id"), FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
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

                // 6 fitness position 2
                formItem = new FormItem(R.array.form_physique_fitness, form.optInt("fitness_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("fitness_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 7 height position 3
                int h = form.optInt("height");
                String height = (h == 0) ? null : Integer.toString(form.optInt("height"));
                formItem = new FormItem(R.array.form_main_height, height, FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
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
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("weight_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 9 hair position 5
                formItem = new FormItem(R.array.form_physique_hairs, form.optInt("hair_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("hair_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 10 eye position 6
                formItem = new FormItem(R.array.form_physique_eyes, form.optInt("eye_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
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
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("marriage_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 14 education position 8
                formItem = new FormItem(R.array.form_social_education, form.optInt("education_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("education_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 15 finances position 9
                formItem = new FormItem(R.array.form_social_finances, form.optInt("finances_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("finances_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 16 residence position 10
                formItem = new FormItem(R.array.form_social_residence, form.optInt("residence_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("residence_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 17 car vs car_id position 11
                formItem = new FormItem(R.array.form_social_car, form.optInt("car_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
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
                    position++;
                    compareFormItemData(formItem, position, profile,
                            form.optBoolean("smoking_goodness", false));
                } else {
                    profile.forms.add(formItem);
                }

                // 21 alcohol position 13
                formItem = new FormItem(R.array.form_habits_alcohol, form.optInt("alcohol_id"),
                        FormItem.DATA, headerItem);
                formInfo.fillFormItem(formItem);
                if (isUserProfile) {
                    position++;
                    compareFormItemData(formItem, position, profile,
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

                // header -= ????????? =-
                // formItem = new FormItem();
                // formItem.type = FormItem.HEADER;
                // formItem.title = "?????????";
                // formItem.value = Static.EMPTY;
                // formItem.equal = false;
                // profile.forms.add(formItem);
                //
                // // job vs job_id
                // formItem = new FormItem();
                // formItem.type = FormItem.DATA;
                // formItem.title = "job";
                // formItem.value = formInfo.getJob(form.optInt("job"));
                // formItem.equal = false;
                // profile.forms.add(formItem);
                //
                // // status vs status_id
                // formItem = new FormItem();
                // formItem.type = FormItem.DATA;
                // formItem.title = "status";
                // formItem.value = formInfo.getJob(form.optInt("status"));
                // formItem.equal = false;
                // profile.forms.add(formItem);
                //
                // // children
                // formItem = new FormItem();
                // formItem.type = FormItem.DATA;
                // formItem.title = "children";
                // formItem.value = "" + form.optInt("children_id");
                // formItem.equal = false;
                // profile.forms.add(formItem);
                //
                // // form_countries
                // //{Array} form_countries; // массив идентификаторов стран, в
                // которых бывал пользователь
                //
                // // valuables
                // formItem = new FormItem();
                // formItem.type = FormItem.DATA;
                // formItem.title = "valuables";
                // formItem.value = "" + form.optInt("valuables");
                // formItem.equal = false;
                // profile.forms.add(formItem);
                //
                // // aspirations
                // formItem = new FormItem();
                // formItem.type = FormItem.DATA;
                // formItem.title = "aspirations";
                // formItem.value = "" + form.optInt("aspirations");
                // formItem.equal = false;
                // profile.forms.add(formItem);
            }

            initPhotos(resp, profile);

            // newbie
            // if (!resp.isNull("flags")) {
            // JSONArray flags = resp.getJSONArray("flags");
            // for (int i = 0; i < flags.length(); i++) {
            // profile.isNewbie = true;
            // String item = flags.getString(i);
            // if (item.equals("NOVICE_ENERGY")) {
            // profile.isNewbie = false;
            // break;
            // }
            // }
            // }
        } catch (Exception e) {
            Debug.log("Profile.class", "Wrong response parsing: " + e);
        }

        return profile;
    }

    // private static void compareFormItemData(FormItem item, int position,
    // Profile profile) {
    // if (item.dataId > 0) {
    // if (item.dataId == CacheProfile.forms.get(position).dataId)
    // item.equal = true;
    // profile.forms.add(item);
    // }
    // }

    private static void compareFormItemData(FormItem item, int position, Profile profile,
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
            result = String.format("%s, %d", first_name, age);
        } else {
            result = first_name;
        }
        return result;
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
    // public static Profile load() {
    // Profile profile = null;
    // ObjectInputStream oin = null;
    // try {
    // oin = new
    // ObjectInputStream(App.getContext().openFileInput(profileFileName));
    // profile = (Profile)oin.readObject();
    // } catch(Exception e) {
    // Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT).show();
    // } finally {
    // try {
    // if(oin != null) oin.close();
    // } catch(IOException e) {}
    // }
    // return profile;
    // }

    // public static Profile load() {
    // BufferedReader br = null;
    // StringBuilder sb = new StringBuilder();
    // Profile profile = new Profile();
    // try {
    // br = new BufferedReader(new
    // InputStreamReader(App.getContext().openFileInput(profileFileName)));
    // if (br != null)
    // for (String line = br.readLine(); line != null; line = br.readLine())
    // sb.append(line);
    // } catch(Exception e) {
    // Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT).show();
    // } finally {
    // try {
    // if(br != null) br.close();
    // } catch(IOException e) {
    // Debug.error(e);
    // }
    // }
    // JSONObject json;
    // try {
    // json = new JSONObject(sb.toString());
    // } catch(JSONException e) {
    // json = new JSONObject();
    // }
    // return Profile.parse(profile, json);
    // }

    // public static void save(final Profile profile) {
    // new Thread(new Runnable() {
    // @Override
    // public void run() {
    // ObjectOutputStream oos = null;
    // try {
    // oos = new
    // ObjectOutputStream(App.getContext().openFileOutput(profileFileName,
    // Context.MODE_PRIVATE));
    // oos.writeObject(profile);
    // oos.flush();
    // } catch(Exception e) {
    // Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT).show();
    // } finally {
    // try {
    // if(oos != null) oos.close();
    // } catch(IOException e) {}
    // }
    // }
    // }).start();
    // }

    // public static void save(final String response) {
    // new Thread(new Runnable() {
    // @Override
    // public void run() {
    // BufferedOutputStream bos = null;
    // try {
    // bos = new
    // BufferedOutputStream(App.getContext().openFileOutput(profileFileName,
    // Context.MODE_PRIVATE));
    // bos.write(response.getBytes("UTF8"));
    // bos.flush();
    // } catch(Exception e) {
    // Toast.makeText(App.getContext(), "", Toast.LENGTH_SHORT).show();
    // } finally {
    // try {
    // if(bos != null) bos.close();
    // } catch(IOException e) {
    // Debug.error(e);
    // }
    // }
    // }
    // }).start();
    // }

    // public static boolean isProfileExist() {
    // File file = new File(App.getContext().getFilesDir(), profileFileName);
    // return file.exists();
    // }
    //
    // public static void deleteProfile() {
    // File file = new File(App.getContext().getFilesDir(), profileFileName);
    // if(file.exists()) {
    // file.delete();
    // }
    // }
}

// "ADMIN_MESSAGE","QUESTIONARY_FILLED","CHANGE_PHOTO","STANDALONE_BONUS","STANDALONE",
// "GUARDBIT","ADMIN_MESSAGES_WITH_ID","IS_TOPFACE_MEMBER","IS_LICE_MER_MEMBER","MAXSTATS_WATCHED",
// "MAXSTATS_CHECKED","MESSAGES_FEW","MESSAGES_MANY","GIFTS_NO","GIFTS_FEW","GIFTS_MANY","ACTIVE",
// "SEXUALITY_FIRST_SEND","FACEBOOK_VIRUS_ACTION_OLD","FACEBOOK_VIRUS_ACTION_OLD_2",
// "FACEBOOK_VIRUS_ACTION_OLD_3","FACEBOOK_VIRUS_ACTION_OLD_4","FACEBOOK_VIRUS_ACTION_OLD_5",
// "FACEBOOK_VIRUS_ACTION_OLD_6","FRIENDS_DUMPED","MY_FRIENDS_CANNOT_SEE_ME","FACEBOOK_VIRUS_ACTION",
// "HAS_RESET_SEXUALITY","HAS_RESET_SEXUALITY_2","IN_SEARCH","SHOW_NEWDESIGN_TIPS","MOBILE_USER",
// "PHONE_APP_USED","NOVICE_BONUS_SHOW","PHONE_APP_ADMSG_RECEIVED"