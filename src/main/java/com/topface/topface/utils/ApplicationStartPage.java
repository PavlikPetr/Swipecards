package com.topface.topface.utils;

import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Created by ppetr on 20.11.14.
 */
public class ApplicationStartPage {
    public static final String startPage_Dating = "START";
    public static final String startPage_Dialog = "DIALOGS";


    public static BaseFragment.FragmentId getStartFragmentId() {

        switch (CacheProfile.getOptions().startPage) {
            case startPage_Dialog:
                return BaseFragment.FragmentId.F_DIALOGS;
            default:
                return BaseFragment.FragmentId.F_DATING;
        }
    }
}
