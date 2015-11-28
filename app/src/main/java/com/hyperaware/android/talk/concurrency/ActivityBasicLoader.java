package com.hyperaware.android.talk.concurrency;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.TextView;

import com.hyperaware.android.talk.concurrency.loader.ExecutorServiceLoader;
import com.hyperaware.android.talk.concurrency.loader.ResultOrException;
import com.hyperaware.android.talk.concurrency.logging.Logging;

/**
 * An example of how to use a Loader to load a string resource into a
 * TextView, pretending that the operation would block for some time.
 */

public class ActivityBasicLoader extends FragmentActivity {

    private static final String LOG_TAG = ActivityBasicLoader.class.getSimpleName();

    private static final int LOADER_ID = 1;

    private TextView tvContent;


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


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initLoader();
    }

    private void initViews() {
        setContentView(R.layout.activity_async_load);
        tvContent = (TextView) findViewById(R.id.tv_content);
    }

    private void initLoader() {
        final TvResPair pair = new TvResPair(tvContent, R.string.content_loaded);
        getSupportLoaderManager().initLoader(LOADER_ID, null, new StringResourceLoaderCallbacks(pair));
    }


    /**
     * Loader callbacks that define the instance of the loader to use and what
     * to do when the loader completes.
     */

    private class StringResourceLoaderCallbacks implements LoaderCallbacks<ResultOrException<String, Exception>> {
        private final TvResPair pair;

        public StringResourceLoaderCallbacks(final TvResPair pair) {
            this.pair = pair;
        }

        @Override
        public Loader<ResultOrException<String, Exception>> onCreateLoader(final int id, final Bundle args) {
            return new StringResourceLoader(ActivityBasicLoader.this, pair.res);
        }

        @Override
        public void onLoadFinished(final Loader<ResultOrException<String, Exception>> loader, final ResultOrException<String, Exception> result) {
            Logging.logDebugThread(LOG_TAG, "Content loaded.");
            // When the load is complete, update the UI.
            if (result.hasResult()) {
                pair.tv.setText(result.getResult());
            }
            else {
                Exception e = result.getException();
                Log.e("OOPS", "", e);
                // TODO show an error
            }
        }

        @Override
        public void onLoaderReset(final Loader<ResultOrException<String, Exception>> loader) {
        }
    }


    /**
     * A simple loader that just looks up a string resource by its id.
     * If a loader is an inner class, IT MUST BE STATIC so it will not
     * accidentally leak an Activity instance!
     */

    private static class StringResourceLoader extends ExecutorServiceLoader<String> {
        private final int resource;

        public StringResourceLoader(final Context context, final int resource) {
            // This context may be an Activity instance, but it will not be stored.
            super(context);
            this.resource = resource;
        }

        @Override
        protected ResultOrException<String, Exception> onLoadInBackground() {
            // Do blocking or lengthy work here and return the result
            Logging.logDebugThread(LOG_TAG, "Loading content in 5 seconds");
            SystemClock.sleep(5000);

            // The context that the loader stores is an Application so that the
            // activity where it is used will not leak.
            final Context context = getContext();
            return new ResultOrException<>(context.getResources().getString(resource));
        }
    }

}
