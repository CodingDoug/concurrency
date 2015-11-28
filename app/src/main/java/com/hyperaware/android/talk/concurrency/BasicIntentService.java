package com.hyperaware.android.talk.concurrency;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.hyperaware.android.talk.concurrency.logging.Logging;

/**
 * A simple IntentService that demonstrates how you might perform a background
 * HTTP download to a file determined by the client.
 *
 * See ActivityBasicIntentService for the client side of this.
 */

public class BasicIntentService extends IntentService {

    private static final String LOG_TAG = BasicIntentService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1;

    private static final String NAMESPACE = BasicIntentService.class.getName();
    public static final String ACTION_START_DOWNLOAD = NAMESPACE + ".START_DOWNLOAD";
    public static final String ACTION_DOWNLOAD_COMPLETE = NAMESPACE + ".DOWNLOAD_COMPLETE";
    public static final String ACTION_DOWNLOAD_ERROR = NAMESPACE + ".DOWNLOAD_ERROR";
    public static final String EXTRA_URL = NAMESPACE + ".url";
    public static final String EXTRA_FILE = NAMESPACE + ".file";
    public static final String EXTRA_EXTRAS = NAMESPACE + ".extras";


    public BasicIntentService() {
        super(BasicIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Logging.logDebugThread(LOG_TAG, "Handling an intent: " + intent);

        final String action = intent.getAction();
        final Bundle extras = intent.getExtras();
        if (ACTION_START_DOWNLOAD.equals(action) && extras != null) {
            final String url = extras.getString(EXTRA_URL);
            final String file = extras.getString(EXTRA_FILE);

            showNotification(url);
            try {
                downloadUrlToFile(url, file);
                broadcastCompletion(intent);
            }
            catch (final Exception e) {
                broadcastError(intent, e);
            }
            removeNotification();
        }
    }

    private void showNotification(final String url) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
            .setSmallIcon(R.drawable.ic_launcher)
            .setTicker(getResources().getString(R.string.downloading_x, url));
        startForeground(NOTIFICATION_ID, builder.build());
        // Or use NotificationManager if foreground isn't needed
    }

    private void removeNotification() {
        stopForeground(true);
        // Or use NotificationManager if foreground isn't needed
    }

    private void downloadUrlToFile(final String url, final String file) throws Exception {
        Logging.logDebugThread(LOG_TAG, "Downloading " + url + "to " + file);
        // Put your real work here
        SystemClock.sleep(5000);
    }

    private void broadcastCompletion(final Intent intent) {
        final Bundle extras = intent.getExtras();
        final Intent status = new Intent(ACTION_DOWNLOAD_COMPLETE);
        status.putExtra(EXTRA_EXTRAS, extras);
        sendBroadcast(status);
    }

    private void broadcastError(final Intent intent, final Exception e) {
        final Bundle extras = intent.getExtras();
        final Intent status = new Intent(ACTION_DOWNLOAD_ERROR);
        status.putExtra(EXTRA_EXTRAS, extras);
        status.putExtra(EXTRA_EXTRAS, e);
        sendBroadcast(status);
    }

}
