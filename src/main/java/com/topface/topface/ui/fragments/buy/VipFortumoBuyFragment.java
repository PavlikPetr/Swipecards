package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;

/**
 * Фрагмент покупки VIP через Fortumo
 */
public class VipFortumoBuyFragment extends VipBuyFragment {
    public static VipFortumoBuyFragment newInstance(boolean needActionBar, String from) {
        VipFortumoBuyFragment fragment = new VipFortumoBuyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ACTION_BAR_CONST, needActionBar);
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }
}
