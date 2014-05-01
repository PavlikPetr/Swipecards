package com.topface.topface.utils.http;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiThreadFactory implements ThreadFactory {
    /**
     * Приоретет по умолчанию для создаваемых поток.
     * Сейчас он у нас ниже, чем для UI и выше чем для фоновых процессов
     */
    public static final int THREAD_PRIORITY_BACKGROUND = Thread.NORM_PRIORITY - 1;
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    ApiThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
    }

    @SuppressWarnings("NullableProblems")
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon())
            t.setDaemon(false);

        t.setPriority(THREAD_PRIORITY_BACKGROUND);
        return t;
    }
}