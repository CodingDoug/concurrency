package com.hyperaware.android.talk.concurrency.fictionalapi;

import android.os.SystemClock;

public class FictionalAsyncApi {

    /**
     * A fictional async api call that generates via a callback on another
     * thread.
     */
    public void asyncCall(final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(3000);
                callback.onComplete("Here's the thing I wanted to tell you.");
            }
        }).start();
    }

    public interface Callback {
        void onComplete(String result);
    }

}
