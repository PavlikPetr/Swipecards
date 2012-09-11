package com.topface.topface.utils;

import java.io.Serializable;

public class FormItem implements Serializable {
    // Data
    public int type;
    public String title;
    public String value;
    public boolean equal;    

    // Constants
    public static final int HEADER = 1;
    public static final int TITLE = 2;
    public static final int DATA = 3;
    public static final int DIVIDER = 4;
    
    private static final long serialVersionUID = 1883262786634798671L;
}

