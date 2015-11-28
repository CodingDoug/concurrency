package com.hyperaware.android.talk.concurrency;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityMusicCursorLoader extends FragmentActivity {

    private static final int LOADER_ID = 1;

    private ListView lvMusic;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        loadMusic();
    }

    private void initViews() {
        setContentView(R.layout.activity_music_cursor_loader);
        lvMusic = (ListView) findViewById(R.id.lv_music);
    }

    private void loadMusic() {
        // TODO Show some kind of progress indicator before loading
        getSupportLoaderManager().initLoader(LOADER_ID, null, new MyLoaderCallbacks());
    }


    public class MyLoaderCallbacks implements LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            return new CursorLoader(
                ActivityMusicCursorLoader.this,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { AudioColumns._ID, AudioColumns.ARTIST, AudioColumns.ALBUM, AudioColumns.TITLE },
                null,
                null,
                AudioColumns.ARTIST
            );
        }

        @Override
        public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
            lvMusic.setAdapter(new MusicListAdapter(ActivityMusicCursorLoader.this, data, 0));
            // TODO Hide a progress indicator, if present
        }

        @Override
        public void onLoaderReset(final Loader<Cursor> loader) {
        }
    }


    private class MusicListAdapter extends CursorAdapter {

        public MusicListAdapter(final Context context, final Cursor c, final int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.row_music, parent);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final String title = cursor.getString(cursor.getColumnIndex(AudioColumns.TITLE));
            final String artist = cursor.getString(cursor.getColumnIndex(AudioColumns.ARTIST));
            final String album = cursor.getString(cursor.getColumnIndex(AudioColumns.ALBUM));

            ((TextView) view.findViewById(R.id.tv_title)).setText(title);
            ((TextView) view.findViewById(R.id.tv_artist)).setText(artist);
            ((TextView) view.findViewById(R.id.tv_album)).setText(album);
        }

    }

}
