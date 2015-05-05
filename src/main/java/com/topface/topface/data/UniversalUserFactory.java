package com.topface.topface.data;

/**
 * Factory for creating universal users from various classes
 */
public class UniversalUserFactory {

    public static IUniversalUser create(Profile profile) {
        if (profile instanceof User) {
            return new UniversalUserWrapper((User) profile);
        } else {
            return new UniversalProfileWrapper(profile);
        }
    }

    public static IUniversalUser create(FeedUser feedUser) {
        return new UniversalFeedUserWrapper(feedUser);
    }
}
