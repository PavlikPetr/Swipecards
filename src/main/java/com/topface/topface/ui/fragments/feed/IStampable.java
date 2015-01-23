package com.topface.topface.ui.fragments.feed;

/**
 * Read/write access to timestamp in class
 * <p/>
 * used for some kind of link for feed fragments and adapters, using them
 */
public interface IStampable {
    @SuppressWarnings("UnusedDeclaration")
    public long getStamp();

    @SuppressWarnings("UnusedDeclaration")
    public void setStamp(long stamp);
}
