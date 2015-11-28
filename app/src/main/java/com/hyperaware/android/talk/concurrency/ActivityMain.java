package com.hyperaware.android.talk.concurrency;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ActivityMain extends Activity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_basic_thread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityBasicThread.class));
            }
        });

        findViewById(R.id.button_basic_asynctask).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityBasicAsyncTask.class));
            }
        });

        findViewById(R.id.button_basic_loader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityBasicLoader.class));
            }
        });

        findViewById(R.id.button_invalid_nonstatic_loader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityInvalidNonStaticLoader.class));
            }
        });

        findViewById(R.id.button_stateful_loader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityStatefulLoader.class));
            }
        });

        findViewById(R.id.button_progress_loader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityProgressLoader.class));
            }
        });

        findViewById(R.id.button_music_cursor_loader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityMusicCursorLoader.class));
            }
        });

        findViewById(R.id.button_async_api_loader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityAsyncApiLoader.class));
            }
        });

        findViewById(R.id.button_basic_intentservice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(ActivityMain.this, ActivityBasicIntentService.class));
            }
        });
    }

}
