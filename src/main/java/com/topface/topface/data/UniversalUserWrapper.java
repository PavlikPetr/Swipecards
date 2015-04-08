package com.topface.topface.data;

/**
 * Universal user from User class
 */
public class UniversalUserWrapper extends UniversalProfileWrapper {

    private User mUser;

    UniversalUserWrapper(User user) {
        super(user);
        mUser = user;
    }

    @Override
    public boolean isOnline() {
        return mUser.online;
    }
}
