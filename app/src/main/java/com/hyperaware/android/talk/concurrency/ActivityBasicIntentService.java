package com.hyperaware.android.talk.concurrency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Sample client activity that delegates work to BasicIntentService.
 */

public class ActivityBasicIntentService extends FragmentActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceivers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceivers();
    }


    private void initViews() {
        setContentView(R.layout.activity_basic_intent_service);

        findViewById(R.id.button_download_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(ActivityBasicIntentService.this, BasicIntentService.class);
                intent.setAction(BasicIntentService.ACTION_START_DOWNLOAD);
                intent.putExtra(BasicIntentService.EXTRA_URL, "http://url/one");
                intent.putExtra(BasicIntentService.EXTRA_FILE, "/path/to/file");
                startService(intent);
            }
        });

        findViewById(R.id.button_download_three).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(ActivityBasicIntentService.this, BasicIntentService.class);
                intent.setAction(BasicIntentService.ACTION_START_DOWNLOAD);
                intent.putExtra(BasicIntentService.EXTRA_URL, "http://url/one");
                intent.putExtra(BasicIntentService.EXTRA_FILE, "/path/to/file");
                startService(intent);
                intent = new Intent(ActivityBasicIntentService.this, BasicIntentService.class);
                intent.setAction(BasicIntentService.ACTION_START_DOWNLOAD);
                intent.putExtra(BasicIntentService.EXTRA_URL, "http://url/two");
                intent.putExtra(BasicIntentService.EXTRA_FILE, "/path/to/file");
                startService(intent);
                intent = new Intent(ActivityBasicIntentService.this, BasicIntentService.class);
                intent.setAction(BasicIntentService.ACTION_START_DOWNLOAD);
                intent.putExtra(BasicIntentService.EXTRA_URL, "http://url/three");
                intent.putExtra(BasicIntentService.EXTRA_FILE, "/path/to/file");
                startService(intent);
            }
        });
    }


    private void registerReceivers() {
        registerReceiver(
            downloadCompleteReceiver,
            new IntentFilter(BasicIntentService.ACTION_DOWNLOAD_COMPLETE)
        );
    }


    private void unregisterReceivers() {
        unregisterReceiver(downloadCompleteReceiver);
    }


    private final BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final Bundle extras = intent.getExtras();
            final Bundle orig_extras = extras.getBundle(BasicIntentService.EXTRA_EXTRAS);
            final String url = orig_extras.getString(BasicIntentService.EXTRA_URL);
            Toast.makeText(context, "Url downloaded: " + url, Toast.LENGTH_SHORT).show();
        }
    };

}
