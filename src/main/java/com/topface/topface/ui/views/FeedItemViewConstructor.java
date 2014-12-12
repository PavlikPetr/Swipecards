package com.topface.topface.ui.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.R;

public class FeedItemViewConstructor {
    private static final int MIN_MSG_AMOUNT_TO_SHOW = 2;

    public static enum Type {
        SIMPLE(R.layout.item_feed_layout_simple),
        HEART(R.layout.item_feed_layout_heart),
        TIME(R.layout.item_feed_layout_time),
        TIME_COUNT(R.layout.item_feed_layout_time_count);

        private int layoutId = 0;

        Type(int layoutId) {
            this.layoutId = layoutId;
        }

        public int getLayoutId() {
            return layoutId;
        }
    }

    public static class Flag {
        public static final int NEW = 0b0000000000000001;
        public static final int VIP = 0b0000000000000010;
        public static final int BANNED = 0b0000000000000100;
    }

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

    public static void setBanned(TextView tv, int flag) {
        if (tv != null) {
            tv.setTextColor((flag & Flag.BANNED) > 0 ?
                    tv.getContext().getResources().getColor(R.color.list_text_gray) :
                    tv.getContext().getResources().getColor(R.color.list_text_black));
        }
    }

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

    public static void setOnline(TextView textView, boolean isOnline) {
        if (textView != null) {
            int onLineDrawableId = isOnline ? R.drawable.im_list_online : 0;
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, onLineDrawableId, 0);
        }

    }

}
