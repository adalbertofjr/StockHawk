package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.common.collect.ImmutableList;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * DetailWidgetRemoteViewsService
 * Created by Adalberto Fernandes Júnior on 18/03/17.
 * Copyright © 2016. All rights reserved.
 */

public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    public static final ImmutableList<String> QUOTE_COLUMNS = ImmutableList.of(
            Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE
    );

    // these indices must match the projection
    public static final int POSITION_ID = 0;
    public static final int POSITION_SYMBOL = 1;
    public static final int POSITION_PRICE = 2;
    public static final int POSITION_ABSOLUTE_CHANGE = 3;
    public static final int POSITION_PERCENTAGE_CHANGE = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            public DecimalFormat percentageFormat;
            public DecimalFormat dollarFormatWithPlus;
            public DecimalFormat dollarFormat;

            @Override
            public void onCreate() {
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri weatherForLocationUri = Contract.Quote.URI;
                data = getContentResolver().query(weatherForLocationUri,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String symbol = data.getString(POSITION_SYMBOL);
                String price = dollarFormat.format(data.getFloat(POSITION_PRICE));

                int symbolIdView = R.id.symbol;
                int priceIdView = R.id.price;
                int changeIdView = R.id.change;

                views.setTextViewText(symbolIdView, symbol);
                views.setContentDescription(symbolIdView, symbol);
                views.setTextViewText(priceIdView, price);
                views.setContentDescription(priceIdView, price);

                float rawAbsoluteChange = data.getFloat(POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(POSITION_PERCENTAGE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    views.setInt(changeIdView, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(changeIdView, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                if (PrefUtils.getDisplayMode(getApplicationContext())
                        .equals(getString(R.string.pref_display_mode_absolute_key))) {
                    views.setTextViewText(changeIdView, change);
                } else {
                    views.setTextViewText(changeIdView, percentage);
                }

                final Intent fillInIntent = new Intent();
                Uri uriSymbol = Contract.Quote.makeUriForStock(symbol);
                fillInIntent.setData(uriSymbol);

                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
