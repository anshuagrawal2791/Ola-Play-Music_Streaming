package com.ola.hackerearth.ola;

import android.app.Application;
import android.content.Context;

/**
 * Created by anshu on 30/10/15.
 */
public class MyApp extends Application {
    private static MyApp inst;

    @Override
    public void onCreate() {
        super.onCreate();
        inst = this;
    }
    public static MyApp getInst(){
        return inst;

    }
    public static Context getAppContext()
    {
        return inst.getApplicationContext();
    }}
