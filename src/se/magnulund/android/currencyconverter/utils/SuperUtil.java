package se.magnulund.android.currencyconverter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.EditText;

public class SuperUtil {

    private static final String TAG = "SuperUtil";

    private static Currencies currencies;

    public static String getDefaultCurrencies(InputStream is) {

        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "loadCurrencies " + "Encoding error");
        } catch (IOException e) {
            Log.e(TAG, "loadCurrencies " + "IOException");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "loadCurrencies " + "IOException - close");
            }
        }
        return writer.toString();
    }

    public static void loadCurrencies(String data) {

        try {
            currencies = (Currencies) Currencies.fromString(data);
        } catch (JSONException e) {
            Log.e(TAG, "loadCurrencies " + "JSON error");
        }

    }

    public static ArrayList<String> getCurrencies() {
        return currencies.getCurrencyArray();
    }

    public static int getCurrencyIndex(String currency_name) {
        return currencies.getIndex(currency_name);
    }

    public static String getCurrencyData() {
        return currencies.asString();
    }

    public static String getResult(EditText input, String toCurrency, String fromCurrency) {
        float fromValue;
        float toValue;
        float amount = 0;

        String inputText = input.getText().toString();
        if (inputText.length() > 0) {
            amount = Float.valueOf(inputText);
        }
        toValue = (Float) currencies.getRate(toCurrency);

        fromValue = (Float) currencies.getRate(fromCurrency);

        float result = amount * toValue / fromValue;
        return new DecimalFormat("#,###,###.####").format(result);
    }

    public static void updateCurrencies(ConnectivityManager cm, final SuperUtil.ISuperUtil callback) {

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {

            Thread sender = new Thread() {
                @Override
                public void run() {

                    URL url = null;
                    try {
                        url = new URL("http://openexchangerates.org/api/latest.json?app_id=226533ed841640eabc16645c93a3aa5a");
                    } catch (MalformedURLException e) {
                        Log.e(TAG, "updateCurrencies " + "Bad URL");
                    }
                    HttpURLConnection urlConnection = null;
                    try {
                        if (url != null) {
                            urlConnection = (HttpURLConnection) url.openConnection();
                        }
                    } catch (IOException e1) {
                        Log.e(TAG, "updateCurrencies " + "Connection Error");
                    }
                    if (urlConnection != null) {
                        BufferedReader in;

                        try {

                            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            String data = "";
                            String line;
                            while ((line = in.readLine()) != null) {
                                data += line;
                                //Log.e("DATA", data);
                            }
                            try {
                                currencies = (Currencies) Currencies.fromString(data);
                                callback.updateReceived();
                            } catch (JSONException e) {
                                Log.e(TAG, "updateCurrencies " + "JSON error");
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "updateCurrencies " + "Read error");
                        } finally {
                            urlConnection.disconnect();
                        }
                    }
                }
            };
            sender.start();
        }
    }

    public static String getCurrencyTimestamp() {

        long timestamp = (Long) currencies.getTimestamp();

        Date date = new Date(timestamp * 1000);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sdf.format(date);
    }

    public static interface ISuperUtil {
        public void updateReceived();
    }

}
