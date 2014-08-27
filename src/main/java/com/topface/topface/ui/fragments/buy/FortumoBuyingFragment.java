package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;

/**
 * Фрагмент покупки монет и симпатий через Fortumo
 */
public class FortumoBuyingFragment extends GooglePlayBuyingFragment {
    public static FortumoBuyingFragment newInstance(String from) {
        FortumoBuyingFragment fragment = new FortumoBuyingFragment();
        Bundle args = new Bundle();
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }
}
