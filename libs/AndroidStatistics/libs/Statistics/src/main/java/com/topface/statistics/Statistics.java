package com.topface.statistics;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Main statistics class. Contains logic for:
 * - queueing events
 * - dispatch data
 * - store/restore not dispatched data
 * - rules to dispatch data
 */
public class Statistics {
    private static final long DEFAULT_MAX_EXPIRE_DELAY = 180000;
    private long mMaxDispatchExpireDelay = DEFAULT_MAX_EXPIRE_DELAY;
    // allow to send hits more often then its expire time
    private long mMaxDispatchExpireDelayToTry = DEFAULT_MAX_EXPIRE_DELAY / 2;
    private static final int DEFAULT_MAX_HITS_DISPATCH = 200;
    private int mMaxHitsDispatch = DEFAULT_MAX_HITS_DISPATCH;
    private HitsQueue mHitsQueue;
    private IHitDataBuilder mHitDataBuilder;
    private IDataDispatcher mDataDispatcher;
    private long mLastDispatchTimestamp;
    private ILogger mLogger;

    public Statistics(IDataDispatcher dataDispatcher) {
        mHitDataBuilder = new GsonHitDataBuilder();
        mHitsQueue = new HitsQueue(null);
        mDataDispatcher = dataDispatcher.setDataBuilder(mHitDataBuilder);
    }

    public Statistics(IDataDispatcher dataDispatcher, IAsyncStorage storage) {
        mHitDataBuilder = new GsonHitDataBuilder();
        mHitsQueue = new HitsQueue(storage);
        mDataDispatcher = dataDispatcher.setDataBuilder(mHitDataBuilder);
    }

    public synchronized void sendHit(Hit hit) {
        long expireTime = System.currentTimeMillis() + mMaxDispatchExpireDelay;
        String hitData = mHitDataBuilder.build(hit);
        mHitsQueue.addHit(expireTime, hitData);
        log(hitData);
        tryToDispatchQueue();
    }

    /**
     * Call on some activity start
     */
    public void onStart() {
        mHitsQueue.restoreHitsQueue(new HitsQueue.IHitsRestoreListener() {
            @Override
            public void onHitsRestored() {
                dispatchQueue();
            }
        });
    }

    /**
     * Call on some activity stop
     */
    public void onStop() {
        dispatchQueue();
        mHitsQueue.storeHitsQueue();
    }

    /**
     * Dispatches hits' data if hits limits are exceeded(quantity limit and time limit)
     */
    private void tryToDispatchQueue() {
        if (canDispatchData()) {
            dispatchQueue();
        }
    }

    private boolean canDispatchData() {
        long timeFromLastDispatch = System.currentTimeMillis() - mLastDispatchTimestamp;
        int queueSize = mHitsQueue.size();
        boolean result = queueSize >= mMaxHitsDispatch || timeFromLastDispatch > mMaxDispatchExpireDelayToTry;
        log("Try to dispatch:" + result + " => queue size=" + queueSize + ":: delay = " + timeFromLastDispatch);
        return result;
    }

    private void dispatchQueue() {
        List<String> data = mHitsQueue.pollHits(mMaxHitsDispatch);
        mDataDispatcher.dispatchData(data);
        log("Dispatched " + data.size() + " hits");
        mLastDispatchTimestamp = System.currentTimeMillis();
    }

    public Statistics setMaxHitsDispatch(int maxHits) {
        mMaxHitsDispatch = maxHits;
        return this;
    }

    public Statistics setMaxDispatchExpireDelay(long maxExpireDelay) {
        mMaxDispatchExpireDelay = maxExpireDelay;
        mMaxDispatchExpireDelayToTry = mMaxDispatchExpireDelay / 2;
        return this;
    }

    public Statistics setDataBuilder(IHitDataBuilder builder) {
        mHitDataBuilder = builder;
        mDataDispatcher.setDataBuilder(mHitDataBuilder);
        return this;
    }

    public Statistics setStorage(IAsyncStorage storage) {
        mHitsQueue.setStorage(storage);
        return this;
    }

    public Statistics setLogger(ILogger logger) {
        mLogger = logger;
        return this;
    }

    private void log(String msg) {
        if (mLogger != null) {
            mLogger.log(msg);
        }
    }

    /**
     * Builds json data from object with Gson functionality
     */
    private class GsonHitDataBuilder implements IHitDataBuilder {
        public String build(List<String> list) {
            if (list != null && !list.isEmpty()) {
                List<Hit> res = new ArrayList<>(list.size());
                for (String str : list) {
                    res.add((new Gson()).fromJson(str, Hit.class));
                }
                return (new Gson()).toJson(res);
            } else {
                return "";
            }
        }

        public String build(Hit hit) {
            return (new Gson()).toJson(hit);
        }
    }
}