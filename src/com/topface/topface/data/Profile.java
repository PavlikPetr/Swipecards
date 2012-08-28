package com.topface.topface.data;

import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

/* Класс профиля владельца устройства */
public class Profile extends AbstractDataWithPhotos {
    // Data
    public int uid; // id пользователя в топфейсе
    public String first_name; // имя пользователя
    public int age; // возраст пользователя
    public int sex; // секс пользователя
    public int unread_rates; // количество непрочитанных оценок пользователя
    public int unread_likes; // количество непрочитанных “понравилось” пользователя
    public int unread_messages; // количество непрочитанных сообщений пользователя
    public int unread_symphaties; // количество непрочитанных симпатий
//    public String avatar_big; // аватарка пользователя большого размера
//    public String avatar_small; // аватарки пользователя маленького размера
    public int city_id; // идентификтаор города пользователя
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя
    public int money; // количество монет у пользователя
    public int power; // количество энергии пользователя
    public int average_rate; // средняя оценка текущего пользователя

    // Form
    public int form_job_id; // идентификатор рабочей партии пользователя
    public String form_job; // описание оригинальной работы пользователя
    public int form_status_id; // идентификатор предопределенного статуса пользователя
    public String form_status; // описание оригинального статуса пользователя
    public int form_education_id; // идентификатор предопределенного уровня образования пользователя
    public int form_marriage_id; // идентификатор предопределенного семейного положения пользователя
    public int form_finances_id; // идентификатор предопределенного финансового положения пользователя
    public int form_character_id; // идентификатор предопределенной характеристики пользователя
    public int form_smoking_id; // идентификатор предопределенного отношения к курению пользователя
    public int form_alcohol_id; // идентификатор предопределенного отношения к алкоголю пользователя
    public int form_fitness_id; // идентификатор предопределенного отношения к спорту пользователя
    public int form_communication_id; // идентификатор предопределенного отношения к коммуникациям пользователя
    public int form_weight; // вес пользователя
    public int form_height; // рост пользователя
    public int form_hair_id; // идентификатор цвета воло пользователя
    public int form_eye_id; // идентификатор цвета глаз пользователя
    public int form_children_id; // идентификатор количества детей пользователя
    public int form_residence_id; // идентификатор условий проживания пользователя
    public int form_car_id; // идентификатор наличия автомобиля у пользователя
    public String form_car; // текстовое описание присутствующего автомобиля у пользователя
    public String form_first_dating; // текстовое описание свидания пользователя
    public String form_achievements; // текстовое описание достижений пользователя
    //{Array} form_countries; // массив идентификаторов стран, в которых бывал пользователь
    public String form_restaurants; // описание предпочитаемых ресторанов пользователя
    public String form_valuables; // описание ценностей пользователя
    public String form_aspirations; // описание достижений пользователя
    
    // Dating
    public int dating_sex; // пол пользователей для поиска
    public int dating_age_start; // начальный возраст для пользователей
    public int dating_age_end; // конечный возраст для пользователей
    public int dating_city_id; // идентификатор города для поиска пользователей
    public String dating_city_name; // наименование пользователя в русской локали
    public String dating_city_full; // полное наименование города
    
    public String status; // статус пользователя

    public static Profile parse(ApiResponse response) {
        Profile profile = new Profile();

        try {
            JSONObject resp = response.mJSONResult;
            profile.unread_rates = resp.optInt("unread_rates");
            profile.unread_likes = resp.optInt("unread_likes");
            profile.unread_messages = resp.optInt("unread_messages");
            profile.unread_symphaties = resp.optInt("unread_symphaties");
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

            // questionary
            if (!resp.isNull("questionary")) {
                JSONObject form = resp.getJSONObject("form");
                profile.form_job_id = form.optInt("job_id");
                profile.form_job = form.optString("job");
                profile.form_status_id = form.optInt("status_id");
                profile.form_status = form.optString("status");
                profile.form_education_id = form.optInt("education_id");
                profile.form_marriage_id = form.optInt("marriage_id");
                profile.form_finances_id = form.optInt("finances_id");
                profile.form_character_id = form.optInt("character_id");
                profile.form_smoking_id = form.optInt("smoking_id");
                profile.form_alcohol_id = form.optInt("alcohol_id");
                profile.form_fitness_id = form.optInt("fitness_id");
                profile.form_communication_id = form.optInt("communication_id");
                profile.form_weight = form.optInt("weight");
                profile.form_height = form.optInt("height");
                profile.form_hair_id = form.optInt("hair_id");
                profile.form_eye_id = form.optInt("eye_id");
                profile.form_children_id = form.optInt("children_id");
                profile.form_residence_id = form.optInt("residence_id");
                profile.form_car_id = form.optInt("car_id");
                profile.form_car = form.optString("car");
                profile.form_first_dating = form.optString("first_dating");
                profile.form_achievements = form.optString("achievements");
                //{Array} form_countries; // массив идентификаторов стран, в которых бывал пользователь
                profile.form_restaurants = form.optString("restaurants");
                profile.form_valuables = form.optString("valuables");
                profile.form_aspirations = form.optString("aspirations");
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