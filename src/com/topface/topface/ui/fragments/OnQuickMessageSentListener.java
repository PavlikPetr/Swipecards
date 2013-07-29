package com.topface.topface.ui.fragments;

/**
 * Листенер событий фрагмента быстрых сообщений
 */
public interface OnQuickMessageSentListener {
    public void onMessageSent(String message, QuickMessageFragment fragment);

    public void onCancel(QuickMessageFragment fragment);
}
