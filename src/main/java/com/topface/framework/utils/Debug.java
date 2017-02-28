package com.topface.framework.utils;

import android.text.TextUtils;
import android.util.Log;

import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.utils.Editor;

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
    public static final int MAX_LOG_MESSAGE_LENGTH = 3500;
    /**
     * Резать длинные сообщения в логах на несколько
     */
    public static final boolean CHUNK_LONG_LOGS = true;
    /**
     * Отла
     */
    private static final int MODE_DISABLE = 3;
    /**
     * Форматировать JSON
     */
    private static final boolean FORMAT_JSON = true;
    private static boolean mShowDebugLogs = BuildConfig.DEBUG;

    public static void log(Object obj, String msg) {
        if (mShowDebugLogs) {
            if (obj == null)
                showChunkedLogInfo(App.TAG, "::" + msg);
            else if (obj instanceof String)
                showChunkedLogInfo(App.TAG, obj + "::" + msg);
            else
                showChunkedLogInfo(App.TAG, obj.getClass().getSimpleName() + "::" + msg);
        }
    }

    public static void debug(Object obj, String msg) {
        if (mShowDebugLogs) {
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

    public static void error(String msg, Throwable e) {
        if (mShowDebugLogs) {
            StringBuilder stack = new StringBuilder("\n");
            for (StackTraceElement st : e.getStackTrace()) {
                stack.append(st.toString()).append("\n");
            }
            msg = !TextUtils.isEmpty(msg) ? msg + " : " : "";
            String errorText = e.toString();
            errorText = "\n" + errorText + "\n";
            showChunkedLogError(App.TAG, msg + errorText + stack.toString());
        }
    }

    public static void log(String msg) {
        if (mShowDebugLogs) {
            showChunkedLogInfo(App.TAG, msg);
        }
    }

    public static void error(String msg, OutOfMemoryError e) {
        if (mShowDebugLogs) {
            StringBuilder stack = new StringBuilder("\n");
            for (StackTraceElement st : e.getStackTrace()) {
                stack.append(st.toString()).append("\n");
            }
            msg = !TextUtils.isEmpty(msg) ? msg + " : " : "";
            String errorText = e.toString();
            errorText = "\n" + errorText + "\n";
            showChunkedLogError(App.TAG, msg + errorText + stack.toString());
        }
    }

    public static void error(String msg) {
        if (mShowDebugLogs) {
            showChunkedLogError(App.TAG, msg);
        }
    }

    public static void error(Throwable e) {
        error(null, e);
    }

    public static void logJson(String tag, String title, String json) {
        if (mShowDebugLogs) {
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
     * В зависимости от режима дебага вклчает отладку2
     *
     * @param mode режим отладки (только при дебаге, включен всегда или для редакторов и при отладке)
     */
    public static void setDebugMode(int mode) {
        switch (mode) {
            case MODE_EDITOR:
                mShowDebugLogs = BuildConfig.DEBUG || Editor.isEditor();
                break;
            case MODE_ALWAYS:
                mShowDebugLogs = true;
                break;
            case MODE_DISABLE:
                mShowDebugLogs = false;
                break;
            case MODE_DEBUG:
            default:
                mShowDebugLogs = BuildConfig.DEBUG;
                break;
        }
    }

    public static boolean isDebugLogsEnabled() {
        return mShowDebugLogs;
    }
}
