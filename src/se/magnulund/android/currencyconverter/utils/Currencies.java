package se.magnulund.android.currencyconverter.utils;


import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 31/10/2012
 * Time: 12:01
 * To change this template use File | Settings | File Templates.
 */
public class Currencies {
    private static final String TAG = "Currencies";

    private JSONObject currencies;

    private ArrayList<String> currencies_list;

    private Currencies(String currencies) throws JSONException {
        this.currencies = new JSONObject(currencies);

        final ArrayList<String> array = new ArrayList<String>();
        try {
            Iterator iterator = this.currencies.getJSONObject("rates").keys();

            while ( iterator.hasNext() ) {
                array.add(iterator.next().toString());
            }
            Collections.sort(array);
        } catch (JSONException e1) {
            Log.e(TAG, "getCurrencies" + "JSON error");
        }
        this.currencies_list = array;

    }

    public static Currencies fromString(String currencies) throws JSONException {
        return new Currencies(currencies);
    }

    public Float getRate(String currency_name) {
        JSONObject rates;
        Float rate = null;
        try {
            rates = this.currencies.getJSONObject("rates");
            rate = (Float) Float.valueOf(rates.getString(currency_name));
        } catch (JSONException e) {
            Log.e(TAG, "getRate" + "JSON error");
        }
        return rate;
    }

    public JSONArray getCurrencies() {
        JSONArray jsonArray = null;
        try {
            jsonArray = (JSONArray) this.currencies.getJSONObject("rates").names();
        } catch (JSONException e1) {
            Log.e(TAG, "getCurrencies" + "JSON error");
        }
        return jsonArray;
    }

    public ArrayList<String> getCurrencyArray() {
        return currencies_list;
    }

    public Long getTimestamp() {

        long timestamp = 0;
        try {
            timestamp = (Long) this.currencies.getLong("timestamp");
        } catch (JSONException e) {
            Log.e(TAG, "getTimestamp" + "JSON error");
        }

        return timestamp;
    }

    public String asString() {
        return this.currencies.toString();
    }

    public int getIndex(String currency_name) {

        Iterator iterator = this.currencies_list.iterator();
        int i = 0;
        while (!currency_name.equals(iterator.next().toString())) {
            i++;
        }

        return i;
    }
}