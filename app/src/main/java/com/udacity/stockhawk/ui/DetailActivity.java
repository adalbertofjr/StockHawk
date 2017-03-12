package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * DetailActivity
 * Created by Adalberto Fernandes Júnior on 11/03/17.
 * Copyright © 2016. All rights reserved.
 */

public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        OnChartValueSelectedListener {
    private static final int STOCK_LOADER = 0;

    @BindView(R.id.symbol)
    TextView mSymbol;

    @BindView(R.id.price)
    TextView mPrice;

    @BindView(R.id.date)
    TextView mDate;

    private DecimalFormat dollarFormat;
    private LineChart mChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        // in this example, a LineChart is initialized from xml
        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setOnChartValueSelectedListener(this);

        mChart.getXAxis().setDrawLabels(false);
        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getLegend().setEnabled(true);
        mChart.getDescription().setEnabled(false);
        mChart.animateX(2500);

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader started...");
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

        if (!cursor.isLast()) {
            cursor.moveToNext();
        }

        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);

        mSymbol.setText(symbol);
        mSymbol.setContentDescription(getString(R.string.a11y_stock, symbol));

        mPrice.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

        String history = cursor.getString(Contract.Quote.POSITION_HISTORY);
        getHistory(history);
    }

    private void getHistory(String history) {
        String[] dateHistorys = history.split("\n");
        Map<Long, Float> pricesHistory = new HashMap<>();

        List<Entry> entries = new ArrayList<Entry>();

        for (String priceHistory : dateHistorys) {
            long date = Long.parseLong(priceHistory.split(",")[0]);
            float price = Float.parseFloat(priceHistory.split(",")[1]);
            pricesHistory.put(date, price);
            entries.add(new Entry(date, price));
        }

        Collections.sort(entries, new EntryXComparator());

        LineDataSet dataSet = new LineDataSet(entries, mSymbol.getText().toString()); // add entries to dataset
        LineData lineData = new LineData(dataSet);

        mChart.setData(lineData);
        mDate.setText(DateUtils.formatDateTime(this, (long) lineData.getXMax(), DateUtils.FORMAT_NUMERIC_DATE));
        mPrice.setText(dollarFormat.format(lineData.getYMax()));

        mChart.invalidate(); // refresh
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        mDate.setText(DateUtils.formatDateTime(this, (long) e.getX(), DateUtils.FORMAT_NUMERIC_DATE));
        mPrice.setText(dollarFormat.format(e.getY()));
    }

    @Override
    public void onNothingSelected() {

    }
}
