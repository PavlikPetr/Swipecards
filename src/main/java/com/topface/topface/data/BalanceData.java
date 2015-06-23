package com.topface.topface.data;

/**
 * Created by ppetr on 16.06.15.
 * data class for balance object from requests
 */
public class BalanceData implements Cloneable {
    public boolean premium;
    public int likes;
    public int money;

    public BalanceData(boolean premium, int likes, int money) {
        this.premium = premium;
        this.likes = likes;
        this.money = money;
    }
}
