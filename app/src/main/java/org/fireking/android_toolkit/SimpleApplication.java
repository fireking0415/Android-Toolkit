package org.fireking.android_toolkit;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by fireking on 2017/10/8.
 */

public class SimpleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
    }
}
