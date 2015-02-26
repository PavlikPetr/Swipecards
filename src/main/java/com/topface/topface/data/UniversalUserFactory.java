package com.topface.topface.data;

/**
 * Factory for creating universal users from various classes
 */
public class UniversalUserFactory {

    public static IUniversalUser create(Profile profile) {
        if (profile instanceof User) {
            return new UniversalUser((User) profile);
        } else {
            return new UniversalProfile(profile);
        }
    }

    public static IUniversalUser create(FeedUser feedUser) {
        return new UniversalFeedUser(feedUser);
    }
}
