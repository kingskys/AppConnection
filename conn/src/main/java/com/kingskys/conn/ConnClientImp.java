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
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class ConnClientImp {


    static boolean mIsOnline = false;

    static ConnClientListener mListener = null;

    static final ArrayList<String> mClientIds = new ArrayList<>();

    static String mClientId = "";
    static String appName = "";

    static String mPackageName = "";
    private static final String mServiceName = "com.kingskys.conn.ConnService";
    static WeakReference<Application> mApp = null;

    private static Messenger mSendMessenger = null;

    private static LockManager mOnlineLock = new LockManager();

    private static LockDataManager mLockDataManager = new LockDataManager();

    static void startService() {
        if (mApp == null || mApp.get() == null) {
            log("开始连接服务器异常，本App可能已死");
            return;
        }

        Intent intent = new Intent();
        intent.setClassName(mPackageName, mServiceName);

        mApp.get().bindService(intent, connection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);

    }

    private static Messenger mReceiveMessenger = new Messenger(new ConnHandler());

//    private static Conn.OnReceivedCallback mCallback = null;
//
//    public interface OnReceivedCallback {
//        void onReceived(String data);
//    }

    private static class ConnHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            log("handleMessage");
            Bundle bundle = msg.getData();
            String clientId = bundle.getString(Const.MSG_KEY_CLIENTID);
            switch (msg.what) {
                case Const.MSG_TYPE_ONLINE: {
                    log("收到(" + clientId + ")上线");
                    onOnline(clientId, bundle);
                    break;
                }
                case Const.MSG_TYPE_OFFLINE: {
                    String data = bundle.getString(Const.MSG_KEY_DATA);
                    log("收到下线(" + clientId + ")，原因：" + data);
                    onOffline(clientId, data);

                    break;
                }
                case Const.MSG_TYPE_KICKOUT: {
                    String data = bundle.getString(Const.MSG_KEY_DATA);
                    log("收到踢下线(" + clientId + ")，原因：" + data);
                    onOffline(clientId, data);
                    break;
                }
                case Const.MSG_TYPE_MSG: {
                    String data = bundle.getString(Const.MSG_KEY_DATA);
                    String action = bundle.getString(Const.MSG_KEY_ACTION);
                    log("收到普通消息(" + clientId + ")[" + action + "]：" + data);
                    onReceivedMsg(clientId, data, action);
                    break;
                }
                case Const.MSG_TYPE_REQUEST: {
                    String action = bundle.getString(Const.MSG_KEY_ACTION);
                    String data = bundle.getString(Const.MSG_KEY_DATA);
                    String msgId = bundle.getString(Const.MSG_KEY_MSGID);
                    log("收到请求消息(" + clientId + ")[" + action + "]：" + data);
                    onRequest(clientId, data, action, msgId);
                    break;
                }
                case Const.MSG_TYPE_RESPONSE: {
                    String action = bundle.getString(Const.MSG_KEY_ACTION);
                    String data = bundle.getString(Const.MSG_KEY_DATA);
                    String msgId = bundle.getString(Const.MSG_KEY_MSGID);
                    log("收到回复消息(" + clientId + ")[" + action + "]：" + data);
                    onResponse(clientId, data, action, msgId);
                    break;
                }
                case Const.MSG_TYPE_HEART: {
                    break;
                }
                default: {
                    log("收到未知消息类型：" + msg.what);
                }
            }

        }

        // 上线
        private static void onOnline(String clientId, Bundle bundle) {
            if (mClientId.equals(clientId)) { // 自己上线
                mIsOnline = true;
                ArrayList<String> ids = bundle.getStringArrayList(Const.MSG_KEY_DATA);
                initClientIds(ids);
                if (mListener != null) {
                    if (ids != null) {
                        String[] idArray = new String[ids.size()];
                        ids.toArray(idArray);
                        mListener.selfOnline(clientId, idArray);
                    } else {
                        mListener.selfOnline(clientId, new String[0]);
                    }
                }
                onlineOk();
            } else { // 不是自己
                addClientId(clientId);
                if (mListener != null) {
                    mListener.otherOnline(clientId);
                }
            }
        }


        // 下线
        private static void onOffline(String clientId, String msg) {
            if (mClientId.equals(clientId)) {
                log("是自己下线");
                if (mListener != null) {
                    mListener.selfOffline(clientId, msg);
                }
            } else {
                log("是他人下线");
                if (mListener != null) {
                    mListener.otherOffline(clientId, msg);
                }
            }
        }

        // 收到消息
        private static void onReceivedMsg(String clientId, String msg, String action) {
            if (mClientId.equals(clientId)) {
                if (mListener != null) {
                    mListener.selfMsg(clientId, msg, action);
                }
            } else {
                if (mListener != null) {
                    mListener.otherMsg(clientId, msg, action);
                }
            }
        }

        private static void onRequest(String clientId, String msg, String action, String msgId) {
            if (!mClientId.equals(clientId)) {
                if (mListener != null) {
                    mListener.onRequest(clientId, msg, action, msgId);
                }
            }
        }

        private static void onResponse(String clientId, String msg, String action, String msgId) {
            if (!mClientId.equals(clientId)) {
                mLockDataManager.setLockOk(msgId, msg);
            }
        }

        // 初始化客户列表
        private static void initClientIds(ArrayList<String> ids) {
            synchronized (mClientIds) {
                mClientIds.clear();
                if (ids == null) {
                    return;
                }

                mClientIds.addAll(ids);
            }
        }

        // 添加客户
        private static void addClientId(String clientId) {
            synchronized (mClientIds) {
                if (TextUtils.isEmpty(clientId)) {
                    return;
                }

                if (!mClientIds.contains(clientId)) {
                    mClientIds.add(clientId);
                }
            }
        }
    }

    static String[] getOnlineClientIds() {
        synchronized (mClientIds) {
            log("当前在线人数：" + mClientIds.size());
            String[] array = new String[mClientIds.size()];
            ConnClientImp.mClientIds.toArray(array);
            return array;
        }
    }

    private static void sendOnline() throws RemoteException, ExecutionException {
        log("sendOnline");

        send(Const.MSG_TYPE_ONLINE, null, null, null);

    }

    /**
     * 注意：不要在主线程调用
     * @param data
     * @param action
     * @param timeout 设置超时时间，单位秒
     * @return
     * @throws RemoteException
     */
    static String request(String data, String action, long timeout) throws RemoteException, InterruptedException, ExecutionException, TimeoutException {
        try {
            String msgId = LockDataManager.getUniqueId();
            send(Const.MSG_TYPE_REQUEST, data, action, msgId);
            return mLockDataManager.get(msgId, timeout);
        } catch (RemoteException e) {
            log("request msg(" + action + ") RemoteException: " + e);
            throw e;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw e;
        }
    }

    static void response(String data, String msgId) throws RemoteException, ExecutionException {
        send(Const.MSG_TYPE_RESPONSE, data, null, msgId);
    }

    static void send(int msgType, String data, String action, String msgId) throws RemoteException, ExecutionException {
        if (mSendMessenger != null) {
            Message message = Message.obtain();
            message.what = msgType;
            message.replyTo = mReceiveMessenger;
            Bundle bundle = new Bundle();
            bundle.putString(Const.MSG_KEY_CLIENTID, mClientId);
            if (data != null) {
                bundle.putString(Const.MSG_KEY_DATA, data);
            }
            if (action != null) {
                bundle.putString(Const.MSG_KEY_ACTION, action);
            }
            if (msgId != null) {
                bundle.putString(Const.MSG_KEY_MSGID, msgId);
            }
            message.setData(bundle);
            try {
                mSendMessenger.send(message);
            } catch (RemoteException e) {
                log("send msg(" + msgType + ") RemoteException: " + e);
                throw e;
            }
        } else {
            log("send msg(" + msgType + ") when is not conn");
            throw new ExecutionException(new Throwable("没有连接服务器"));
        }

    }

    private static void onConnected() {
        log("onConnected()");
        try {
            sendOnline();
        } catch (RemoteException e) {
            // 连接关闭了（刚连上就关闭，不正常）
            log("连接关闭了（刚连上就关闭，不正常）");
        }
        catch (ExecutionException e) {
            log("执行上线错误，重新执行：" + e);
            onConnected();
        }
    }

    private static void onDisConnected() {
        mIsOnline = false;
        log("onDisConnected()");
        startService();
    }

    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            log("onServiceConnected");
            mSendMessenger = new Messenger(service);
            onConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            log("onServiceDisconnected");
            mSendMessenger = null;
            onDisConnected();
        }

        @Override
        public void onBindingDied(ComponentName name) {
            log("onBindingDied");
            mSendMessenger = null;
            onDisConnected();
        }
    };

    static void waitOnlineOk() throws InterruptedException {
        while (!mIsOnline) {
            waitOnline();
        }
    }

    private static void waitOnline() throws InterruptedException {
        if (!mIsOnline) {
            try {
                mOnlineLock.addLock();
            } catch (InterruptedException e) {
                log("等待上线，操作被打断");
                throw e;
            } catch (TimeoutException e) {
                log("等待上线超时");
            }
        }
    }

    private static void onlineOk() {
        mOnlineLock.unLock();
    }



    private static void log(String msg) {
        Log.e("AppConn_ConnClient_" + appName, "[" + mClientId + "]: " + msg);
    }
}
