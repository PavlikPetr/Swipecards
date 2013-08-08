package com.topface.topface.data.search;

import android.text.TextUtils;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.SerializableToJson;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

public class UsersList<T extends FeedUser> extends LinkedList<T> implements SerializableToJson {

    /**
     * Определяет за сколько пользователей до конца списка нужно предзагружать список
     */
    public static final int USERS_FOR_PRELOAD_CNT = 6;

    /**
     * Эта константа определяет с какой периодичностью мы чистим список оценненных
     * Не путать с максимальным количеством оцененных, т.е. с {@link UsersList#RATED_USERS_CNT}
     * Данное значение говорит через сколько пользователей после превышения лимита нужно чистить список оцененных
     * Нужно что бы очищать поиск не после каждого пользователя, а через нескольких, это важно для кэша
     */
    public static final int REMOVE_RATED_BUFFER_SIZE = 6;

    /**
     * Количество оцененных пользователей которое мы храним
     */
    public static final int RATED_USERS_CNT = 8;

    protected int mPosition = 0;
    private OnUsersListEventsListener mOnEmptyListener;
    private String mSignature;
    private boolean useSignature;
    private boolean mNeedPreload = true;
    private final Class<T> mClass;

    public UsersList(Class<T> itemClass) {
        super();
        mClass = itemClass;
        useSignature = mClass == SearchUser.class;
    }

    @SuppressWarnings("unchecked")
    public UsersList(FeedListData<FeedItem> feedItems, Class<T> itemClass) {
        this(itemClass);
        for(FeedItem item : feedItems.items) {
            add((T) item.user);
        }
    }

    public UsersList(ApiResponse response, Class<T> itemClass) {
        this(itemClass);
        if (response != null) {
            parseResult(response.jsonResult);
        }
    }

    public UsersList(JSONObject jsonResult, Class<T> itemClass) {
        this(itemClass);
        parseResult(jsonResult);
    }

    @Override
    public void add(int location, T object) {
        if (!contains(object)) {
            super.add(location, object);
            int position = getSearchPosition();
            //Если позиция куда добавляется элемент меньше текущей позиции поиска
            if (position >= location) {
                //To смещаем нашу позицию поиска на 1 элемент
                setSearchPosition(getSearchPosition() + 1);
            }
        }
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        removeDublicates(collection);
        boolean result = super.addAll(location, collection);
        int position = getSearchPosition();
        if (position >= location) {
            setSearchPosition(position + collection.size());
        }

        mNeedPreload = collection.size() >= USERS_FOR_PRELOAD_CNT;

        return result;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        removeDublicates(collection);
        mNeedPreload = collection.size() >= USERS_FOR_PRELOAD_CNT;
        return super.addAll(collection);
    }

    @Override
    public boolean add(T object) {
        return !contains(object) && super.add(object);
    }

    @Override
    public void addLast(T object) {
        if (!contains(object)) {
            super.addLast(object);
        }
    }

    @Override
    public void addFirst(T object) {
        if (!contains(object)) {
            super.addFirst(object);
            //Если добавляем в начало, то указатель должен увеличиться на 1
            setSearchPosition(getSearchPosition() + 1);
        }
    }

    /**
     * Из вновь полученного списка удаляем юзеров, если они уже есть в текущем списке
     *
     * @param collection списко в который нужно добавить
     */
    private void removeDublicates(Collection<? extends T> collection) {
        LinkedList<T> needRemove = new LinkedList<T>();

        //Ищем пользователей, которые уже есть в списке
        for (T user : collection) {
            if (contains(user)) {
                needRemove.add(user);
                log(String.format("Remove dublicate user #%d %s", user.id, user.getNameAndAge()));
            }
        }
        //Удаляем их из списка на добавление
        collection.removeAll(needRemove);
    }


    public void setOnEmptyListListener(OnUsersListEventsListener listener) {
        mOnEmptyListener = listener;
        if (mOnEmptyListener != null && isEmpty()) {
            mOnEmptyListener.onEmptyList(this);
        }
    }

    private void parseResult(JSONObject jsonResult) {
        if (jsonResult != null) {
            fillList(jsonResult.optJSONArray("users"));
        }
    }


    private void fillList(JSONArray users) {
        if (users != null) {
            for (int i = 0; i < users.length(); i++) {
                try {
                    add(mClass.getConstructor(JSONObject.class).newInstance(users.optJSONObject(i)));
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        mPosition = 0;
    }

    public int getSearchPosition() {
        if (mPosition > (this.size() - 1)) {
            mPosition = this.size();
        }
        return mPosition;
    }

    public void setSearchPosition(int position) {
        mPosition = position >= 0 ? position : 0;
    }

    public T nextUser() {
        setSearchPosition(mPosition + 1);
        log("Next user #" + mPosition);
        //Удаляем лишнее количество оцененных пользователей
        removeRatedUsers(RATED_USERS_CNT);
        return getCurrentUser();
    }

    public T prevUser() {
        T user = null;
        if (mPosition > 0 && !isEmpty()) {
            setSearchPosition(mPosition - 1);
            log("Prev user #" + mPosition);
            user = getCurrentUser();
        }
        return user;
    }

    public T getCurrentUser() {
        T user = null;
        if (mPosition >= 0 && mPosition < size()) {
            user = get(mPosition);
            log(String.format("Get current user #%d %s from %s id%d", mPosition, user.getNameAndAge(), user.city.name,user.id));
        }

        if (mOnEmptyListener != null) {
            if (user == null) {
                log("Search is empty");
                //Если текущий пользователь пустой
                mOnEmptyListener.onEmptyList(this);
            } else {
                checkPreload();
            }
        }

        return user;
    }

    public boolean isEnded() {
        return mPosition >= size() - 1;
    }

    public boolean isHasRated() {
        return !isEmpty() && mPosition > 0;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONArray usersJson = new JSONArray();
        for (T user : this) {
            usersJson.put(user.toJson());
        }

        return new JSONObject().put("users", usersJson);
    }

    @SuppressWarnings("unchecked")
    public void replace(UsersList usersList) {
        clear();
        usersList = usersList != null ? usersList : new UsersList(mClass);

        log("Replace search size " + usersList.size());
        setSearchPosition(usersList.getSearchPosition());
        setSignature(usersList.getSignature());
        addAll(usersList);
    }

    private boolean isNeedPreload() {
        return mNeedPreload && size() > 0 && mPosition > size() - USERS_FOR_PRELOAD_CNT;
    }

    /**
     * Удаляет оцененных пользователей
     */
    protected boolean removeRatedUsers(int removeCnt) {
        boolean result = false;
        int searchPosition = getSearchPosition();
        if (searchPosition > removeCnt + REMOVE_RATED_BUFFER_SIZE && size() > removeCnt) {
            int deleteCnt = searchPosition - removeCnt;
            log("Remove rated from 0 to " + deleteCnt);
            try {
                removeRange(0, deleteCnt);
                setSearchPosition(searchPosition - deleteCnt);
                result = true;
            } catch (Exception e) {
                Debug.error("Remove rated exception", e);
            }
        }

        return result;
    }

    public static void log(String message) {
        Debug.log("Search:: " + message);
    }

    public String getSignature() {
        return mSignature;
    }

    public boolean setSignature(String signature) {
        if (!useSignature) return false;
        boolean result = false;
        String currentSignature = getSignature();
        if (currentSignature == null) {
            mSignature = signature;
        } else if (!TextUtils.equals(currentSignature, signature)) {
            log("Signature is changed. Clear search");
            clear();
            mSignature = signature;
            result = true;
        }

        return result;
    }

    private void checkPreload() {
        if (isNeedPreload()) {
            log(String.format("Search preload on position #%d with size %d", mPosition, size()));
            mOnEmptyListener.onPreload(this);
        }
    }

    public boolean updateSignature() {
        return useSignature && setSignature(CacheProfile.dating != null ? CacheProfile.dating.getFilterSignature() : "");
    }

    public void updateSignatureAndUpdate() {
        updateSignature();
        checkPreload();
    }

    /**
     * Добавляет пользователей в поиск, обновляя подпись фильтра
     *
     * @param usersList поиск
     */
    public void addAndUpdateSignature(UsersList usersList) {
        //noinspection unchecked
        addAll(usersList);
        updateSignature();
    }
}
