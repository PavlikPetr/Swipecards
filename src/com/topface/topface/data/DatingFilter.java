package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Класс реализующий параметры фильтров поиска
 */
public class DatingFilter extends AbstractData implements Cloneable, Parcelable {

    public static final String DATING_ONLINE_FIELD = "dating_online";
    public City city;
    public int sex;
    public int age_start;
    public int age_end;
    public int alcohol;
    public boolean beautiful;
    public int xstatus;
    public int marriage;
    public int character;
    //Эти два параметра имеют одно и тоже значение в базе
    public int breast;
    public int finances;
    private static Boolean mOnlineDating;

    public DatingFilter() {
        super();
        city = new City();
        age_start = 16;
        age_end = 99;
    }

    public DatingFilter(JSONObject data) {
        super(data);
    }

    /**
     * Поля online нет в списке основных фильтров, т.к. оно не хранится на сервере.
     * Оно хранится в настройках и работать с ним нужно отдельно
     */
    public static boolean getOnlineField() {
        if (mOnlineDating == null) {
            mOnlineDating = PreferenceManager.getDefaultSharedPreferences(App.getContext())
                    .getBoolean(DATING_ONLINE_FIELD, false);
        }

        return mOnlineDating;
    }

    public static void setOnlineField(boolean online) {
        PreferenceManager.getDefaultSharedPreferences(App.getContext())
                .edit()
                .putBoolean(DATING_ONLINE_FIELD, online)
                .commit();

        mOnlineDating = online;
    }

    @Override
    protected void fillData(JSONObject data) {
        city = new City(data.optJSONObject("city"));
        sex = data.optInt("sex");
        age_start = data.optInt("age_start");
        age_end = data.optInt("age_end");
        alcohol = data.optInt("alcohol");
        beautiful = data.optBoolean("beautiful");
        xstatus = data.optInt("xstatus");
        breast = data.optInt("breast");
        finances = data.optInt("finances");
        marriage = data.optInt("marriage");
        character = data.optInt("character");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DatingFilter) {
            DatingFilter filter = (DatingFilter) o;

            if (filter.sex != sex) return false;
            else if (filter.age_start != age_start) return false;
            else if (filter.age_end != age_end) return false;
            else if (filter.city.id != city.id) return false;
            else if (filter.beautiful != beautiful) return false;
            else if (filter.xstatus != xstatus) return false;
            else if (filter.marriage != marriage) return false;
            else if (filter.character != character) return false;
            else if (filter.alcohol != alcohol) return false;
            else if (filter.breast != breast) return false;
            else if (filter.finances != finances) return false;

            return true;

        } else {
            return false;
        }
    }

    @Override
    public DatingFilter clone() throws CloneNotSupportedException {
        super.clone();

        DatingFilter filter = new DatingFilter();
        filter.sex = sex;
        filter.age_start = age_start;
        filter.age_end = age_end;
        filter.city = (City) city.clone();
        filter.beautiful = beautiful;
        filter.xstatus = xstatus;
        filter.marriage = marriage;
        filter.character = character;
        filter.alcohol = alcohol;
        filter.breast = breast;
        filter.finances = finances;

        return filter;
    }

    public int getShowOff() {
        return sex == Static.GIRL ? breast : finances;
    }

    /**
     * @return строка-идентификатор фильтра поиска. Применяется для кеширования
     */
    public String getFilterSignature() {
        return city.id + "#" +
                sex + "#" +
                age_start + "#" +
                age_end + "#" +
                beautiful + "#" +
                xstatus + "#" +
                marriage + "#" +
                character + "#" +
                alcohol + "#" +
                breast + "#" +
                finances + "#" +
                DatingFilter.getOnlineField();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeString(city.toJson().toString());
        } catch (JSONException e) {
            Debug.error(e);
        }
        dest.writeInt(sex);
        dest.writeInt(age_start);
        dest.writeInt(age_end);
        dest.writeInt(alcohol);
        dest.writeInt(beautiful ? 1 : 0);
        dest.writeInt(xstatus);
        dest.writeInt(marriage);
        dest.writeInt(character);
        dest.writeInt(breast);
        dest.writeInt(finances);
    }

    @SuppressWarnings({"rawtypes", "UnusedDeclaration"})
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public DatingFilter createFromParcel(Parcel in) {

                    DatingFilter result = new DatingFilter();

                    try {
                        result.city = new City(new JSONObject(in.readString()));
                    } catch (JSONException e) {
                        Debug.error(e);
                    }

                    result.sex = in.readInt();
                    result.age_start = in.readInt();
                    result.age_end = in.readInt();
                    result.alcohol = in.readInt();
                    result.beautiful = in.readInt() == 1;
                    result.xstatus = in.readInt();
                    result.marriage = in.readInt();
                    result.character = in.readInt();
                    result.breast = in.readInt();
                    result.finances = in.readInt();

                    return result;
                }

                public DatingFilter[] newArray(int size) {
                    return new DatingFilter[size];
                }
            };
}
