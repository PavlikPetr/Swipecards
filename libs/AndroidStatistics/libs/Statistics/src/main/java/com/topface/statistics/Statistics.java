package com.topface.statistics;

import com.google.gson.Gson;

public class Statistics {
    private static final long DEFAULT_MAX_EXPIRE_DELAY = 180000;
    private static final int DEFAULT_MAX_HITS_DISPATCH = 200;

    private HitsQueue mHitsQueue;
    private IHitDataBuilder mHitDataBuilder;
    private IDataDispatcher mDataDispatcher;
    private int mMaxHitsDispatch = DEFAULT_MAX_HITS_DISPATCH;
    private long mMaxDispatchExpireDelay = DEFAULT_MAX_EXPIRE_DELAY;
    // allow to send hits more often then its expire time
    private long mMaxDispatchExpireDelayToTry = DEFAULT_MAX_EXPIRE_DELAY/2;
    private long mLastDispatchTimestamp;

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
        mHitsQueue.addHit(expireTime, mHitDataBuilder.build(hit));
        tryToDispatchQueue();
    }

    public void onStart() {
        mHitsQueue.restoreHitsQueue(new HitsQueue.IHitsRestoreListener() {
            @Override
            public void onHitsRestored() {
                dispatchQueue();
            }
        });
    }

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
        return mHitsQueue.size() >= mMaxHitsDispatch || timeFromLastDispatch > mMaxDispatchExpireDelayToTry;
    }

    private void dispatchQueue() {
        mDataDispatcher.dispatchData(mHitsQueue.pollHits(mMaxHitsDispatch));
        mLastDispatchTimestamp = System.currentTimeMillis();
    }

    public Statistics setMaxHitsDispatch(int maxHits) {
        mMaxHitsDispatch = maxHits;
        return this;
    }

    public Statistics setMaxDispatchExpireDelay(long maxExpireDelay) {
        mMaxDispatchExpireDelay = maxExpireDelay;
        mMaxDispatchExpireDelayToTry = mMaxDispatchExpireDelay/2;
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

    /**
     * Builds json data from object with Gson functionality
     */
    private class GsonHitDataBuilder implements IHitDataBuilder {
        public String build(Object object) {
            return (new Gson()).toJson(object);
        }
    }
}