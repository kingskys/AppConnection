package com.kingskys.conn;

import android.app.Application;
import android.os.RemoteException;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ConnClient {
    private static boolean isInited = false;

    /**
     * 初始化
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

    // 发送消息
    // 注意：不要在主线程调用
    public static void send(String data) throws RemoteException, ExecutionException {
        ConnClientImp.send(Const.MSG_TYPE_MSG, data, "normal", null);
    }

    /**
     * 发送消息并等待其它客户端对此消息进行回复
     * @param timeout 等待超时时间，单位秒
     */
    // 注意：不要在主线程调用
    public static String request(String data, String action, long timeout) throws RemoteException, ExecutionException, TimeoutException, InterruptedException {
        return ConnClientImp.request(data, action, timeout);
    }

    /**
     * 回复消息
     * @param data 消息内容
     * @param msgId 消息ID
     * @throws RemoteException 断线异常
     * @throws ExecutionException 执行异常
     */
    public static void response(String data, String msgId) throws RemoteException, ExecutionException {
        ConnClientImp.response(data, msgId);
    }

    /**
     * 设置监听回调消息
     */
    public static void setListener(ConnClientListener listener) {
        ConnClientImp.mListener = listener;
    }

    /**
     * 是否连接成功
     * @return 是否连接成功
     */
    public static boolean isOnline() {
        return ConnClientImp.mIsOnline;
    }

    /**
     * 等待连接成功
     * @throws InterruptedException 操作被打断
     */
    public static void waitOnlineOk() throws InterruptedException {
        ConnClientImp.waitOnlineOk();
    }

    /**
     * 获取当前连接服务器成功的客户端的ID，包含自己
     * @return 客户端ID数组
     */
    public static String[] getOnlineClients() {
        return ConnClientImp.getOnlineClientIds();
    }
}
