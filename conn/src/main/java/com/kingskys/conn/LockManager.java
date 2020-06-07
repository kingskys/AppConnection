package com.kingskys.conn;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LockManager {
    private final List<Lock> mLocks = new ArrayList<>();
//    private final Object mLock = new Object();
    public static String appName = "";

    public void addLock() throws InterruptedException, TimeoutException {
        Lock lock = null;
        synchronized (mLocks) {
            lock = new Lock();
            mLocks.add(lock);
        }

        try {
            log("lock start...: " + lock);
            lock.lock(30000, TimeUnit.MILLISECONDS); // 等30秒
            log("lock end...: " + lock);
        } catch (InterruptedException | TimeoutException e) {
            log("lock error: " + lock);
            log("err: " + e);
            throw e;
        }

    }

    public void removeLock(Lock lock) {
        synchronized (mLocks) {
            mLocks.remove(lock);
        }
    }

    public void unLock() {
        log("unlock start");
        try {
            synchronized (mLocks) {
                for (Lock lock : mLocks) {
                    try {
                        log("unlick() lock = " + lock);
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
        log("unlock end");
    }

    private static void log(String msg) {
        Log.e("AppConn_LockManager_" + appName, msg);
    }
}
