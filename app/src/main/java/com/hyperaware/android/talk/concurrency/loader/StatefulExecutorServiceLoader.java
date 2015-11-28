package com.hyperaware.android.talk.concurrency.loader;

import java.util.concurrent.ExecutorService;

import android.content.Context;

public abstract class StatefulExecutorServiceLoader<T> extends ExecutorServiceLoader<T> {

    private volatile State state;

    public enum State {
        Init, Loading, Loaded
    }

    public StatefulExecutorServiceLoader(final Context context) {
        super(context);
        this.state = State.Init;
    }

    public StatefulExecutorServiceLoader(final Context context, final ExecutorService executor) {
        super(context, executor);
        this.state = State.Init;
    }

    @Override
    protected final ResultOrException<T, Exception> onLoadInBackground() {
        state = State.Loading;
        final ResultOrException<T, Exception> roe = onLoadInBackgroundStateful();
        state = State.Loaded;
        return roe;
    }

    protected abstract ResultOrException<T, Exception> onLoadInBackgroundStateful();

    public State getState() {
        return state;
    }

}
