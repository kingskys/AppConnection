package com.kingskys.conn;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConnClient {
    private static boolean isInited = false;

    /**
     *
     * @param clientId 客户端ID
     * @param packageName 服务器的包名
     * @param app 自己应用的Application
     */
    public static void init(String clientId, String packageName, Application app, ConnClientListener callback) {
        if (isInited) return;
        isInited = true;

        ConnClientImp.mClientId = clientId;
        ConnClientImp.mPackageName = packageName;
        ConnClientImp.mApp = new WeakReference<Application>(app);
        ConnClientImp.mListener = callback;
        ConnClientImp.appName = app.getPackageName();
        LockManager.appName = app.getPackageName();

        ConnClientImp.startService();
    }

    // 发送消息
    // 注意：不要在主线程调用
    public static void send(String data, String action) throws RemoteException, ExecutionException {
        ConnClientImp.send(Const.MSG_TYPE_MSG, data, action, null);
    }

    // 注意：不要在主线程调用
    public static void send(String data) throws RemoteException, ExecutionException {
        ConnClientImp.send(Const.MSG_TYPE_MSG, data, "normal", null);
    }

    /**
     * 申请有人返回数据
     * @param timeout 等待超时时间，单位秒
     */
    // 注意：不要在主线程调用
    public static String request(String data, String action, long timeout) throws RemoteException, ExecutionException, TimeoutException, InterruptedException {
        return ConnClientImp.request(data, action, timeout);
    }

    public static void response(String data, String msgId) throws RemoteException, ExecutionException {
        ConnClientImp.response(data, msgId);
    }

    public static void setListener(ConnClientListener listener) {
        ConnClientImp.mListener = listener;
    }

    public static boolean isOnline() {
        return ConnClientImp.mIsOnline;
    }

    public static void waitOnlineOk() throws InterruptedException {
        ConnClientImp.waitOnlineOk();
    }

    public static String[] getOnlineClients() {
        return ConnClientImp.getOnlineClientIds();
    }
}
