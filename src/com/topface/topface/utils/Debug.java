package com.topface.topface.utils;

import android.util.Log;
import com.topface.topface.App;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
public class Debug {

    /**
     * Отладка включена только в debug режиме
     */
    public static final int MODE_DEBUG = 0;
    /**
     * Отладка включена для редакторов
     */
    public static final int MODE_EDITOR = 1;
    /**
     * Отладка включена всегда
     */
    public static final int MODE_ALWAYS = 2;
    /**
     * Отла
     */
    private static final int MODE_DISABLE = 3;

    public static final int MAX_LOG_MESSAGE_LENGTH = 3500;
    /**
     * Резать длинные сообщения в логах на несколько
     */
    public static final boolean CHUNK_LONG_LOGS = true;

    /**
     * Форматировать JSON
     */
    private static final boolean FORMAT_JSON = true;
    private static boolean mShowDebug = App.DEBUG;

    public static void log(Object obj, String msg) {
        if (mShowDebug) {
            if (obj == null)
                showChunkedLogInfo(App.TAG, "::" + msg);
            else if (obj instanceof String)
                showChunkedLogInfo(App.TAG, obj + "::" + msg);
            else
                showChunkedLogInfo(App.TAG, obj.getClass().getSimpleName() + "::" + msg);
        }
    }

    public static void debug(Object obj, String msg) {
        if (mShowDebug) {
            if (obj == null)
                showChunkedLogDebug(App.TAG, "::" + msg);
            else if (obj instanceof String)
                showChunkedLogDebug(App.TAG, obj + "::" + msg);
            else
                showChunkedLogDebug(App.TAG, obj.getClass().getSimpleName() + "::" + msg);
        }
    }

    public static void showChunkedLogInfo(String tag, String msg) {
        if (CHUNK_LONG_LOGS && msg.length() > MAX_LOG_MESSAGE_LENGTH) {
            int chunkCount = (int) Math.ceil(msg.length() / MAX_LOG_MESSAGE_LENGTH) + 1;
            for (int i = 0; i < chunkCount; i++) {
                int max = MAX_LOG_MESSAGE_LENGTH * (i + 1);
                if (max >= msg.length()) {
                    Log.i(tag, msg.substring(MAX_LOG_MESSAGE_LENGTH * i));
                } else {
                    Log.i(tag, msg.substring(MAX_LOG_MESSAGE_LENGTH * i, max));
                }
            }
        } else {
            Log.i(tag, msg);
        }
    }

    public static void showChunkedLogDebug(String tag, String msg) {
        if (CHUNK_LONG_LOGS && msg.length() > MAX_LOG_MESSAGE_LENGTH) {
            int chunkCount = (int) Math.ceil(msg.length() / MAX_LOG_MESSAGE_LENGTH) + 1;
            for (int i = 0; i < chunkCount; i++) {
                int max = MAX_LOG_MESSAGE_LENGTH * (i + 1);
                if (max >= msg.length()) {
                    Log.d(tag, msg.substring(MAX_LOG_MESSAGE_LENGTH * i));
                } else {
                    Log.d(tag, msg.substring(MAX_LOG_MESSAGE_LENGTH * i, max));
                }
            }
        } else {
            Log.d(tag, msg);
        }
    }

    public static void showChunkedLogError(String tag, String msg) {
        if (CHUNK_LONG_LOGS && msg.length() > MAX_LOG_MESSAGE_LENGTH) {
            int chunkCount = (int) Math.ceil(msg.length() / MAX_LOG_MESSAGE_LENGTH) + 1;
            for (int i = 0; i < chunkCount; i++) {
                int max = MAX_LOG_MESSAGE_LENGTH * (i + 1);
                if (max >= msg.length()) {
                    Log.e(tag, msg.substring(MAX_LOG_MESSAGE_LENGTH * i));
                } else {
                    Log.e(tag, msg.substring(MAX_LOG_MESSAGE_LENGTH * i, max));
                }
            }
        } else {
            Log.e(tag, msg);
        }
    }

    public static void log(String msg) {
        if (mShowDebug) {
            showChunkedLogInfo(App.TAG, msg);
        }
    }

    public static void error(String msg, Throwable e) {
        if (mShowDebug) {
            StringBuilder stack = new StringBuilder("\n");
            for (StackTraceElement st : e.getStackTrace()) {
                stack.append(st.toString()).append("\n");
            }
            msg = msg != null && !msg.equals("") ? msg + " : " : "";
            String errorText = e.toString();
            errorText = "\n" + errorText + "\n";
            showChunkedLogError(App.TAG, msg + errorText + stack.toString());
        }
    }

    public static void error(String msg, OutOfMemoryError e) {
        if (mShowDebug) {
            StringBuilder stack = new StringBuilder("\n");
            for (StackTraceElement st : e.getStackTrace()) {
                stack.append(st.toString()).append("\n");
            }
            msg = msg != null && !msg.equals("") ? msg + " : " : "";
            String errorText = e.toString();
            errorText = "\n" + errorText + "\n";
            showChunkedLogError(App.TAG, msg + errorText + stack.toString());
        }
    }

    public static void error(String msg) {
        if (mShowDebug) {
            showChunkedLogError(App.TAG, msg);
        }
    }

    public static void error(Throwable e) {
        error(null, e);
    }

    public static void logJson(String tag, String title, String json) {
        if (mShowDebug) {
            if (json != null) {
                JSONTokener tokener = new JSONTokener(json);
                JSONObject finalResult;
                try {
                    finalResult = new JSONObject(tokener);
                    if (FORMAT_JSON) {
                        Debug.debug(tag, title + "\n" + finalResult.toString(4));
                    } else {
                        Debug.debug(tag, title + "\n" + finalResult.toString());
                    }
                } catch (JSONException ignored) {
                    Debug.debug(tag, title + "\n" + json);
                }
            } else {
                Debug.debug(tag, title + "\n" + json);
            }
        }
    }

    /**
     * В зависимости от режима дебага вклчает отладку
     *
     * @param mode режим отладки (только при дебаге, включен всегда или для редакторов и при отладке)
     */
    public static void setDebugMode(int mode) {
        switch (mode) {
            case MODE_EDITOR:
                mShowDebug = App.DEBUG || Editor.isEditor();
                break;
            case MODE_ALWAYS:
                mShowDebug = true;
                break;
            case MODE_DISABLE:
                mShowDebug = false;
                break;
            case MODE_DEBUG:
            default:
                mShowDebug = App.DEBUG;
                break;
        }
    }

}
