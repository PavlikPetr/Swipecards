package com.topface.topface.utils;

/**
 * Listener for connection process
 */
public interface IRequestConnectionListener {
    void onConnectionStarted();

    void onConnectInvoked();

    void onConnectionEstablished();

    void onConnectionClose();
}
