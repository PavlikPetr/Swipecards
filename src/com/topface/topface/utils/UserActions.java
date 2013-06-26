package com.topface.topface.utils;
import java.util.ArrayList;


import android.view.View;

public class UserActions {
    private ArrayList<ActionItem> items;
    private View actionsView;

    public UserActions(View view, ArrayList<ActionItem> items) {
        this.items = items;
        this.actionsView = view;
        initActionItems();
    }

    private void initActionItems() {
        for (ActionItem item : items) {
            actionsView.findViewById(item.id).setVisibility(View.VISIBLE);
            actionsView.findViewById(item.id).setOnClickListener(item.listener);
        }
    }

    public View getViewById (int id) {
        return actionsView.findViewById(id);
    }

    private void initListeners() {

    }

    public static class ActionItem {
        public int id;
        public View.OnClickListener listener;

        public ActionItem(int id, View.OnClickListener listener) {
            this.id = id;
            this.listener = listener;
        }
    }
}
