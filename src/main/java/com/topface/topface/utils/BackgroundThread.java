package com.topface.topface.utils;

import android.os.Process;

/**
 * Вспомогательный класс
 */
public abstract class BackgroundThread extends Thread {
    public static final int DEFAULT_PRIORITY = Process.THREAD_PRIORITY_BACKGROUND;

    public BackgroundThread() {
        this(DEFAULT_PRIORITY);

    }

    public BackgroundThread(int priority) {
        setPriority(priority);
        super.start();
    }

    @Override
    final public void run() {
        execute();
    }

    abstract public void execute();

    @Override
    public synchronized void start() {
        super.start();
        throw new RuntimeException("BackgroundThread automaticaly start runnable");
    }
}
