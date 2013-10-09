package com.topface.topface.utils;

import android.os.Process;

/**
 * Вспомогательный класс, наследник обычного {@link Thread}, но с немного иным способом использования:
 * <pre>
 *     new BackgroundThread() {
 *         {@literal @}Override
 *         public void execute() {
 *             Debug.log("BackgroundThread with priority " + getPriority());
 *         }
 *     };
 *     //BackgroundThread with priority 10
 * </pre>
 * <ul>
 * <li>
 * Т.е. не нужно создавать дополнительный объект Runnable,
 * достаточно переопредилить абстрактный метод execute,
 * все что внутри будет выполнено в отдельном потоке
 * </li>
 * <li>
 * По умолчанию этот поток будет иметь
 * приоритет {@link Process#THREAD_PRIORITY_BACKGROUND} (приоретет 10),
 * это нужно, что бы не конкурировать с UI
 * </li>
 * <li>
 * Есть конструктор  {@link #BackgroundThread(int priority)},
 * который позволяет задавать приоритет
 * </li>
 * <li>
 * Больше вы не забудете вызвать метод start(), т.к. код выполняется сразу.
 * Если попытаетесь вызвать start() то получите RuntimeException
 * </li>
 * </ul>
 * <p/>
 * Крайне рекомендуется использовать его везде где это возможно, где вам нужны фоновые операции.
 * И очень рекомендуется заменять обычные вызовы Thread,
 * если они есть в коде, с которым вы работаете, но даже не вы писали
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
