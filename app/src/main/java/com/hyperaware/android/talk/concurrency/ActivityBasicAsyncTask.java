package com.hyperaware.android.talk.concurrency;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import com.hyperaware.android.talk.concurrency.logging.Logging;

/**
 * An example of how to use an AsyncTask to load a string resource into a
 * TextView, pretending that the operation would block for some time.
 */

public class ActivityBasicAsyncTask extends Activity {

    private static final String LOG_TAG = ActivityBasicAsyncTask.class.getSimpleName();

    private TextView tvContent;
    private BasicAsyncTask asyncTask;


    /**
     * Defines a mapping between a TextView and the string resource we want to
     * load into it using an AsyncTask.
     */
    private static class TvResPair {
        public final TextView tv;
        public final int res;
        public TvResPair(final TextView tv, final int res) {
            this.tv = tv;
            this.res = res;
        }
    }

    /**
     * Defines a mapping between a TextView and the String that was loaded
     * from a resource using an AsyncTask.  The UI thread will populate
     * the TextView with the String.
     */
    private static class TvStringPair {
        public final TextView tv;
        public final String string;
        public TvStringPair(final TextView tv, final String string) {
            this.tv = tv;
            this.string = string;
        }
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        // Start loading the string resource into TextView using an AsyncTask.
        final TvResPair pair = new TvResPair(tvContent, R.string.content_loaded);
        asyncTask = new BasicAsyncTask(this);
        asyncTask.execute(pair);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asyncTask.cancel(true);
    }

    private void initViews() {
        setContentView(R.layout.activity_async_load);
        tvContent = (TextView) findViewById(R.id.tv_content);
    }


    private static class BasicAsyncTask extends AsyncTask<TvResPair, TvStringPair, Void> {
        private final WeakReference<Activity> wact;
        public BasicAsyncTask(final Activity act) {
            this.wact = new WeakReference<>(act);
        }

        // This will execute on a background thread so we don't block the main thread
        @Override
        protected Void doInBackground(final TvResPair... params) {
            for (final TvResPair pair : params) {
                if (isCancelled()) {
                    Logging.logDebugThread(LOG_TAG, "Loading canceled");
                    break;
                }

                Logging.logDebugThread(LOG_TAG, "Loading content in 5 seconds");
                SystemClock.sleep(5000);
                final Activity act = wact.get();
                if (act != null) {
                    final String string = act.getResources().getString(pair.res);
                    Logging.logDebugThread(LOG_TAG, "Content loaded");

                    // Send the loaded string to onProgressUpdate for UI update
                    publishProgress(new TvStringPair(pair.tv, string));
                }
                else {
                    Logging.logDebugThread(LOG_TAG, "Activity is gone");
                    break;
                }
            }
            return null;
        }

        // This will execute on the main thread so we can update the UI
        @Override
        protected void onProgressUpdate(final TvStringPair... values) {
            for (final TvStringPair pair : values) {
                pair.tv.setText(pair.string);
            }
        }
    }

}
