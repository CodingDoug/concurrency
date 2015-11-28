package com.hyperaware.android.talk.concurrency.logging;

import android.util.Log;

public class Logging {

    public static void logDebugThread(final String tag, final String s) {
        final Thread t = Thread.currentThread();
        Log.d(tag, "Thread " + t.getId() + " " + t.getName() + ": " + s);
    }

}
