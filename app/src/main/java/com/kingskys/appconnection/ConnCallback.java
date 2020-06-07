package com.kingskys.appconnection;

import com.kingskys.conn.ConnClientListener;

import java.lang.ref.WeakReference;

public class ConnCallback implements ConnClientListener {
    private static WeakReference<MainActivity> mActivity = null;
    public static void setmActivity(MainActivity mainActivity) {
        mActivity = new WeakReference<>(mainActivity);
    }

    @Override
    public void selfOnline(String clientId, String[] onlineIds) {
        addLog("selfOnline(" + clientId + ")");
        updateOnlines();
    }

    @Override
    public void otherOnline(String clientId) {
        addLog("otherOnline(" + clientId + ")");
        updateOnlines();
    }

    @Override
    public void selfOffline(String clientId, String msg) {
        addLog("selfOffline(" + clientId + "): " + msg);
        updateOnlines();
    }

    @Override
    public void otherOffline(String clientId, String msg) {
        addLog("otherOffline(" + clientId + "): " + msg);
        updateOnlines();
    }

    @Override
    public void selfMsg(String clientId, String msg, String action) {
        addLog("self[" + clientId + "][" + action + "]: " + msg);
    }

    @Override
    public void otherMsg(String clientId, String msg, String action) {
        addLog("other[" + clientId + "][" + action + "]: " + msg);
    }

    @Override
    // 请求消息
    public void onRequest(String clientId, String msg, String action, String msgId) {
        // 暂时不处理
    }

    private void addLog(String msg) {
        if (mActivity != null && mActivity.get() != null) {
            mActivity.get().addLog(msg);
        }
    }

    private void updateOnlines() {
        if (mActivity != null && mActivity.get() != null) {
            mActivity.get().updateOnlines();
        }
    }
}