package com.kingskys.conn;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ConnService extends Service {
    private static ReceiveHandler mReceiveHandler = new ReceiveHandler();
    private static Messenger mMessenger = new Messenger(mReceiveHandler);

    public static int iCount = 0;

    public ConnService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        return mMessenger.getBinder();
    }

    private static class ReceiveHandler extends Handler {
        private final HashMap<Messenger, String> clients = new HashMap<>();

        @Override
        public void handleMessage(Message msg) {
            Messenger messenger = msg.replyTo;

            switch (msg.what) {
                case Const.MSG_TYPE_ONLINE: {
                    if (!haveClient(messenger)) {
                        Bundle bundle = msg.getData();
                        String clientId = bundle.getString(Const.MSG_KEY_CLIENTID);
                        addClient(messenger, clientId);
                        notifyOnline(messenger, clientId);
                        listenClientOffline(messenger, clientId);
                    }
                    break;
                }
                case Const.MSG_TYPE_OFFLINE: {
                    if (haveClient(messenger)) {
                        Bundle bundle = msg.getData();
                        String clientId = bundle.getString(Const.MSG_KEY_CLIENTID);
                        notifyOffline(messenger, clientId, "正常下线");
                        removeClient(messenger);
                    }
                    break;
                }
                case Const.MSG_TYPE_MSG: {
                    if (haveClient(messenger)) {
                        Bundle bundle = msg.getData();
                        String clientId = bundle.getString(Const.MSG_KEY_CLIENTID);
                        String data = bundle.getString(Const.MSG_KEY_DATA);
                        String action = bundle.getString(Const.MSG_KEY_ACTION);
                        notifyMsg(messenger, clientId, data, action);
                    }
                    break;
                }
                case Const.MSG_TYPE_REQUEST: {
                    if (haveClient(messenger)) {
                        Bundle bundle = msg.getData();
                        String clientId = bundle.getString(Const.MSG_KEY_CLIENTID);
                        String data = bundle.getString(Const.MSG_KEY_DATA);
                        String action = bundle.getString(Const.MSG_KEY_ACTION);
                        String msgId = bundle.getString(Const.MSG_KEY_MSGID);
                        notifyRequest(messenger, clientId, data, action, msgId);
                    }
                    break;
                }
                case Const.MSG_TYPE_RESPONSE: {
                    if (haveClient(messenger)) {
                        Bundle bundle = msg.getData();
                        String clientId = bundle.getString(Const.MSG_KEY_CLIENTID);
                        String data = bundle.getString(Const.MSG_KEY_DATA);
                        String action = bundle.getString(Const.MSG_KEY_ACTION);
                        String msgId = bundle.getString(Const.MSG_KEY_MSGID);
                        notifyResponse(messenger, clientId, data, action, msgId);
                    }
                    break;
                }
                case Const.MSG_TYPE_HEART: {
                    if (!haveClient(messenger)) {
                        Bundle bundle = msg.getData();
                        String clientId = bundle.getString(Const.MSG_KEY_CLIENTID);
                        notifyKickout(messenger, clientId, "请先登录，再发心跳");
                        removeClient(messenger);
                    }
                    break;
                }
                default: { // 未知的消息
                    log("未知的消息: " + msg.what);
                }
            }

        }

        private boolean haveClient(Messenger messenger) {
            synchronized (clients) {
                return clients.containsKey(messenger);
            }
        }

        private void removeClient(Messenger messenger) {
            synchronized (clients) {
                clients.remove(messenger);
            }
        }

        private void addClient(Messenger messenger, String clientId) {
            synchronized (clients) {
                clients.put(messenger, clientId);
            }
        }

        private String get(Messenger messenger) {
            synchronized (clients) {
                return clients.get(messenger);
            }
        }

        private void notifyOnline(Messenger messenger, String clientId) {
            log("客户端上线：" + clientId);
            synchronized (clients) {
                ArrayList<String> ids = new ArrayList<>(clients.values());

                Message message = Message.obtain();
                message.what = Const.MSG_TYPE_ONLINE;
                Bundle bundle = new Bundle();
                bundle.putString(Const.MSG_KEY_CLIENTID, clientId);

                message.setData(bundle);
                for (Messenger m : clients.keySet()) {
                    if (messenger.equals(m)) {
                        continue;
                    }
                    try {
                        m.send(message);
                    } catch (RemoteException e) {
                        log("服务器推送上线消息失败: " + clients.get(m));
                    }
                }

                bundle.putStringArrayList(Const.MSG_KEY_DATA, ids);
                message.setData(bundle);
                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    log("服务器推送自己上线消息失败: " + clients.get(messenger));
                }
            }
        }

        private void notifyOffline(Messenger messenger, String clientId, String msg) {
            log("客户端下线(" + clientId + "): " + msg);
            synchronized (clients) {
                Message message = Message.obtain();
                message.what = Const.MSG_TYPE_OFFLINE;
                Bundle bundle = new Bundle();
                bundle.putString(Const.MSG_KEY_CLIENTID, clientId);
                bundle.putString(Const.MSG_KEY_DATA, msg);
                message.setData(bundle);
                for (Messenger m : clients.keySet()) {
                    try {
                        m.send(message);
                    } catch (RemoteException e) {
                        log("服务器推送下线消息失败: " + clients.get(m));
                    }
                }
            }
        }

        private void notifyKickout(Messenger messenger, String clientId, String msg) {
            log("客户端被踢下线(" + clientId + "): ");
            synchronized (clients) {
                Message message = Message.obtain();
                message.what = Const.MSG_TYPE_KICKOUT;
                Bundle bundle = new Bundle();
                bundle.putString(Const.MSG_KEY_CLIENTID, clientId);
                bundle.putString(Const.MSG_KEY_DATA, msg);
                message.setData(bundle);
                for (Messenger m : clients.keySet()) {
                    try {
                        m.send(message);
                    } catch (RemoteException e) {
                        log("服务器推送下线消息失败: " + clients.get(m));
                    }
                }
            }
        }

        private void notifyMsg(Messenger messenger, String clientId, String msg, String action) {
            log("客户端消息(" + clientId + ")(act:" + action + "): " + msg);
            synchronized (clients) {
                Message message = Message.obtain();
                message.what = Const.MSG_TYPE_MSG;
                Bundle bundle = new Bundle();
                bundle.putString(Const.MSG_KEY_CLIENTID, clientId);
                bundle.putString(Const.MSG_KEY_DATA, msg);
                if (action != null) {
                    bundle.putString(Const.MSG_KEY_ACTION, action);
                }
                message.setData(bundle);
                for (Messenger m : clients.keySet()) {
                    try {
                        m.send(message);
                    } catch (RemoteException e) {
                        log("服务器推送普通消息失败: " + clients.get(m));
                    }
                }
            }
        }

        private void notifyRequest(Messenger messenger, String clientId, String msg, String action, String msgId) {
            log("客户端请求消息(c:" + clientId + ")(act:" + action + ")(msgId:" + msgId + "): " + msg);
            synchronized (clients) {
                Message message = Message.obtain();
                message.what = Const.MSG_TYPE_REQUEST;
                Bundle bundle = new Bundle();
                bundle.putString(Const.MSG_KEY_CLIENTID, clientId);
                bundle.putString(Const.MSG_KEY_DATA, msg);
                bundle.putString(Const.MSG_KEY_ACTION, action);
                bundle.putString(Const.MSG_KEY_MSGID, msgId);
                message.setData(bundle);
                for (Messenger m : clients.keySet()) {
                    try {
                        m.send(message);
                    } catch (RemoteException e) {
                        log("服务器推送请求消息失败: " + clients.get(m));
                    }
                }
            }
        }

        private void notifyResponse(Messenger messenger, String clientId, String msg, String action, String msgId) {
            log("客户端回复消息(c:" + clientId + ")(act:" + action + ")(msgId:" + msgId + "): " + msg);
            synchronized (clients) {
                Message message = Message.obtain();
                message.what = Const.MSG_TYPE_RESPONSE;
                Bundle bundle = new Bundle();
                bundle.putString(Const.MSG_KEY_CLIENTID, clientId);
                bundle.putString(Const.MSG_KEY_DATA, msg);
                bundle.putString(Const.MSG_KEY_ACTION, action);
                bundle.putString(Const.MSG_KEY_MSGID, msgId);
                message.setData(bundle);
                for (Messenger m : clients.keySet()) {
                    try {
                        m.send(message);
                    } catch (RemoteException e) {
                        log("服务器推送回复消息失败: " + clients.get(m));
                    }
                }
            }
        }

        private void listenClientOffline(final Messenger messenger, final String clientId) {
            try {
                messenger.getBinder().linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        removeClient(messenger);
                        notifyOffline(messenger, clientId, "与客户端失去连接");
                    }
                }, 0);
            } catch (RemoteException e) {
                removeClient(messenger);
                notifyOffline(messenger, clientId, "监听离线失败");
            }
        }
    }

    private static void log(String msg) {
        Log.e("AppConn_Server", msg);
    }
}
