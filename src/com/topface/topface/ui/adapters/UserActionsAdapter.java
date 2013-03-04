package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Debug;

import java.util.LinkedList;

public class UserActionsAdapter extends BaseAdapter{
    private final Context mContext;
    private LinkedList<UserAction> userActions = new LinkedList<UserAction>();

    public UserActionsAdapter(Context context) {
        mContext = context;
        userActions.add(new UserAction(UserAction.ADMIRATION, "Восхищение", R.drawable.ico_drop_menu_delight_normal));
        userActions.add(new UserAction(UserAction.SYMPATHY, "Симпатия", R.drawable.ico_drop_menu_sympathy_normal));
        userActions.add(new UserAction(UserAction.CHAT, "Разговор", R.drawable.ico_drop_menu_chat_normal));
        userActions.add(new UserAction(UserAction.GIFT, "Подарок", R.drawable.ico_drop_menu_gift_normal));
        userActions.add(new UserAction(UserAction.BLOCK, "Блокировать", R.drawable.ico_drop_menu_blacklist_normal));
    }

    @Override
    public int getCount() {
        return userActions.size();
    }

    @Override
    public Object getItem(int i) {
        return userActions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.user_action_item, viewGroup, false);
            TextView tv = (TextView)view.findViewById(R.id.tvItemTitle);
            tv.setText(userActions.get(i).text);
            ImageView iv = (ImageView) view.findViewById(R.id.ivItemIcon);
            iv.setImageResource(userActions.get(i).iconResource);
//            LinearLayout container = (LinearLayout) view.findViewById(R.id.container);
//            container.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Debug.log("Something");
//                }
//            });
        }
        return view;
    }

    public static class UserAction {
        public static final int ADMIRATION = 0;
        public static final int SYMPATHY = 1;
        public static final int CHAT = 2;
        public static final int GIFT = 3;
        public static final int BLOCK = 4;

        public int id;
        public String text;
        public int iconResource;

        public UserAction(int id, String text, int iconResource) {
            this.id = id;
            this.text = text;
            this.iconResource = iconResource;

        }
    }
}
