package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import timber.log.Timber;

/**
 * DetailActivity
 * Created by Adalberto Fernandes Júnior on 11/03/17.
 * Copyright © 2016. All rights reserved.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int STOCK_LOADER = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String symbol = getIntent().getExtras().getString(Intent.EXTRA_TEXT);

        if (symbol == null) {
            return null;
        }

        String selection = Contract.Quote.COLUMN_SYMBOL + "=?";
        String[] selectionArgs = new String[]{symbol};

        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                selection,
                selectionArgs,
                Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            return;
        }

        cursor.moveToNext();

        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        Timber.i("Stock detail: " + symbol);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
