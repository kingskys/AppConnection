package com.hooker.hook;

import android.util.Log;

import com.kingskys.conn.ConnClient;
import com.kingskys.conn.ConnClientListener;

import java.util.Locale;

public class ConnCallback implements ConnClientListener {
    public ConnCallback() {
    }

    @Override
    public void selfOnline(String clientId, String[] onlineIds) {

    }

    @Override
    public void otherOnline(String clientId) {

    }

    @Override
    public void selfOffline(String clientId, String msg) {

    }

    @Override
    public void otherOffline(String clientId, String msg) {

    }

    @Override
    public void selfMsg(String clientId, String msg, String action) {

    }

    @Override
    public void otherMsg(String clientId, String msg, String action) {

    }

    // 请求消息
    public void onRequest(String clientId, String msg, String action, final String msgId) {
        log("hook脚本收到请求消息(c:" + clientId + ")(act:" + action + ")(msgId:" + msgId + "): " + msg);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long dur = (long)(Math.random() * 60 + 1);
                    log("计时等待" + dur + "秒后回复");
                    Thread.sleep(dur * 1000);
                    String data = String.format(Locale.ENGLISH, "等待了%d秒返回", dur);
                    log("返回消息: " + data);
                    ConnClient.response(data, msgId);
                } catch (Throwable e) {
                    log("返回消息时错误：" + e);
                }
            }
        }).start();
    }

    private static void log(String msg) {
        Log.e("AppConn_hooker_callback", msg);
    }
}