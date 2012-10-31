package se.magnulund.android.currencyconverter;

import android.content.ClipData;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import se.magnulund.android.currencyconverter.utils.SuperUtil;

public class MainActivity extends Activity implements OnItemSelectedListener {

    private EditText input;
    private EditText output;
    private Spinner toSpinner;
    private Spinner fromSpinner;
    private TextView timestamp;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        input = (EditText) findViewById(R.id.amount);

        output = (EditText) findViewById(R.id.result);

        timestamp = (TextView) findViewById(R.id.time_label);

        SharedPreferences currencies = getSharedPreferences("currencies", 0);

        SuperUtil.loadCurrencies(currencies.getString("data", SuperUtil.getDefaultCurrencies(getResources().openRawResource(R.raw.currencies))));

        toSpinner = (Spinner) findViewById(R.id.to_currency);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this, R.layout.spinnerlayout, SuperUtil.getCurrencies());
        toAdapter.setDropDownViewResource(R.layout.dropdownlayout);
        toSpinner.setAdapter(toAdapter);
        toSpinner.setSelection(SuperUtil.getCurrencyIndex(currencies.getString("to", "USD")));
        toSpinner.setOnItemSelectedListener(this);

        fromSpinner = (Spinner) findViewById(R.id.from_currency);
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this, R.layout.spinnerlayout, SuperUtil.getCurrencies());
        fromAdapter.setDropDownViewResource(R.layout.dropdownlayout);
        fromSpinner.setAdapter(fromAdapter);
        fromSpinner.setSelection(SuperUtil.getCurrencyIndex(currencies.getString("from", "EUR")));
        fromSpinner.setOnItemSelectedListener(this);

        timestamp.setText("Currency timestamp: " + SuperUtil.getCurrencyTimestamp());

        input.setText(currencies.getString("amount", "1"));

        output.setText(SuperUtil.getResult(input, toSpinner.getSelectedItem().toString(), fromSpinner.getSelectedItem().toString()));

        input.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                output.setText(SuperUtil.getResult(input, toSpinner.getSelectedItem().toString(), fromSpinner.getSelectedItem().toString()));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    final SuperUtil.ISuperUtil callback = new SuperUtil.ISuperUtil() {
        public void updateReceived() {
            runOnUiThread(new Runnable() {
                public void run() {
                    timestamp.setText("New Currency timestamp: " + SuperUtil.getCurrencyTimestamp());
                    output.setText(SuperUtil.getResult(input, toSpinner.getSelectedItem().toString(), fromSpinner.getSelectedItem().toString()));
                }
            });
        }
    };

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        output.setText(SuperUtil.getResult(input, toSpinner.getSelectedItem().toString(), fromSpinner.getSelectedItem().toString()));
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setDisplayShowTitleEnabled(false);
        getMenuInflater().inflate(R.menu.activity_main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_update:
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                SuperUtil.updateCurrencies(cm, callback);
                return true;
            case R.id.menu_reverse:
                int toIndex = toSpinner.getSelectedItemPosition();
                int fromIndex = fromSpinner.getSelectedItemPosition();
                toSpinner.setSelection(fromIndex);
                fromSpinner.setSelection(toIndex);
                output.setText(SuperUtil.getResult(input, toSpinner.getSelectedItem().toString(), fromSpinner.getSelectedItem().toString()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
        SharedPreferences settings = getSharedPreferences("currencies", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("data", SuperUtil.getCurrencyData());
        editor.putString("to", toSpinner.getSelectedItem().toString());
        editor.putString("from", fromSpinner.getSelectedItem().toString());
        editor.putString("amount", input.getText().toString());
        editor.commit();

    }
}
