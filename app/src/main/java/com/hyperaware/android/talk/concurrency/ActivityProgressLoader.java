package com.hyperaware.android.talk.concurrency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
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

public class ActivityProgressLoader extends FragmentActivity {

    private static final String LOG_TAG = ActivityProgressLoader.class.getSimpleName();
    private static final int LOADER_ID = 1;

    private View vProgressContainer;
    private Button buttonLoadNow;
    private TextView tvProgress;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        attachLoader();
    }

    private void initViews() {
        setContentView(R.layout.activity_progress_loader);
        buttonLoadNow = (Button) findViewById(R.id.button_load_now);
        buttonLoadNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                loadNow();
            }
        });
        vProgressContainer = findViewById(R.id.v_progress_container);
        tvProgress = (TextView) findViewById(R.id.tv_progress);
    }

    private void attachLoader() {
        // Get the currently active loader (returns null if not previously
        // initialized)
        final Loader<ResultOrException<Void, Exception>> loader = getSupportLoaderManager().getLoader(LOADER_ID);
        final ProgressLoader progressLoader = (ProgressLoader) loader;

        // If the loader is active...
        if (progressLoader != null) {
            // Reattach to it so it can continue where it left off
            initProgressLoader();
            updateUiLoading();

            // And update the UI to match the state
            switch (progressLoader.getState()) {
            case Init:
                break;
            case Loading:
                updateUiStart();
                updateUiProgress(progressLoader.getCurrent(), progressLoader.getTotal());
                break;
            case Loaded:
                updateUiEnd();
                break;
            }
        }

        final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(startReceiver, new IntentFilter(ProgressLoader.ACTION_START));
        lbm.registerReceiver(progressReceiver, new IntentFilter(ProgressLoader.ACTION_PROGRESS));
        lbm.registerReceiver(endReceiver, new IntentFilter(ProgressLoader.ACTION_END));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Android won't warn you about LocalBroadcastManager receiver
        // leaks, so don't forget to couple all registers with an unregister.
        final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(startReceiver);
        lbm.unregisterReceiver(progressReceiver);
        lbm.unregisterReceiver(endReceiver);
    }

    private void initProgressLoader() {
        getSupportLoaderManager().initLoader(LOADER_ID, null, new ProgressLoaderCallbacks());
    }

    private void loadNow() {
        // Update UI to indicate a load is in progress
        updateUiLoading();
        // Destroy any existing loader that had been run
        getSupportLoaderManager().destroyLoader(LOADER_ID);
        // Init a new loader
        initProgressLoader();
    }

    private void updateUiLoading() {
        buttonLoadNow.setEnabled(false);
    }

    private void updateUiStart() {
        vProgressContainer.setVisibility(View.VISIBLE);
        tvProgress.setText("");
    }

    private void updateUiProgress(final int current, final int total) {
        tvProgress.setText(current + "/" + total);
    }

    private void updateUiEnd() {
        vProgressContainer.setVisibility(View.GONE);
    }

    /**
     * Handle ProgressLoader.ACTION_START by showing the progress UI.
     */
    private final BroadcastReceiver startReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateUiStart();
        }
    };


    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int current = intent.getIntExtra(ProgressLoader.EXTRA_CURRENT, 0);
            final int total = intent.getIntExtra(ProgressLoader.EXTRA_TOTAL, 0);
            updateUiProgress(current, total);
        }
    };


    private final BroadcastReceiver endReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateUiEnd();
        }
    };

    /**
     * Loader callbacks that define the instance of the loader to use and what
     * to do when the loader completes.
     */

    private class ProgressLoaderCallbacks implements LoaderCallbacks<ResultOrException<Void, Exception>> {
        public ProgressLoaderCallbacks() {
        }

        @Override
        public Loader<ResultOrException<Void, Exception>> onCreateLoader(final int id, final Bundle args) {
            return new ProgressLoader(ActivityProgressLoader.this);
        }

        @Override
        public void onLoadFinished(final Loader<ResultOrException<Void, Exception>> loader, final ResultOrException<Void, Exception> result) {
            Logging.logDebugThread(LOG_TAG, "Content loaded.");
            buttonLoadNow.setEnabled(true);
            getSupportLoaderManager().destroyLoader(LOADER_ID);
        }

        @Override
        public void onLoaderReset(final Loader<ResultOrException<Void, Exception>> loader) {
        }
    }


    private static class ProgressLoader extends StatefulExecutorServiceLoader<Void> {
        public static final String NAMESPACE = ProgressLoader.class.getName() + '.';
        public static final String ACTION_START = NAMESPACE + "ACTION_START";
        public static final String ACTION_PROGRESS = NAMESPACE + "ACTION_PROGRESS";
        public static final String ACTION_END = NAMESPACE + "ACTION_END";
        public static final String EXTRA_CURRENT = NAMESPACE + "EXTRA_CURRENT";
        public static final String EXTRA_TOTAL = NAMESPACE + "EXTRA_TOTAL";

        private static final int ITERATIONS = 10;

        private volatile int current;
        private volatile int total;

        public ProgressLoader(final Context context) {
            // This context may be an Activity instance, but it will not be stored.
            super(context);
        }

        @Override
        protected ResultOrException<Void, Exception> onLoadInBackgroundStateful() {
            current = 0;
            total = ITERATIONS;

            // Use LocalBroadcastManager to advertise changes in state and
            // progress.
            final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            lbm.sendBroadcast(new Intent(ACTION_START));

            for (int i = 0; i < ITERATIONS; i++) {
                final int cur = i + 1;
                current = cur;
                Logging.logDebugThread(LOG_TAG, "Doing work unit " + cur);
                final Intent intent = new Intent(ACTION_PROGRESS);
                intent.putExtra(EXTRA_CURRENT, cur);
                intent.putExtra(EXTRA_TOTAL, ITERATIONS);
                lbm.sendBroadcast(intent);

                // No actual work here, just delay
                SystemClock.sleep(1000);
            }

            lbm.sendBroadcast(new Intent(ACTION_END));
            return new ResultOrException<>((Void) null);
        }

        public int getCurrent() {
            return current;
        }

        public int getTotal() {
            return total;
        }
    }

}
