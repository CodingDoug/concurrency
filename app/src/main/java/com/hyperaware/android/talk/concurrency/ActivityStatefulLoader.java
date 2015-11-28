package com.hyperaware.android.talk.concurrency;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hyperaware.android.talk.concurrency.loader.ResultOrException;
import com.hyperaware.android.talk.concurrency.loader.StatefulExecutorServiceLoader;
import com.hyperaware.android.talk.concurrency.logging.Logging;

/**
 * An example of how to use a Loader to load a string resource into a
 * TextView, pretending that the operation would block for some time.
 */

public class ActivityStatefulLoader extends FragmentActivity {

    private static final String LOG_TAG = ActivityStatefulLoader.class.getSimpleName();
    private static final int LOADER_ID = 1;

    private TextView tvContent;
    private Button buttonLoadNow;


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
        setupLoader();
    }

    private void initViews() {
        setContentView(R.layout.activity_stateful_loader);
        tvContent = (TextView) findViewById(R.id.tv_content);
        buttonLoadNow = (Button) findViewById(R.id.button_load_now);
        buttonLoadNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadNow();
            }
        });
    }

    private void setupLoader() {
        // Get the currently active loader (returns null if not previously
        // initialized)
        final Loader<ResultOrException<String, Exception>> loader = getSupportLoaderManager().getLoader(LOADER_ID);
        final StatefulLoader statefulLoader = (StatefulLoader) loader;

        // If the loader is active...
        if (statefulLoader != null) {
            // Reattach to it so it can continue where it left off
            initStatefulLoader();

            // And update the UI to match the state
            switch (statefulLoader.getState()) {
            case Init:
                break;
            case Loading:
                updateUiLoading();
                break;
            case Loaded:
                break;
            }
        }
    }

    private void initStatefulLoader() {
        final TvResPair pair = new TvResPair(tvContent, R.string.content_loaded);
        getSupportLoaderManager().initLoader(LOADER_ID, null, new StatefulLoaderCallbacks(pair));
    }

    private void loadNow() {
        // Update UI to indicate a load is in progress
        updateUiLoading();
        // Destroy any existing loader that had been run
        getSupportLoaderManager().destroyLoader(LOADER_ID);
        // Init a new loader
        initStatefulLoader();
    }

    private void updateUiLoading() {
        tvContent.setText(R.string.waiting);
        buttonLoadNow.setEnabled(false);
    }


    /**
     * Loader callbacks that define the instance of the loader to use and what
     * to do when the loader completes.
     */

    private class StatefulLoaderCallbacks implements LoaderCallbacks<ResultOrException<String, Exception>> {
        private final TvResPair pair;

        public StatefulLoaderCallbacks(final TvResPair pair) {
            this.pair = pair;
        }

        @Override
        public Loader<ResultOrException<String, Exception>> onCreateLoader(final int id, final Bundle args) {
            return new StatefulLoader(ActivityStatefulLoader.this, pair.res);
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

            buttonLoadNow.setEnabled(true);
        }

        @Override
        public void onLoaderReset(final Loader<ResultOrException<String, Exception>> loader) {
        }
    }


    private static class StatefulLoader extends StatefulExecutorServiceLoader<String> {
        private final int resource;

        public StatefulLoader(final Context context, final int resource) {
            super(context);
            this.resource = resource;
        }

        @Override
        protected ResultOrException<String, Exception> onLoadInBackgroundStateful() {
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
