package com.topface.topface.requests.transport.scruffy;

import android.os.Handler;
import android.os.Message;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.statistics.ScruffyStatistics;
import com.topface.topface.utils.social.AuthToken;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализации Fatwood API - https://github.com/Topface/fatwood/blob/master/README.md
 */
public class ScruffyRequestManager {

    public static final String API_URL = "wss://scruffy.core.tf/";
    private static final long FATWOOD_TIMEOUT = 10 * 1000;
    private static final int SENT_REQUESTS_LIMIT = 10;
    public static final int MIN_RECONNECT_DELAY_SEC = 2;
    private static final int RECONNECTION_LIMIT_FOR_HTTP_SWITCH = 3;

    private static ScruffyRequestManager mInstance;
    private WebSocket mWebSocket;
    /**
     * Flag to allow send broadcast that connection were established to prevent send it too often
     * (for example we do not need to send it for first connection to Fatwood in App lifecycle)
     */
    private AtomicBoolean mConnectionEverBeenEstablished = new AtomicBoolean(false);
    /**
     * Счетчик числа реконнектов к API, по достижению которого мы говорим что не удалось подключиться к серверу
     */
    private static AtomicInteger mReconnectCounter = new AtomicInteger(0);
    /**
     * В этой очереди список запросов которые ждут отправки
     */
    private final ConcurrentHashMap<String, ScruffyRequestHolder> mPendingRequests;
    /**
     * В этой очереди список уже отправленных на сервер запросов
     */
    private final ConcurrentHashMap<String, ScruffyRequestHolder> mSentRequests;
    /**
     * Атомарный флаг, по которому мы смотрим не начат ли уже процесс авторизации
     */
    private static AtomicBoolean mIsAuthInProgress = new AtomicBoolean(false);

    private CompletedCallback mClosedCallback = new CompletedCallback() {
        @Override
        public void onCompleted(Exception ex) {
            Debug.error("Scruffy:: Connection closed", ex);
            reconnect();
        }
    };
    private WebSocket.StringCallback mStringCallback = new WebSocket.StringCallback() {
        //Слушаем строки которые шлет нам сервер (если будет gzip то будем слушать бинарные данные)
        public void onStringAvailable(String s) {
            Debug.log("Scruffy:: Response <<<\n" + s);
            processResponseWrapper(s);
        }
    };
    private Timer mTimer;
    private int mLastDecreasedSentRequestsCount;
    private long mLastDecreasedSentRequestTime;
    private boolean mScruffyAvailable = true;

    /**
     * Parses common structure of response
     *
     * @param responseString server response string
     */
    private void processResponseWrapper(String responseString) {
        try {
            ScruffyRequest scruffyResponse = JsonUtils.fromJson(responseString, ScruffyRequest.class);

            ScruffyRequestHolder holder = mSentRequests.get(scruffyResponse.getId());
            Debug.log("Scruffy:: response with id: " + scruffyResponse.getId());
            Debug.log("Scruffy:: sent requests: " + mSentRequests.size());
            for (Map.Entry<String, ScruffyRequestHolder> entry : mSentRequests.entrySet()) {
                Debug.log("Scruffy:: request " + entry.getValue().getId());
            }
            if (holder != null) {
                holder.setResponse(scruffyResponse);
                //noinspection SuspiciousMethodCalls
                mSentRequests.remove(holder.getId());
            } else {
                Debug.error(String.format("Scruffy:: Request for response #%s not found", scruffyResponse.getId()));
            }
        } catch (Exception e) {
            Debug.error("Scruffy::", e);
        }
    }

    private ScruffyRequestManager() {
        mPendingRequests = new ConcurrentHashMap<>();
        mSentRequests = new ConcurrentHashMap<>();
    }

    public static ScruffyRequestManager getInstance() {
        if (mInstance == null) {
            mInstance = new ScruffyRequestManager();
        }
        return mInstance;
    }

    public void addRequest(ScruffyRequestHolder holder) {
        if (holder != null) {
            mPendingRequests.put(holder.getId(), holder);
            sendRequests();
        } else {
            throw new IllegalArgumentException("ScruffyRequestHolder is null");
        }
    }

    private void sendRequests() {
        new BackgroundThread() {
            @Override
            public void execute() {
                synchronized (mPendingRequests) {
                    Debug.log("Scruffy:: try send requests");
                    boolean authInProgress = mIsAuthInProgress.get();
                    if (mPendingRequests.size() > 0 && mWebSocket != null) {
                        Debug.log("Scruffy:: mPendingRequests " + mPendingRequests.size());
                        if (!authInProgress) {
                            for (Map.Entry<String, ScruffyRequestHolder> entry : mPendingRequests.entrySet()) {
                                sendPendingRequestWithKey(entry.getKey());
                            }
                        }
                        if (sentQueueIsGrowing()) {
                            mSentRequests.clear();
                            reconnect();
                        }
                    } else if (!isConnected()) {
                        //Если мы не подключены, то коннектимся
                        connect();
                    }
                }
            }
        };

    }

    /**
     * Checks if sent requests are growing in case of fatwood timeout is exceeded
     *
     * @return true if need to reconnect
     */
    private boolean sentQueueIsGrowing() {
        final int sentRequestsCount = mSentRequests.size();
        int sentDiff = sentRequestsCount - mLastDecreasedSentRequestsCount;
        if (sentDiff < 0) {
            mLastDecreasedSentRequestsCount = sentRequestsCount;
            mLastDecreasedSentRequestTime = System.currentTimeMillis();
        } else if (sentDiff > 0) {
            if (sentDiff > SENT_REQUESTS_LIMIT &&
                    System.currentTimeMillis() - mLastDecreasedSentRequestTime > FATWOOD_TIMEOUT) {
                Debug.log("Scruffy:: need to reconnect, sent requests queue is growing too fast");
                return true;
            }
        }
        return false;
    }

    private void sendPendingRequestWithKey(String key) {
        if (key != null) {
            final ScruffyRequestHolder request = mPendingRequests.get(key);
            IApiRequest apiRequest = request.getRequest();
            if (apiRequest == null) {
                mPendingRequests.remove(key);
                Debug.error(String.format("Scruffy:: Request %s is null", key));
                return;
            }
            if (!apiRequest.isCanceled()) {
                try {
                    String requestString = new ScruffyRequest(
                            apiRequest.getHeaders("Scruffy"),
                            apiRequest.getRequestBodyData()
                    ).toString();

                    Debug.log("Scruffy:: Request " + App.getAppConfig().getScruffyApiUrl() + " >>>\n" + requestString);
                    ScruffyStatistics.sendScruffyRequestSend();
                    mWebSocket.send(requestString);
                    mSentRequests.put(key, request);
                    mPendingRequests.remove(key);
                } catch (Exception e) {
                    Debug.error("Scruffy:: send error", e);
                }
            } else {
                mPendingRequests.get(key).cancel();
                Debug.error(String.format("Scruffy:: Request #%s is canceled", key));
                mPendingRequests.remove(key);
            }
        }
    }

    public void connect() {
        connect(null);
    }

    public void connect(final ConnectedListener listener) {
        if (mIsAuthInProgress.getAndSet(true)) {
            Debug.log("Scruffy:: auth in progress");
            if (listener != null) {
                listener.sendError(null);
            }
            return;
        }
        if (!AuthToken.getInstance().isEmpty()) {
            //Если мы авторизованы коннектимся
            AsyncHttpClient.getDefaultInstance().websocket(App.getAppConfig().getScruffyApiUrl(), null, new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception ex, final WebSocket webSocket) {
                    Debug.log("Scruffy:: try connect");
                    if (ex != null || webSocket == null) {
                        ScruffyStatistics.sendScruffyConnectFailure();
                        Debug.error("Scruffy::", ex);
                        if (listener != null) {
                            listener.onError(ErrorCodes.ERRORS_PROCESSED, ex != null ? ex.toString() : "");
                        }
                        mIsAuthInProgress.set(false);
                        reconnect();
                        return;
                    }
                    ScruffyStatistics.sendScruffyConnectSuccess();
                    //Листенер получения данных от сервера
                    webSocket.setStringCallback(mStringCallback);
                    webSocket.setClosedCallback(mClosedCallback);
                    mWebSocket = webSocket;
                    if (listener != null) {
                        listener.sendConnected();
                    }
                    mReconnectCounter.set(0);
                    mIsAuthInProgress.set(false);
                    mConnectionEverBeenEstablished.set(true);
                    sendRequests();
                }
            });
        } else {
            if (listener != null) {
                listener.sendError(null);
            }
            startAuthorization();
        }
    }

    private void startAuthorization() {
        Debug.error("Scruffy:: Start authorization");
        mIsAuthInProgress.set(false);
    }

    void reconnect() {
        if (!mIsAuthInProgress.get()) {
            int reconnectCounter = mReconnectCounter.incrementAndGet();
            if (reconnectCounter > RECONNECTION_LIMIT_FOR_HTTP_SWITCH) {
                makeScruffyUnavailable();
            }
            killConnection(true);
            int reconnectDelay = (int) Math.pow(MIN_RECONNECT_DELAY_SEC, reconnectCounter);
            Debug.error("Scruffy:: connect error. Try reconnect #" + reconnectCounter
                    + " with delay=" + reconnectDelay + " sec");
            if (mTimer != null) {
                mTimer.cancel();
            }
            mTimer = new Timer();
            mTimer.schedule(new ReconnectTask(), reconnectDelay * 1000L);
        }
    }

    private void makeScruffyUnavailable() {
        ScruffyStatistics.sendScruffyTransportFallback();
        mScruffyAvailable = false;
        for (ScruffyRequestHolder holder : mSentRequests.values()) {
            holder.getRequest().exec();
        }
        for (ScruffyRequestHolder holder : mPendingRequests.values()) {
            holder.getRequest().exec();
        }
        clearState();
    }

    public boolean isConnected() {
        return mWebSocket != null;
    }

    public void logout() {
        clearState();
    }

    private void clearState() {
        mPendingRequests.clear();
        mSentRequests.clear();
        mConnectionEverBeenEstablished.set(false);
        killConnection(true);
    }

    public void killConnection(boolean ignoreCallbacks) {
        if (mWebSocket != null) {
            if (ignoreCallbacks) {
                mWebSocket.setStringCallback(null);
                mWebSocket.setClosedCallback(null);
            }
            mWebSocket.close();
        }
        mWebSocket = null;
    }

    private class ReconnectTask extends TimerTask {

        @Override
        public void run() {
            connect();
        }
    }

    public static abstract class ConnectedListener extends Handler {
        private static final int CONNECTED = 1;
        private static final int FAILED = 2;

        public abstract void onConnected();

        public abstract void onError(int errorCode, String errorMessage);

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECTED:
                    onConnected();
                    break;
                case FAILED:
                    IApiResponse response = (IApiResponse) msg.obj;
                    onError(response.getResultCode(), response.getErrorMessage());
                    break;
            }
        }

        private void sendConnected() {
            Message msg = new Message();
            msg.what = CONNECTED;
            sendMessage(msg);
        }

        private void sendError(IApiResponse error) {
            Message msg = new Message();
            msg.what = FAILED;
            msg.obj = error;
            sendMessage(msg);
        }
    }

    public boolean isAvailable() {
        return mScruffyAvailable;
    }
}
