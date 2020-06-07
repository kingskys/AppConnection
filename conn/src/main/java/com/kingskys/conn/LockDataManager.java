package com.kingskys.conn;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class LockDataManager {

    private static long preTime = 0;
    private static long idIdx = 0;
    private static final Object idLock = new Object();

    static String getUniqueId() {
        synchronized (idLock) {
            long now = System.currentTimeMillis();
            if (now == preTime) {
                idIdx++;
            } else {
                idIdx = 0;
            }
            preTime = now;
            return String.format(Locale.ENGLISH, "%d%d", now, idIdx);
        }
    }


    private final Map<String, LockData<String>> mLocks = new HashMap<>();

    private LockData<String> addLock(String uid) {
        synchronized (mLocks) {
            LockData<String> lockData = new LockData<>();
            mLocks.put(uid, lockData);
            return lockData;
        }
    }

    void setLockOk(String uid, String v) {
        synchronized (mLocks) {
            LockData<String> lockData = mLocks.get(uid);
            if (lockData != null) {
                lockData.set(v);
            }
        }
    }

    private void removeLock(String uid) {
        synchronized (mLocks) {
            mLocks.remove(uid);
        }
    }

    String get(String uid, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
        LockData<String> lockData = addLock(uid);
        try {
            return lockData.get(timeout, TimeUnit.SECONDS);
        } finally {
            removeLock(uid);
        }
    }
}
