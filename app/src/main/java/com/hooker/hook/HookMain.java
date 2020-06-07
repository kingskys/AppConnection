package com.hooker.hook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.kingskys.conn.ConnClient;

import java.util.concurrent.ExecutionException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {

    private String targetPackageName = "com.work.proxydemo";
    private String targetProcessName = targetPackageName;
    private boolean ishook = false;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpp) throws Throwable {
//        log("package = " + lpp.packageName + " process = " + lpp.processName);

        if (lpp.appInfo == null || (lpp.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }

        if (lpp.packageName.equals(targetPackageName)) {
            if (targetProcessName.equals(lpp.processName)) {
                log("start hook " + targetProcessName);
                hooker(lpp);
            }
        }

    }

    private void hooker(final XC_LoadPackage.LoadPackageParam lpp) {
        try {
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (ishook) return;

                    if (lpp.processName.equals(targetPackageName)) {
                        log("Application attach");
                        ishook = true;

                        Context context = (Context) param.args[0];
                        ClassLoader classLoader = context.getClassLoader();

                        HookApp.hooker(classLoader, context);

                        ConnClient.init("002", "com.kingskys.appconnection", (Application)param.thisObject, new ConnCallback());

                        sendData("hooker started");

                        log("app 启动...");

                        activityListen((Application)param.thisObject, classLoader);
                    }

                }
            });
        } catch (Exception e) {

        }
    }



    private void sendData(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    log("start0 send data: " + msg);
//                    log("waitConnected");
                    ConnClient.waitOnlineOk();
//                    log("waitConnected ok");
                    log("start1 send data: " + msg);
                    ConnClient.send(msg);
                } catch (InterruptedException e) {
                    log("发送错误：操作被打断，停止发送");
                } catch (RemoteException e) {
                    log("发送错误：连接异常，停止发送");
                } catch (ExecutionException e) {
                    log("发送错误：执行异常，停止发送");
                }
            }
        }).start();
    }

    private static long preTime = 0;

    private void activityListen(Application application, final ClassLoader classLoader) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(final Activity activity) {
                String name = activity.getClass().getName();
                log("scene onResume - " + name);
//                ActivityUtils.setCurrentActivity(activity);
//                if (TextUtils.equals(name, "com.eg.android.AlipayGphone.AlipayLogin")) {
//                    log("start get list");
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
////                            String list = AlipayTools.getBillList(classLoader);
////                            log("bill list = " + list);
//                            log("start get list");
//                            log("pre time = " + preTime);
//                            if (System.currentTimeMillis() - preTime > 5000) {
//                                preTime = System.currentTimeMillis();
//                                log("cur time = " + preTime);
////                                String bizInNo = "20200306200040011100380064100715";
////                                String bizInNo = "20200223200040011100080063064415";
////                                String bizInNo = "100";
////                                String billInfo = AlipayTools.getBillInfo(classLoader, bizInNo);
////                                log("bill info = " + billInfo);
//
//                                log("start jump url");
//                                AlipayTools.jumpUrl(classLoader, activity);
//                            }
//                            else {
//                                log("操作太频繁了");
//                            }
//
//                        }
//                    }).start();
//
//                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                log("scene onPause - " + activity.getClass().getName());
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }


    private static void log(String msg) {
        Log.e("AppConn_hooker", msg);
    }
}
