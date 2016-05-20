package com.topface.topface.data;

public class ViewLifreCycleData {

    private String mClassName;

    public ViewLifreCycleData(String className) {
        this.mClassName = className;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    @Override
    public int hashCode() {
        return mClassName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ViewLifreCycleData)) return false;
        ViewLifreCycleData data = (ViewLifreCycleData) o;
        return mClassName != null && mClassName.equals(data.getClassName());
    }
}
