package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

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

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int STOCK_LOADER = 0;

    @BindView(R.id.symbol)
    TextView mSymbol;

    @BindView(R.id.price)
    TextView mPrice;

    @BindView(R.id.change)
    TextView mChange;

    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;
    private LineChart mChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        // in this example, a LineChart is initialized from xml
        mChart = (LineChart) findViewById(R.id.chart);

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
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

        cursor.moveToNext();

        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);

        mSymbol.setText(symbol);
        mSymbol.setContentDescription(getString(R.string.a11y_stock, symbol));

        mPrice.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            mChange.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            mChange.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            mChange.setText(change);
        } else {
            mChange.setText(percentage);
        }

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
        mChart.getXAxis().setDrawLabels(false);
        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getLegend().setEnabled(true);
        mChart.getDescription().setEnabled(false);


        mChart.invalidate(); // refresh
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
