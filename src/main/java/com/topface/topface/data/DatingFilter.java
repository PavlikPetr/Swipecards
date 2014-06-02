package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Класс реализующий параметры фильтров поиска
 */
public class DatingFilter extends AbstractData implements Cloneable, Parcelable {

    public static final String DATING_ONLY_ONLINE_FIELD = "datingOnline";

    public static final int MAX_AGE = 99;
    public static final int MIN_AGE = 16;

    public City city = new City();
    public int sex;
    public int ageStart;
    public int ageEnd;
    public int alcohol;
    public boolean beautiful;
    public int xstatus;
    public int marriage;
    public int character;
    //Эти два параметра имеют одно и тоже значение в базе
    public int breast;
    public int finances;
    private static Boolean mOnlyOnlineDating;

    public DatingFilter() {
        city = new City();
        ageStart = MIN_AGE;
        ageEnd = MAX_AGE;
    }

    public DatingFilter(JSONObject data) {
        if (data != null) {
            fillData(data);
        }
    }

    /**
     * Поля online нет в списке основных фильтров, т.к. оно не хранится на сервере.
     * Оно хранится в настройках и работать с ним нужно отдельно
     */
    public static boolean getOnlyOnlineField() {
        if (mOnlyOnlineDating == null) {
            mOnlyOnlineDating = PreferenceManager.getDefaultSharedPreferences(App.getContext())
                    .getBoolean(DATING_ONLY_ONLINE_FIELD, false);
        }

        return mOnlyOnlineDating;
    }

    public static void setOnlyOnlineField(boolean onlyOnline) {
        PreferenceManager.getDefaultSharedPreferences(App.getContext())
                .edit()
                .putBoolean(DATING_ONLY_ONLINE_FIELD, onlyOnline)
                .commit();

        mOnlyOnlineDating = onlyOnline;
    }

    protected void fillData(JSONObject data) {
        city = new City(data.optJSONObject("city"));
        sex = data.optInt("sex");
        ageStart = data.optInt("ageStart");
        ageEnd = data.optInt("ageEnd");
        trimToMinMaxAge();
        alcohol = data.optInt("alcohol");
        beautiful = data.optBoolean("beautiful");
        xstatus = data.optInt("xstatus");
        breast = data.optInt("breast");
        finances = data.optInt("finances");
        marriage = data.optInt("marriage");
        character = data.optInt("character");
    }

    private void trimToMinMaxAge() {
        if (ageStart < MIN_AGE) {
            ageStart = MIN_AGE;
        }

        if (ageEnd > MAX_AGE) {
            ageEnd = MAX_AGE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DatingFilter) {
            DatingFilter filter = (DatingFilter) o;

            if (filter.sex != sex) return false;
            else if (filter.ageStart != ageStart) return false;
            else if (filter.ageEnd != ageEnd) return false;
            else if (filter.city.id != city.id) return false;
            else if (filter.beautiful != beautiful) return false;
            else if (filter.xstatus != xstatus) return false;
            else if (filter.marriage != marriage) return false;
            else if (filter.character != character) return false;
            else if (filter.alcohol != alcohol) return false;
            else if (filter.breast != breast && filter.sex == Static.GIRL) return false;
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
        filter.ageStart = ageStart;
        filter.ageEnd = ageEnd;
        filter.trimToMinMaxAge();
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

    /**
     * @return строка-идентификатор фильтра поиска. Применяется для кеширования
     */
    public String getFilterSignature() {
        return city.id + "#" +
                sex + "#" +
                ageStart + "#" +
                ageEnd + "#" +
                beautiful + "#" +
                xstatus + "#" +
                marriage + "#" +
                character + "#" +
                alcohol + "#" +
                breast + "#" +
                finances + "#" +
                DatingFilter.getOnlyOnlineField();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String cityJson = null;
        try {
            cityJson = city.toJson().toString();
        } catch (JSONException e) {
            Debug.error(e);
        }
        dest.writeString(cityJson);
        dest.writeInt(sex);
        dest.writeInt(ageStart);
        dest.writeInt(ageEnd);
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
                    result.ageStart = in.readInt();
                    result.ageEnd = in.readInt();
                    result.trimToMinMaxAge();
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
