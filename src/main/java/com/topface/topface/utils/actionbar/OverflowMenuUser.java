package com.topface.topface.utils.actionbar;

import android.content.Intent;

public interface OverflowMenuUser {
    void setBlackListValue(Boolean value);

    Boolean getBlackListValue();

    void setBookmarkValue(Boolean value);

    Boolean getBookmarkValue();

    void setSympathySentValue(Boolean value);

    Boolean getSympathySentValue();

    Integer getUserId();

    Intent getOpenChatIntent();

    boolean isOpenChatAvailable();

    boolean isAddToFavoritsAvailable();

    Boolean isMutual();

    void clickSendGift();

    Integer getProfileId();

    Boolean isBanned();
}
