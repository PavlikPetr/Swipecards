package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

public class DeleteMutualsRequest extends DeleteAbstractFeedsRequest {
    private static final String SERVICE = "mutual.delete";

    public DeleteMutualsRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
