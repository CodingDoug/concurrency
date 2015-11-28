package com.hyperaware.android.talk.concurrency;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.TextView;

import com.hyperaware.android.talk.concurrency.fictionalapi.FictionalAsyncApi;
import com.hyperaware.android.talk.concurrency.loader.ExecutorServiceLoader;
import com.hyperaware.android.talk.concurrency.loader.ResultOrException;
import com.hyperaware.android.talk.concurrency.logging.Logging;

import java.util.concurrent.CountDownLatch;

/**
 * An example of how to use a Loader with a fictional asynchronous API.
 */

public class ActivityAsyncApiLoader extends FragmentActivity {

    private static final String LOG_TAG = ActivityAsyncApiLoader.class.getSimpleName();

    private static final int LOADER_ID = 1;

    private TextView tvContent;


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
        getSupportLoaderManager().initLoader(LOADER_ID, null, new FictionalApiLoaderCallbacks());
    }


    /**
     * Loader callbacks that define the instance of the loader to use and what
     * to do when the loader completes.
     */

    private class FictionalApiLoaderCallbacks implements LoaderCallbacks<ResultOrException<String, Exception>> {

        @Override
        public Loader<ResultOrException<String, Exception>> onCreateLoader(final int id, final Bundle args) {
            return new FictionalApiLoader(ActivityAsyncApiLoader.this);
        }

        @Override
        public void onLoadFinished(final Loader<ResultOrException<String, Exception>> loader, final ResultOrException<String, Exception> result) {
            Logging.logDebugThread(LOG_TAG, "Content loaded.");
            // When the load is complete, update the UI.
            if (result.hasResult()) {
                tvContent.setText(result.getResult());
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
     * A simple loader that invokes a fictional asynchronous API and waits for
     * its result.
     */

    private static class FictionalApiLoader extends ExecutorServiceLoader<String> {

        public FictionalApiLoader(final Context context) {
            // This context may be an Activity instance, but it will not be stored.
            super(context);
        }

        @Override
        protected ResultOrException<String, Exception> onLoadInBackground() {
            // Use a latch to wait on the results of the async call
            final CountDownLatch latch = new CountDownLatch(1);

            // Async results will be placed in this container object
            class ResultContainer {
                String result;
                Exception exception;
            }
            final ResultContainer container = new ResultContainer();

            // Make the async call; it should return immediately without blocking
            final FictionalAsyncApi api = new FictionalAsyncApi();
            api.asyncCall(new FictionalAsyncApi.Callback() {
                @Override
                public void onComplete(final String result) {
                    // When the call completes, stash the result in a container object
                    // and notify the loader thread of completion.
                    container.result = result;
                    latch.countDown();
                }
            });

            try {
                // This will block the loader thread until the callback is called by the
                // async api.
                latch.await();
            }
            catch (final InterruptedException e) {
                container.exception = e;
            }

            if (container.exception != null) {
                return new ResultOrException<>(container.exception);
            }
            else {
                return new ResultOrException<>(container.result);
            }
        }
    }

}
