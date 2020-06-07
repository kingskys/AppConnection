package com.kingskys.conn;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class LockManager {
    private final List<Lock> mLocks = new ArrayList<>();
//    private final Object mLock = new Object();
    static String appName = "";

    /**
     *
     * @param timeout 超时时间。单位毫秒
     * @throws InterruptedException 被打断异常
     * @throws TimeoutException 超时异常
     */
    void addLock(long timeout) throws InterruptedException, TimeoutException {
        Lock lock = null;
        synchronized (mLocks) {
            lock = new Lock();
            mLocks.add(lock);
        }

        try {
//            log("lock start...: " + lock);
            lock.lock(timeout, TimeUnit.MILLISECONDS); // 等30秒
//            log("lock end...: " + lock);
        } catch (InterruptedException | TimeoutException e) {
            log("lock error: " + lock);
            log("err: " + e);
            throw e;
        }

    }

    void removeLock(Lock lock) {
        synchronized (mLocks) {
            mLocks.remove(lock);
        }
    }

    void unLock() {
//        log("unlock start");
        try {
            synchronized (mLocks) {
                for (Lock lock : mLocks) {
                    try {
//                        log("unlick() lock = " + lock);
                        lock.unlock();
                    } catch (Throwable e) {
                        log("unlock one error: " + e);
                    }
                }
                mLocks.clear();
            }
        } catch (Throwable e) {
            log("unlock error: " + e);
        }
//        log("unlock end");
    }

    private static void log(String msg) {
        Log.e("AppConn_LockManager_" + appName, msg);
    }
}
