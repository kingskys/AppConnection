package com.kingskys.conn;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LockData<T> {

    private final Object mLock = new Object();
    private boolean mHaveResult = false;
    private T mData = null;
    FutureTask<T> mTask = null;

    public LockData() {
        mTask = createTask();
        new Thread(mTask).start();
    }

    private FutureTask<T> createTask() {
        return new FutureTask<T>(new Callable<T>() {
            @Override
            public T call() throws Exception {
                if (mHaveResult) {
                    return mData;
                }

                lock();
                return mData;
            }
        });

    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return mTask.get(timeout, unit);
    }

    public void set(T value) {
        mData = value;
        mHaveResult = true;
        unlock();
    }

    private void lock() throws InterruptedException {
        synchronized (mLock) {
            mLock.wait();
        }
    }

    private void unlock() {
        synchronized (mLock) {
            mLock.notifyAll();
        }
    }

}
