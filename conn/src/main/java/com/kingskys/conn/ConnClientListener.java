package com.kingskys.conn;

public interface ConnClientListener {
    // 自己上线
    void selfOnline(String clientId, String[] onlineIds);
    // 他人上线
    void otherOnline(String clientId);

    // 自己下线
    void selfOffline(String clientId, String msg);
    // 他人下线
    void otherOffline(String clientId, String msg);

    // 自己普通消息
    void selfMsg(String clientId, String msg, String action);
    // 他人普通消息
    void otherMsg(String clientId, String msg, String action);

    // 请求消息
    void onRequest(String clientId, String msg, String action, String msgId);

}
