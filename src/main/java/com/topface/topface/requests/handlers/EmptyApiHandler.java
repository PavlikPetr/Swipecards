package com.topface.topface.requests.handlers;

import com.topface.topface.requests.IApiResponse;

/**
 * Created by kirussell on 21.01.14.
 * EmptyHandler does not produce any reaction to request
 * Need for background request with low priority (for example: send feedback on low rate)
 */
public class EmptyApiHandler extends ApiHandler {
    @Override
    public final void success(IApiResponse response) {
    }

    @Override
    public final void fail(int codeError, IApiResponse response) {
    }

    @Override
    public final void always(IApiResponse response) {
    }
}
