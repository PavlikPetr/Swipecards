package com.topface.topface.utils.actionbar;

/**
 * Created by kirussell on 26.09.13.
 * Interface to set actionBar title and subtitle
 */
public interface IActionBarTitleSetter {
    void setActionBarTitles(String title, String subtitle);

    void setActionBarTitles(int title, int subtitle);

    void setActionBarTitles(String title, int subtitle);

    void setActionBarTitles(int title, String subtitle);
}
