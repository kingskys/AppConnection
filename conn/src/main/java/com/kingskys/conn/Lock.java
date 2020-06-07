package com.kingskys.conn;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Lock {
    private final Object mLock = new Object();
    private Boolean mData = null;

    private static void log(String msg) {
        Log.e("AppConn", msg);
    }

    public void lock(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (mData == null) {
                    pause();
                }

                return true;
            }
        });

        new Thread(task).start();
        try {
            task.get(timeout, unit);
        } catch (TimeoutException | InterruptedException e) {
            throw e;
        } catch (Throwable e) {}
        finally {
            reset();
        }
    }

    public void unlock() {
        mData = true;
        resume();
    }

    public void reset() {
        mData = null;
    }

    private void pause() {
        synchronized (mLock) {
            try {
                mLock.wait();
            } catch (InterruptedException e) {}
        }
    }

    private void resume() {
        synchronized (mLock) {
            try {
                mLock.notifyAll();
            } catch (Throwable e) {}
        }
    }
}
