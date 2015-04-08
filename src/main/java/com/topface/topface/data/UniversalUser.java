package com.topface.topface.data;

/**
 * Universal user from User class
 */
public class UniversalUser extends UniversalProfile {

    private User mUser;

    UniversalUser(User user) {
        super(user);
        mUser = user;
    }

    @Override
    public boolean isOnline() {
        return mUser.online;
    }
}
