package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;

public abstract class AbstractData {

    /**
     * Устареший метод, использовать не стоит, он бесполезен, там где используется следует заменить на fillData
     */
    @SuppressWarnings("UnusedParameters")
    @Deprecated
    public static Object parse(ApiResponse response) {
        //Extend me
        return null;
    }

}
