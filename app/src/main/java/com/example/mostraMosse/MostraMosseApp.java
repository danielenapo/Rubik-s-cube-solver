package com.example.mostraMosse;

import android.app.Application;
import android.util.Log;

import timber.log.BuildConfig;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class MostraMosseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if(priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
            // TODO log to Firebase or something like that
        }
    }
}
