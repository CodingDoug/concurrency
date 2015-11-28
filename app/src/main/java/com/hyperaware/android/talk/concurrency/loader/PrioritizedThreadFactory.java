package com.hyperaware.android.talk.concurrency.loader;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * A ThreadFactory that sets each of its new threads to a given priority.
 * Priority values must be taken from android.os.Process constants.
 */
public class PrioritizedThreadFactory implements ThreadFactory {

    private final int prio;

    public PrioritizedThreadFactory(final int prio) {
        this.prio = prio;
    }

    @Override
    public Thread newThread(@NonNull final Runnable r) {
        final Runnable prio_first = new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(prio);
                r.run();
            }
        };
        return new Thread(prio_first, "Prioritized Thread prio " + prio);
    }

}
