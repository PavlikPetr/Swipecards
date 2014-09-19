package com.topface.topface.utils.loadcontollers;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.receivers.ConnectionChangeReceiver;

import java.util.HashMap;

/**
 * Created by ilya on 12.09.14.
 */
public class ChatLoadController extends LoadController {

    @Override
    protected int[] getPreloadLimits() {
        return App.getContext().getResources().getIntArray(R.array.chat_limit);
    }

    @Override
    protected int[] getPreloadOffset() {
        return App.getContext().getResources().getIntArray(R.array.chat_offset);
    }
}
