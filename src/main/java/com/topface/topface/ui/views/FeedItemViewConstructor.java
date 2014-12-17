package com.topface.topface.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.R;

/**
 * класс создает и настраивает view для элемента фида, в зависимости от нужд адаптера
 * <p/>
 * для создания нужно указать тип разметки и, по желанию, флаги
 * опциональные флаги можно обновить и после созднания, для переключения вида элемента,
 * например с нормального пользователя на забаненного
 */
public class FeedItemViewConstructor {
    // минимальное количество непрочитанных сообщений для отображения (включительно)
    private static final int MIN_MSG_AMOUNT_TO_SHOW = 2;

    /**
     * типовые разметки элементов
     */
    public static enum Type {
        // аватарка, имя, сообщение
        SIMPLE(R.layout.item_feed_layout_simple),
        // аватарка, имя, сообщение + сердечко
        HEART(R.layout.item_feed_layout_heart),
        // аватарка, имя, сообщение + некоторый индикатор времени
        TIME(R.layout.item_feed_layout_time),
        // аватарка, имя, сообщение, время + счетчик сообщений
        TIME_COUNT(R.layout.item_feed_layout_time_count);

        private int layoutId = 0;

        Type(int layoutId) {
            this.layoutId = layoutId;
        }

        public int getLayoutId() {
            return layoutId;
        }
    }

    /**
     * дополнительная настройка view путем флагов
     * т.е. некоторые опции могут быть установлены одновременно
     * например - непрочитанное сообщение от забаненного вип-пользователя
     */
    public static class Flag {
        // для непрочитанных элементов
        public static final int NEW = 0b0000000000000001;
        // для вип-пользователей
        public static final int VIP = 0b0000000000000010;
        // для забаненных пользователей
        public static final int BANNED = 0b0000000000000100;
    }

    /**
     * комбинация типа разметки и дополнительных флагов
     */
    public static class TypeAndFlag {
        public Type type;
        public int flag;

        public TypeAndFlag(Type type, int flag) {
            this.flag = flag;
            this.type = type;
        }

        public TypeAndFlag(Type type) {
            this(type, 0);
        }

        public TypeAndFlag(int flag) {
            this(Type.SIMPLE, flag);
        }

        public TypeAndFlag() {
            this(0);
        }
    }

    /**
     * создает новый объект view, выполняет его предварительную настройку
     * @param context        контекст
     * @param typeAndFlag    тип нужной разметки и флаги настроек
     * @return вновь созднанный и настроенный View
     */
    public static View construct(Context context, TypeAndFlag typeAndFlag) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View resultView = li.inflate(typeAndFlag.type.getLayoutId(), null, true);

        // when item is new
        if ((typeAndFlag.flag & Flag.NEW) > 0) {
            resultView.setBackgroundResource(R.drawable.item_new_list_selector);
            TextView tv = (TextView) resultView.findViewById(R.id.ifp_text);
            tv.setTextColor(context.getResources().getColor(R.color.list_text_black));
        } else {
            resultView.setBackgroundResource(R.drawable.item_list_selector);
        }

        // for banned user
        setBanned((TextView) resultView.findViewById(R.id.ifp_name), typeAndFlag.flag);

        //for vip
        /* stub for future usage, when vip difference will be designed, remove this comment line
        if ((typeAndFlag.flag & Flag.VIP) > 0) {
        }/**/

        return resultView;
    }

    /**
     * обновление текста существующего элемента для состояния забанен/не забанен
     * вынесено сюда, что бы не было путаницы с обновлениями элементов из разных мест
     * TextView используется напрямую так как работа с элементами идет через ViewHolder
     * @param tv      TextView цвет текста которого надо обновить
     * @param flag    флаг с настройкой
     */
    public static void setBanned(TextView tv, int flag) {
        if (tv != null) {
            tv.setTextColor((flag & Flag.BANNED) > 0 ?
                    tv.getContext().getResources().getColor(R.color.list_text_gray) :
                    tv.getContext().getResources().getColor(R.color.list_text_black));
        }
    }

    /**
     * обновление счетчика непрочитанных сообщений в элементе списка
     * может скрыть счетчик, если сообщений слишком мало
     * вынесено сюда, что бы не было путаницы с обновлениями из разных мест
     * TextView используется напрямую так как работа с элементами идет через ViewHolder
     * @param counter    TextView счетчика
     * @param amount     количество сообщений
     */
    public static void setCounter(TextView counter, int amount) {
        if (counter != null) {
            if (amount >= MIN_MSG_AMOUNT_TO_SHOW) {
                counter.setText(Integer.toString(amount));
                counter.setVisibility(View.VISIBLE);
            } else {
                counter.setVisibility(View.GONE);
            }
        }
    }

    /**
     * обновление индикатора onLine, как правило, используется для TextView,
     * содержащего имя пользователя
     * TextView используется напрямую так как работа с элементами идет через ViewHolder
     * @param textView    TextView с именем пользователя
     * @param isOnline    сам индикатор
     */
    public static void setOnline(TextView textView, boolean isOnline) {
        if (textView != null) {
            int onLineDrawableId = isOnline ? R.drawable.im_list_online : 0;
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, onLineDrawableId, 0);
        }

    }

}
