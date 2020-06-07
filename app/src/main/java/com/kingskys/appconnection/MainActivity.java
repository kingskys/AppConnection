package com.kingskys.appconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kingskys.conn.ConnClient;
import com.kingskys.conn.ConnClientListener;
import com.kingskys.conn.ConnService;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private EditText mMsgInput = null;
    private TextView mOnlineLabel = null;
    private TextView mLogLabel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mMsgInput = findViewById(R.id.input_msg);
        mOnlineLabel = findViewById(R.id.label_onlines);
        mLogLabel = findViewById(R.id.label_msg);

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendData();
            }
        });
    }

    private void onSendData() {
        String text = mMsgInput.getText().toString().trim();
        if (text.isEmpty()) {
            text = "发送测试数据";
        }
        sendData(text);
    }

    private void sendData(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConnClient.waitOnlineOk();
                    ConnClient.send(msg);
                } catch (Throwable e) {
                    log("发送消息错误：" + e);
                }
            }
        }).start();

    }

    @Override
    public void onResume() {
        super.onResume();

        log("onResume() service count = " + ConnService.iCount);
        updateOnlines();
        register();
        request();
    }

    private void request() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    log("开始请求回复");
                    String result = ConnClient.request("请求回复", "back", 30);
                    log("请求回复 结果：" + result);
                } catch (InterruptedException e) {
                    log("请求回复错误：操作被打断，停止发送" + e);
                } catch (RemoteException e) {
                    log("请求回复错误：连接异常，停止发送" + e);
                } catch (ExecutionException e) {
                    log("请求回复错误：执行异常，停止发送" + e);
                } catch (TimeoutException e) {
                    log("请求回复错误：超时，停止发送" + e);
                }
            }
        }).start();
    }

    @Override
    public void onPause() {
        unRegister();

        super.onPause();
    }

    public void updateOnlines() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] clients = ConnClient.getOnlineClients();
                String msg = "在线:";
                for (String client : clients) {
                    msg += " " + client;
                }
                mOnlineLabel.setText(msg);
            }
        });
    }

    public void addLog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogLabel.append("\n" + msg);
            }
        });
    }

    private void register() {
        ConnCallback.setmActivity(this);
    }

    private void unRegister() {
        ConnCallback.setmActivity(null);
    }


    private static void log(String msg) {
        Log.e("AppConn_app", msg);
    }
}
