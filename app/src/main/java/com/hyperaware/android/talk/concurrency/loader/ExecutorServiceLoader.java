package com.hyperaware.android.talk.concurrency.loader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.util.Log;

/**
 * A loader that puts work onto threads controlled by an ExecutorService.
 *
 * @param <D>
 *     the type that this loader generates
 */

public abstract class ExecutorServiceLoader<D> extends Loader<ResultOrException<D, Exception>> {

    private static final String LOG_TAG = ExecutorServiceLoader.class.getSimpleName();
    private static final boolean LOG_DEBUG = false;

    private static ExecutorService defaultExecutorService;

    private final Handler handler = new Handler();
    private final ExecutorService executor;

    // The following members must only be accessed on the UI thread

    // Loaded data store
    private ResultOrException<D, Exception> data;
    // A handle for canceling the load and determining if in progress
    private Future<Void> future;


    /**
     * Creates a new ExecutorServiceLoader whose background work is delegated
     * to a global Executor using a fixed thread pool with max(1,numcores-1)
     * threads set at background priority.
     *
     * @param context context (Activity) where the loader is being used
     */

    public ExecutorServiceLoader(final Context context) {
        this(context, null);
    }


    /**
     * Creates a new ExecutorServiceLoader whose background work is delegated
     * to the given ExecutorService.  That ExecutorService should probably be
     * manged as a singleton.  Passing null for executor is the same as using
     * the other constructor.
     *
     * @param context context (Activity) where this Loader is being used
     * @param executor the Executor used to perform this Loader's work
     */

    public ExecutorServiceLoader(final Context context, final ExecutorService executor) {
        super(context);

        if (executor == null) {
            synchronized (ExecutorServiceLoader.class) {
                if (defaultExecutorService == null) {
                    // The default executor service will create threads at background priority
                    final ThreadFactory tf = new PrioritizedThreadFactory(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    final int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                    defaultExecutorService = Executors.newFixedThreadPool(threads, tf);
                }
            }
            this.executor = defaultExecutorService;
        }
        else {
            this.executor = executor;
        }
    }


    /**
     * Subclasses must implement this to perform the background loading.
     *
     * @return
     *     a ResultOrException with the results of the load
     */

    protected abstract ResultOrException<D, Exception> onLoadInBackground();


    @Override
    protected void onStartLoading() {
        if (LOG_DEBUG) { Log.d(LOG_TAG, "onStartLoading " + this); }
        super.onStartLoading();

        if (data != null) {
            if (LOG_DEBUG) { Log.v(LOG_TAG, "Delivering existing result=" + data); }
            deliverResult(data);
        }
        else if (takeContentChanged() || future == null) {
            forceLoad();
        }
        else {
            if (LOG_DEBUG) { Log.v(LOG_TAG, "Doing nothing"); }
        }
    }


    @Override
    protected void onForceLoad() {
        if (LOG_DEBUG) { Log.d(LOG_TAG, "onForceLoad " + this); }
        super.onForceLoad();

        try {
            if (future != null) {
                future.cancel(true);
            }
            future = executor.submit(new RunTaskCallable());
        }
        catch (final RejectedExecutionException e) {
            // This can happen if the executor service was shut down
            deliverResult(new ResultOrException<D, Exception>(e));
            Log.e(LOG_TAG, "", e);
        }
    }


    @Override
    protected void onStopLoading() {
        if (LOG_DEBUG) { Log.d(LOG_TAG, "onStopLoading " + this); }
        super.onStopLoading();
    }


    @Override
    protected void onAbandon() {
        if (LOG_DEBUG) { Log.d(LOG_TAG, "onAbandon " + this); }
        super.onAbandon();
    }


    @Override
    protected void onReset() {
        if (LOG_DEBUG) { Log.d(LOG_TAG, "onReset " + this); }
        super.onReset();

        if (future != null) {
            if (LOG_DEBUG) { Log.d(LOG_TAG, "canceling work"); }
            future.cancel(true);
        }
        data = null;
        future = null;
    }


    private void dispatchOnLoadComplete(final ResultOrException<D, Exception> data) {
        future = null;

        if (data == null) {
            throw new NullPointerException("Loader returned null ResultOrException");
        }

        this.data = data;  // but remember the result

        if (isAbandoned()) {
            if (LOG_DEBUG) { Log.d(LOG_TAG, "Loader was abandoned, not delivering"); }
        }
        else if (isStarted()) {
            if (LOG_DEBUG) { Log.v(LOG_TAG, "Delivering new result=" + data); }
            deliverResult(data);
        }
        else {
            if (LOG_DEBUG) { Log.d(LOG_TAG, "Loader was stopped, not delivering"); }
        }
    }


    private class RunTaskCallable implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            ResultOrException<D, Exception> d;
            try {
                d = onLoadInBackground();
            }
            catch (final Exception e) {
                d = new ResultOrException<>(e);
            }

            final ResultOrException<D, Exception> d2 = d;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dispatchOnLoadComplete(d2);
                }
            });
            return null;
        }
    }

}
