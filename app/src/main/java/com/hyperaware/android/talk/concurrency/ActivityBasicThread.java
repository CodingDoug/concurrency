package com.hyperaware.android.talk.concurrency;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import com.hyperaware.android.talk.concurrency.logging.Logging;

/**
 * A trivial example of a background thread that performs some work to
 * populate an activity's UI.
 *
 * Please don't actually create threads for work like this!
 * This is an anti-pattern demo!
 */

public class ActivityBasicThread extends Activity {

    private static final String LOG_TAG = ActivityBasicThread.class.getSimpleName();

    private TextView tvContent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        new BasicThread(R.string.content_loaded).start();
    }

    private void initViews() {
        setContentView(R.layout.activity_async_load);
        tvContent = (TextView) findViewById(R.id.tv_content);
    }


    /**
     * A trivial thread that loads a string resource into a TextView.
     */
    private class BasicThread extends Thread {
        private final int resId;

        public BasicThread(final int res_id) {
            this.resId = res_id;
        }

        @Override
        public void run() {
            Logging.logDebugThread(LOG_TAG, "Loading content in 5 seconds");
            SystemClock.sleep(5000);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvContent.setText(resId);
                    Logging.logDebugThread(LOG_TAG, "Content loaded");
                }
            });
        }
    }

}
