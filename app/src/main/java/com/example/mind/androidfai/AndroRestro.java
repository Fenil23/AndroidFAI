package com.example.mind.androidfai;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by mind on 1/2/17.
 */
public class AndroRestro extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
