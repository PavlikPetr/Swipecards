package com.topface.topface.utils;

public  interface JsonSerializable {
    public abstract String toJSON();
    public abstract void fromJSON(String json);
}
