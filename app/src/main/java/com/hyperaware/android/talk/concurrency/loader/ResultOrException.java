package com.hyperaware.android.talk.concurrency.loader;

/**
 * Data structure containing either some expected results or an exception
 * indicating a problem getting results.  Users should ALWAYS call hasResult()
 * to figure out which one is present before assuming one or the other.
 *
 * @param <T>
 * @param <E>
 */

public class ResultOrException<T, E extends Exception> {

    private final boolean hasResult;
    private final T result;
    private final E exception;

    public ResultOrException(final T result) {
        this.hasResult = true;
        this.result = result;
        this.exception = null;
    }

    public ResultOrException(final E e) {
        this.hasResult = false;
        this.result = null;
        this.exception = e;
    }

    public boolean hasResult() {
        return hasResult;
    }

    public T getResult() {
        return result;
    }

    public E getException() {
        return exception;
    }


    @Override
    public String toString() {
        return
            "{" +
            "hasResult=" + hasResult +
            " result=" + result +
            " exception=" + exception +
            "}";
    }

}
