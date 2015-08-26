package com.topface.topface.data;

/**
 * Created by ppetr on 16.06.15.
 * data class for balance object from requests
 */
public class BalanceData implements Cloneable {
    public boolean premium;
    public int likes = 0;
    public int money = 0;

    public BalanceData(boolean premium, int likes, int money) {
        this.premium = premium;
        this.likes = likes;
        this.money = money;
    }

    public BalanceData(BalanceData balanceData) {
        this.premium = balanceData.premium;
        this.likes = balanceData.likes;
        this.money = balanceData.money;
    }

    public BalanceData() {
    }
}
