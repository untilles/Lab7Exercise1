package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    int tid;
    long ttime;

    public void buttonClicked(View v) {
        int id = v.getId();
        Toast t = Toast.makeText(getApplicationContext(), "Wait 1 minute", Toast.LENGTH_SHORT);

        WeatherTask w = new WeatherTask();
        switch (id) {
            case R.id.btBangkok:
                if (tid != R.id.btBangkok || System.currentTimeMillis()-ttime >= 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    ttime = System.currentTimeMillis();
                } else {
                    t.show();
                }

                break;
            case R.id.btNon:
                if (tid != R.id.btNon || System.currentTimeMillis()-ttime >= 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    ttime = System.currentTimeMillis();
                } else {
                    t.show();
                }

                break;
            case R.id.btPathum:
                if (tid != R.id.btPathum || System.currentTimeMillis()-ttime >= 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    ttime = System.currentTimeMillis();
                } else {
                    t.show();
                }

                break;
        }

        tid = id;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        double windSpeed;
        double temp;
        double maxTemp;
        double minTemp;
        double humid;
        String weather;


        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jWind = jWeather.getJSONObject("wind");

                    windSpeed = jWind.getDouble("speed");
                    weather = jWeather.getJSONArray("weather").getJSONObject(0).getString("main");
                    temp = jWeather.getJSONObject("main").getDouble("temp");
                    maxTemp = jWeather.getJSONObject("main").getDouble("temp_max");
                    minTemp = jWeather.getJSONObject("main").getDouble("temp_min");
                    humid = jWeather.getJSONObject("main").getDouble("humidity");
                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind, tvTemp, tvHumid;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvHumid = (TextView)findViewById(R.id.tvHumid);

            if (result) {

                double tempC = temp-272.15;
                double maxTempC = maxTemp-272.15;
                double MinTempC = minTemp-272.15;
                String tempS = String.format("%.1f", tempC) + "(max = " + String.format("%.1f", maxTempC) +
                               ", min = " + String.format("%.1f", MinTempC) + ")";

                tvTitle.setText(title);
                tvWeather.setText(weather);
                tvWind.setText(String.format("%.1f", windSpeed));
                tvTemp.setText(tempS);
                tvHumid.setText(String.format("%.0f%%", humid));
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
                tvTemp.setText("");
                tvHumid.setText("");
            }
        }
    }
}
