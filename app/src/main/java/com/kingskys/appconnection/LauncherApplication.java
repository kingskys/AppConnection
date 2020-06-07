package com.kingskys.appconnection;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.kingskys.conn.ConnClient;

public class LauncherApplication extends Application {
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        log("attachBaseContext");
        ConnClient.init("001", "com.kingskys.appconnection", this, new ConnCallback());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConnClient.waitOnlineOk();
                    log("app started wait end");
                    ConnClient.send("LauncherApplication started");
                } catch (Throwable e) {
                    log("app started wait err: " + e);
                }
            }
        }).start();
    }

    private static void log(String msg) {
        Log.e("AppConn_app", msg);
    }

}
