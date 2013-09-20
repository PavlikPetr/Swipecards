package com.topface.topface.requests;

import android.content.Context;

@SuppressWarnings("UnusedDeclaration")
public class ParallelApiRequest extends MultipartApiRequest {

    public static final String BOUNDARY = "XFDNSAdakmslen23n4123asdmnasd";
    public static final String REQUEST_TYPE = "parallel";

    public ParallelApiRequest(Context context) {
        super(context);
    }

    @Override
    protected String getRequestType() {
        return REQUEST_TYPE;
    }

    @Override
    protected String getBoundary() {
        return BOUNDARY;
    }


}
