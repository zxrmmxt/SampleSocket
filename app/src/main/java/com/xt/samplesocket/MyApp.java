package com.xt.samplesocket;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * @author xt on 2020/4/3 16:12
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
