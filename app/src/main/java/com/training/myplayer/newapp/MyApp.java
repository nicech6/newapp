package com.training.myplayer.newapp;/*
 *
 * Copyright (C) 2022 NIO Inc
 *
 * Ver   Date        Author    Desc
 *
 * V1.0  2022/6/11  hai.cui  Add for
 *
 */

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
    static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.context = base;
        UdpServerSocket.INSTANCE.start();
    }
}
