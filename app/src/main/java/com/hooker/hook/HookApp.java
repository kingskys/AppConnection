package com.hooker.hook;

import android.content.Context;
import android.util.Log;

public class HookApp {
    public static void hooker(ClassLoader classLoader, Context context) throws Throwable {
        hook(classLoader);
    }

    private static void hook(final ClassLoader classLoader) throws Throwable {

    }






    private static void log(String msg) {
        Log.d("tag", "hooker - " + msg);
    }
}
