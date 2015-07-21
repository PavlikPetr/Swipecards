package com.topface.topface.data;

import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Counters data
 * Created by onikitin on 24.06.15.
 */
public class CountersData {

    public int likes = 0;
    public int mutual = 0;
    public int dialogs = 0;
    public int visitors = 0;
    public int fans = 0;
    public int admirations = 0;
    public int peopleNearby = 0;
    public int bonus = 0;

    public CountersData(CountersData countersData) {
        this.likes = countersData.likes;
        this.mutual = countersData.mutual;
        this.dialogs = countersData.dialogs;
        this.visitors = countersData.visitors;
        this.fans = countersData.fans;
        this.admirations = countersData.admirations;
        this.peopleNearby = countersData.peopleNearby;
    }

    public CountersData() {
    }

    public int getCounterByFragmentId(BaseFragment.FragmentId id) {
        switch (id) {
            case TABBED_DIALOGS:
                return dialogs;
            case TABBED_VISITORS:
                return visitors;
            case TABBED_LIKES:
                return likes;
            case GEO:
                return peopleNearby;
            case BONUS:
                return bonus;
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public boolean isNotEmpty() {
        return likes != 0 || mutual != 0 || dialogs != 0 || visitors != 0
                || fans != 0 || admirations != 0 || peopleNearby != 0;
    }

    @Override
    public boolean equals(Object data) {
        return ((CountersData) data).likes == likes &&
                ((CountersData) data).mutual == mutual &&
                ((CountersData) data).dialogs == dialogs &&
                ((CountersData) data).visitors == visitors &&
                ((CountersData) data).fans == fans &&
                ((CountersData) data).admirations == admirations &&
                ((CountersData) data).peopleNearby == peopleNearby;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + likes;
        hash = hash * 31 + mutual;
        hash = hash * 31 + dialogs;
        hash = hash * 31 + visitors;
        hash = hash * 31 + fans;
        hash = hash * 31 + admirations;
        hash = hash * 31 + peopleNearby;
        return hash;
    }
}
