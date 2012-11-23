package com.topface.topface.utils;

import com.topface.topface.Static;

public class FormItem {
    // Data
    public int type;
    public String title;
    public String value;
    public FormItem header;
    public boolean equal;

    public int titleId = NO_RESOURCE_ID;
    public int dataId = NO_RESOURCE_ID;

    // Constants
    public static final int HEADER = 1;
    public static final int DATA = 3;
    public static final int DIVIDER = 4;

    public static final int NO_RESOURCE_ID = -1;
    public static final int NOT_SPECIFIED_ID = 0;

    private static FormItem divider = null;

    //private static final long serialVersionUID = 1883262786634798671L;    

    public FormItem(int titleId, int type) {
        this.titleId = titleId;
        this.type = type;
        this.value = Static.EMPTY;
        this.dataId = NO_RESOURCE_ID;
        this.equal = false;
    }    

    public FormItem(int titleId, int dataId, int type) {
        this.titleId = titleId;
        this.dataId = dataId;
        this.type = type;
        this.equal = false;
    }
    
    public FormItem(int titleId, int dataId, int type, FormItem header) {
        this.titleId = titleId;
        this.dataId = dataId;
        this.type = type;
        this.equal = false;
        this.header = header;
    }

    public FormItem(int titleId, String data, int type) {
        this.titleId = titleId;
        this.value = data;
        this.dataId = NO_RESOURCE_ID;
        this.type = type;
        this.equal = false;
    }
    
    public FormItem(int titleId, String data, int type, FormItem header) {
        this.titleId = titleId;
        this.value = data;
        this.dataId = NO_RESOURCE_ID;
        this.type = type;
        this.equal = false;
        this.header = header;
    }

    private FormItem(int type) {
        this.type = type;
        this.value = Static.EMPTY;
        this.dataId = NO_RESOURCE_ID;
        this.title = Static.EMPTY;
        this.titleId = NO_RESOURCE_ID;
        this.equal = false;
    }

    public FormItem() {
    }

    public static FormItem getDivider() {
        if (divider == null) {
            FormItem.divider = new FormItem(DIVIDER);
        }
        return divider;
    }
}
