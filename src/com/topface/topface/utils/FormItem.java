package com.topface.topface.utils;

import java.io.Serializable;

import com.topface.topface.Static;

public class FormItem implements Serializable {
    // Data
    public int type;
    public String title;    
    public String value;
    public boolean equal;
    
    public int titleId = NO_RESOURCE_ID;
    public int dataId = NO_RESOURCE_ID;

    // Constants
    public static final int HEADER = 1;    
    public static final int DATA = 3;
    public static final int DIVIDER = 4;
    
    public static final int NO_RESOURCE_ID = -1;
    
    private static FormItem divider = null;
    
    private static final long serialVersionUID = 1883262786634798671L;    
    
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
    
    public FormItem(int titleId, String data, int type) {
    	this.titleId = titleId;
    	this.value = data;
    	this.dataId = NO_RESOURCE_ID;
    	this.type = type;
    	this.equal = false;
    }
    
    private FormItem(int type) {
    	this.type = type;
    	this.value = Static.EMPTY;
    	this.dataId = NO_RESOURCE_ID;
    	this.title = Static.EMPTY;
    	this.titleId = NO_RESOURCE_ID;
    	this.equal = false;
    }
    
    public FormItem() { }
    
    public static FormItem getDivider() {
    	if (divider == null) {
    		FormItem.divider = new FormItem(DIVIDER);
    	}
    	return divider;
    }
}
