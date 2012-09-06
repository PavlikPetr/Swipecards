package com.topface.topface.data;

import java.util.LinkedHashMap;
import org.json.JSONObject;
import android.content.Context;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.Triple;

/* Класс профиля владельца устройства */
public class Profile extends AbstractDataWithPhotos {
    // Data
    public int uid; // id пользователя в топфейсе
    public String first_name; // имя пользователя
    public int age; // возраст пользователя
    public int sex; // секс пользователя
    
    // Unread
    public int unread_rates; // количество непрочитанных оценок пользователя
    public int unread_likes; // количество непрочитанных “понравилось” пользователя
    public int unread_messages; // количество непрочитанных сообщений пользователя
    public int unread_mutual; // количество непрочитанных симпатий
    
//    public String avatar_big; // аватарка пользователя большого размера
//    public String avatar_small; // аватарки пользователя маленького размера
    
    // City
    public int city_id; // идентификтаор города пользователя
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя
    
    // Resources
    public int money; // количество монет у пользователя
    public int power; // количество энергии пользователя
    public int average_rate; // средняя оценка текущего пользователя
    
    // Form
//    public int form_job_id; // идентификатор рабочей партии пользователя
//    public String form_job; // описание оригинальной работы пользователя
//    public int form_status_id; // идентификатор предопределенного статуса пользователя
//    public String form_status; // описание оригинального статуса пользователя
//    public int form_education_id; // идентификатор предопределенного уровня образования пользователя
//    public int form_marriage_id; // идентификатор предопределенного семейного положения пользователя
//    public int form_finances_id; // идентификатор предопределенного финансового положения пользователя
//    public int form_character_id; // идентификатор предопределенной характеристики пользователя
//    public int form_smoking_id; // идентификатор предопределенного отношения к курению пользователя
//    public int form_alcohol_id; // идентификатор предопределенного отношения к алкоголю пользователя
//    public int form_fitness_id; // идентификатор предопределенного отношения к спорту пользователя
//    public int form_communication_id; // идентификатор предопределенного отношения к коммуникациям пользователя
//    public int form_weight; // вес пользователя
//    public int form_height; // рост пользователя
//    public int form_hair_id; // идентификатор цвета воло пользователя
//    public int form_eye_id; // идентификатор цвета глаз пользователя
//    public int form_children_id; // идентификатор количества детей пользователя
//    public int form_residence_id; // идентификатор условий проживания пользователя
//    public int form_car_id; // идентификатор наличия автомобиля у пользователя
//    public String form_car; // текстовое описание присутствующего автомобиля у пользователя
//    public String form_first_dating; // текстовое описание свидания пользователя
//    public String form_achievements; // текстовое описание достижений пользователя
//    //{Array} form_countries; // массив идентификаторов стран, в которых бывал пользователь
//    public String form_restaurants; // описание предпочитаемых ресторанов пользователя
//    public String form_valuables; // описание ценностей пользователя
//    public String form_aspirations; // описание достижений пользователя
    
    // Dating
    public int dating_sex; // пол пользователей для поиска
    public int dating_age_start; // начальный возраст для пользователей
    public int dating_age_end; // конечный возраст для пользователей
    public int dating_city_id; // идентификатор города для поиска пользователей
    public String dating_city_name; // наименование пользователя в русской локали
    public String dating_city_full; // полное наименование города
    
    public String status; // статус пользователя
    
    public LinkedHashMap<String, Triple<String, String, Boolean>> forms = new LinkedHashMap<String, Triple<String, String, Boolean>>();
    
    public static Profile parse(ApiResponse response) {
        return parse(response.mJSONResult);
    }
    protected static Profile parse(JSONObject resp) {
        Profile profile = new Profile();

        try {
            profile.unread_rates = resp.optInt("unread_rates");
            profile.unread_likes = resp.optInt("unread_likes");
            profile.unread_messages = resp.optInt("unread_messages");
            profile.unread_mutual = resp.optInt("unread_symphaties");
            profile.average_rate = resp.optInt("average_rate");
            profile.money = resp.optInt("money");

            int power = resp.optInt("power");
            //if(power > 10000) power = 10000;
            profile.power = (int)(power * 0.01);

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

            // avatars
//            if (!resp.isNull("avatars")) {
//                JSONObject avatars = resp.getJSONObject("avatars");
//                profile.avatar_big = avatars.optString("big");
//                profile.avatar_small = avatars.optString("small");
//            }

            // albums
//              if (!resp.isNull("album")) {
//                JSONArray albums = resp.getJSONArray("album");
//                profile.albums = new LinkedList<Album>();
//                if (albums.length() > 0)
//                    for (int i = 0; i < albums.length(); i++) {
//                        JSONObject item = albums.getJSONObject(i);
//                        Album album = new Album();
//                        album.id = item.optInt("id");
//                        album.small = item.optString("small");
//                        album.big = item.optString("big");
//                        if (!item.isNull("ero")) {
//                            album.ero = true;
//                            album.buy = item.optBoolean("buy");
//                            album.cost = item.optInt("cost");
//                            album.likes = item.optInt("likes");
//                            album.dislikes = item.optInt("dislikes");
//                        } else
//                            album.ero = false;
//                        profile.albums.add(album);
//                    }
//            }

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

            Context context = App.getContext();
            // questionary
            if (!resp.isNull("questionary")) {
                JSONObject form = resp.getJSONObject("form");
                
                FormInfo formInfo = new FormInfo(context, profile);
                
                profile.forms.put(context.getString(R.string.s_profile_job_id), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_job), 
                                                            formInfo.getFinances(form.optInt("job_id")), 
                                                            null));
                        
                profile.forms.put(context.getString(R.string.s_profile_job), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_job), 
                                                            formInfo.getFinances(form.optInt("job")), 
                                                            null));

                profile.forms.put(context.getString(R.string.s_profile_status_id), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_status), 
                                                            formInfo.getFinances(form.optInt("status_id")), 
                                                            null));
                                                
                profile.forms.put(context.getString(R.string.s_profile_status), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_status), 
                                                            formInfo.getFinances(form.optInt("status")), 
                                                            null));
                
                
                profile.forms.put(context.getString(R.string.s_profile_education), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_education), 
                                                            formInfo.getFinances(form.optInt("education_id")), 
                                                            null));
                int marriage_id = profile.sex==Static.GIRL ? R.string.profile_marriage_female : R.string.profile_marriage_male ;
                profile.forms.put(context.getString(R.string.s_profile_marriage), 
                        new Triple<String, String, Boolean>(context.getString(marriage_id), 
                                                            formInfo.getFinances(form.optInt("marriage_id")), 
                                                            null));

                profile.forms.put(context.getString(R.string.s_profile_finances), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_finances), 
                                                            formInfo.getFinances(form.optInt("finances_id")), 
                                                            null));
                
                profile.forms.put(context.getString(R.string.s_profile_character), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_character), 
                                                            formInfo.getFinances(form.optInt("character_id")), 
                                                            null));

                profile.forms.put(context.getString(R.string.s_profile_smoking), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_smoking), 
                                                            formInfo.getFinances(form.optInt("smoking_id")), 
                                                            null));
                
                profile.forms.put(context.getString(R.string.s_profile_alcohol), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_alcohol), 
                                                            formInfo.getFinances(form.optInt("alcohol_id")), 
                                                            null));
                
                profile.forms.put(context.getString(R.string.s_profile_fitness), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_fitness), 
                                                            formInfo.getFinances(form.optInt("fitness_id")), 
                                                            null));
                
                profile.forms.put(context.getString(R.string.s_profile_communication), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_commutability), 
                                                            formInfo.getFinances(form.optInt("communication_id")), 
                                                            null));
                
                profile.forms.put(context.getString(R.string.s_profile_weight), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_weight), 
                                                            formInfo.getFinances(form.optInt("weight")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_height), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("height")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_hair), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("hair_id")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_eye), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("eye_id")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_children), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("children_id")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_residence), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("residence_id")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_car_id), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("car_id")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_car), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("car")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_first_dating), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("first_dating")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_achievements), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_height), 
                                                            formInfo.getFinances(form.optInt("achievements")), 
                                                            null));

                //{Array} form_countries; // массив идентификаторов стран, в которых бывал пользователь
                
                profile.forms.put(context.getString(R.string.s_profile_restaurants), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_finances), 
                                                            formInfo.getFinances(form.optInt("restaurants")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_valuables), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_finances), 
                                                            formInfo.getFinances(form.optInt("valuables")), 
                                                            null));
                profile.forms.put(context.getString(R.string.s_profile_aspirations), 
                        new Triple<String, String, Boolean>(context.getString(R.string.profile_finances), 
                                                            formInfo.getFinances(form.optInt("aspirations")), 
                                                            null));
            }
            
            initPhotos(resp, profile);

            // newbie
//            if (!resp.isNull("flags")) {
//                JSONArray flags = resp.getJSONArray("flags");
//                for (int i = 0; i < flags.length(); i++) {
//                    profile.isNewbie = true;
//                    String item = flags.getString(i);
//                    if (item.equals("NOVICE_ENERGY")) {
//                        profile.isNewbie = false;
//                        break;
//                    }
//                }
//            }
        } catch(Exception e) {
            Debug.log("Profile.class", "Wrong response parsing: " + e);
        }

        return profile;
    }

    public int getUid() {
        return uid;
    };

    @Override
    public String getLargeLink() {
        return null;
    }

    @Override
    public String getSmallLink() {
        return null;
    }    
}

// "ADMIN_MESSAGE","QUESTIONARY_FILLED","CHANGE_PHOTO","STANDALONE_BONUS","STANDALONE",
// "GUARDBIT","ADMIN_MESSAGES_WITH_ID","IS_TOPFACE_MEMBER","IS_LICE_MER_MEMBER","MAXSTATS_WATCHED",
// "MAXSTATS_CHECKED","MESSAGES_FEW","MESSAGES_MANY","GIFTS_NO","GIFTS_FEW","GIFTS_MANY","ACTIVE",
// "SEXUALITY_FIRST_SEND","FACEBOOK_VIRUS_ACTION_OLD","FACEBOOK_VIRUS_ACTION_OLD_2",
// "FACEBOOK_VIRUS_ACTION_OLD_3","FACEBOOK_VIRUS_ACTION_OLD_4","FACEBOOK_VIRUS_ACTION_OLD_5",
// "FACEBOOK_VIRUS_ACTION_OLD_6","FRIENDS_DUMPED","MY_FRIENDS_CANNOT_SEE_ME","FACEBOOK_VIRUS_ACTION",
// "HAS_RESET_SEXUALITY","HAS_RESET_SEXUALITY_2","IN_SEARCH","SHOW_NEWDESIGN_TIPS","MOBILE_USER",
// "PHONE_APP_USED","NOVICE_BONUS_SHOW","PHONE_APP_ADMSG_RECEIVED"