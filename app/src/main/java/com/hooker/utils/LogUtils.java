package com.hooker.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class LogUtils {
    private final static String TAGNAME = "hooker";
    private static final String i_default_flag = "- " + TAGNAME + " - hooker ";
    private static final String i_default_tag = TAGNAME;

    public static void log(String msg) {
        log(i_default_tag, i_default_flag, msg);
    }

    public static void logE(String msg) {
        logE(i_default_tag, i_default_flag, msg);
    }


    private static void log(String tag, String flag, String msg) {
//        synchronized (LogUtils.class) {
//            int length = 2000;
//            if (msg.length() <= length) {
//                Log.d(tag, flag + msg);
//            } else {
//                int idx = 0;
//                while (idx < msg.length()) {
//                    int endIdx = Math.min(msg.length(), idx + length);
//                    String logmsg = msg.substring(idx, endIdx);
//                    Log.d(tag, flag + logmsg);
//                    idx = endIdx;
//                }
//            }
//        }
        Log.e(tag, flag + msg);
    }

    private static void logE(String tag, String flag, String msg) {
        Log.e(tag, flag + msg);
//        synchronized (LogUtils.class) {
//            int length = 2000;
//            if (msg.length() <= length) {
//                Log.e(tag, flag + msg);
//            } else {
//                int idx = 0;
//                while (idx < msg.length()) {
//                    int endIdx = Math.min(msg.length(), idx + length);
//                    String logmsg = msg.substring(idx, endIdx);
//                    Log.e(tag, flag + logmsg);
//                    idx = endIdx;
//                }
//            }
//        }
    }

    public static void dumpRunningProcess(Context context) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> list = manager.getRunningAppProcesses();
            log("------------------------------");
            log("running process count = " + list.size());
            int idx = 0;
            for (ActivityManager.RunningAppProcessInfo info : list) {
                log(String.format("info[%d] process = %s", idx++, info.processName));
            }
            log("------------------------------");
        }
        catch (Exception e) {
            log("error = " + e);
        }
    }

    public static void dumpTrack(String flag) {
        String text = " dump " + " ============= dumpTrack " + flag + " \n\b" + Log.getStackTraceString(new Throwable());

        log(text);
    }

    public static void dumpTrack() {
        String text = " dump " + " ============= dumpTrack \n\b" + Log.getStackTraceString(new Throwable());

        log(text);
    }

    public static String getDumpArgs(Object[] args) {
        return getDumpArgs(args, null);
    }

    // 打印参数，className 为需要详细打印的类
    public static String getDumpArgs(Object[] args, List<String> classNames) {
        try {
            if (args == null) {
                return "args is empty";
            }

            StringBuilder builder = new StringBuilder();
            builder.append("args[");
            builder.append(args.length);
            builder.append("] = {");

            for (int i = 0; i < args.length; i++) {
                builder.append("\n [");
                builder.append(i);
                builder.append("]");
                if (args[i] == null) {
                    builder.append(" == null");
                } else if (Modifier.isInterface(args[i].getClass().getModifiers())) {
                    builder.append(" interface = ");
                    builder.append(args[i].getClass());
                } else if (args[i] instanceof String) {
                    builder.append(" string = ");
                    builder.append(args[i]);
                } else {
                    builder.append(" obj = ");
                    if (classNames != null && containsString(classNames, args[i].getClass().getName())) {
                        builder.append(getObjectAllFiled(args[i], classNames));
                    } else {
                        builder.append(args[i]);
                    }
                }

            }

            if (args.length > 0) {
                builder.append("\n}");
            } else {
                builder.append("}");
            }

            return builder.toString();
        }
        catch (Throwable e) {
            return "getDumpArgs error = " + e;
        }
    }

    private static boolean containsString(List<String> strs, String subStr) {
        if (strs == null) {
            return false;
        }

        for (String str : strs) {
            if (str.equals(subStr)) {
                return true;
            }
        }

        return false;
    }

    /** 获取对象所有的成员变量
     *
     * @param object 需要获取所有成员变量的对象
     * @param classNames 成员变量的类名集合。成员变量中，需要获取其所有成员变量的对象的类名
     * @return 字符串结果
     * @throws Throwable
     */
    public static String getObjectAllFiled(Object object, List<String> classNames) throws Throwable {
        if (object == null) {
            return "null";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("class = ");
        stringBuilder.append(object.getClass().getName());
        stringBuilder.append("\n{");
        Class<?> clazz = object.getClass();
        if (clazz.getName().equals("java.lang.Object")) {
            stringBuilder.append("java.lang.Object}");
            return stringBuilder.toString();
        }

        Field[] declaredFields = clazz.getDeclaredFields();
        while (declaredFields != null) {
            for (Field field : declaredFields) {
                stringBuilder.append("\n\t");
                int mod = field.getModifiers();
                stringBuilder.append((mod == 0) ? "" : (Modifier.toString(mod) + " "));
                stringBuilder.append(field.getType().getName());
                stringBuilder.append(" ");
                stringBuilder.append(field.getName());

//                stringBuilder.append(" = ");

                try {
                    String name = field.getName();
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value == null) {
                        stringBuilder.append(" == null");
                    } else if (classNames != null && containsString(classNames, value.getClass().getName())) {
                        stringBuilder.append(" = ");
                        stringBuilder.append(getObjectAllFiled(value, classNames));
                    } else {
                        stringBuilder.append(" = ");
                        stringBuilder.append(String.valueOf(value));
                    }
                }
                catch (Throwable e) {
                    stringBuilder.append("- err = ");
                    stringBuilder.append(e.toString());
                }
            }

            stringBuilder.append("\n---------\n");

            clazz = clazz.getSuperclass();
            if (clazz == null) {
                break;
            }

            if (clazz.getName().equals("java.lang.Object")) {
                stringBuilder.append("java.lang.Object");
                break;
            }

            declaredFields = clazz.getDeclaredFields();
        }

        stringBuilder.append("\n\b}\n--");
        return stringBuilder.toString();
    }
}
