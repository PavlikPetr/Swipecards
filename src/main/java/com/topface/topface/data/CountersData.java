package com.topface.topface.data;

/**
 * Counters data
 * Created by onikitin on 24.06.15.
 */
public class CountersData implements Cloneable {

    public int likes = 0;
    public int mutual = 0;
    public int dialogs = 0;
    public int visitors = 0;
    public int fans = 0;
    public int admirations = 0;
    public int peopleNearby = 0;


    public CountersData(int likes, int mutual, int dialogs, int visitors
            , int fans, int admirations, int peopleNearby) {
        this.likes = likes;
        this.mutual = mutual;
        this.dialogs = dialogs;
        this.visitors = visitors;
        this.fans = fans;
        this.admirations = admirations;
        this.peopleNearby = peopleNearby;
    }

    @SuppressWarnings("unused")
    public boolean isNotEmpty() {
        return likes != 0 || mutual != 0 || dialogs != 0 || visitors != 0
                || fans != 0 || admirations != 0 || peopleNearby != 0;
    }
}
