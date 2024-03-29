package hr.vsite.sensor;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final String SENSOR_API_URL_STRING = "http://10.0.2.2:5000/api/values";
    final int SENSOR_SAMPLING_PERIOD_US = 10 * 1000000;

    private SensorManager sensorManager;
    private SensorAPI sensorAPI;
    private Sensor at_sensor, rh_sensor;
    private float ambientTemperature, relativeHumidity;
    private long samples = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        sensorAPI = new SensorAPI(this);
        if(sensorAPI.setURL(SENSOR_API_URL_STRING) == false){
            Toast.makeText(this, "Invalid SensorAPI URL!", Toast.LENGTH_LONG);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        at_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        rh_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (at_sensor != null) {
            sensorManager.registerListener(this, at_sensor, SENSOR_SAMPLING_PERIOD_US);
        } else disableTempSensor();
        if(rh_sensor != null) {
            sensorManager.registerListener(this, rh_sensor, SENSOR_SAMPLING_PERIOD_US);
        } else disableHumSensor();
        if(at_sensor == null && rh_sensor == null) {
            Toast.makeText(this, "No available sensors!", Toast.LENGTH_LONG);
        } else sensorManager.flush(this);
    }

    public void disableTempSensor(){
        findViewById(R.id.tv_temp_label).setEnabled(false);
        findViewById(R.id.tv_temp_val).setEnabled(false);
        findViewById(R.id.tv_temp_unit).setEnabled(false);
    }

    public void disableHumSensor(){
        findViewById(R.id.tv_hum_label).setEnabled(false);
        findViewById(R.id.tv_hum_unit).setEnabled(false);
        findViewById(R.id.tv_hum_val).setEnabled(false);
    }

    public void apiButton(View v){
        if(sensorAPI.apiOK && sensorAPI.apiCached.longValue()>0) {
            Toast.makeText(this,"Sending cached values...",Toast.LENGTH_SHORT).show();
            sensorAPI.storeCached();
        } else if(!sensorAPI.apiOK){
            Toast.makeText(this,"API not available!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values.length > 0) {
            Button bt_api = findViewById(R.id.bt_apistatus);
            if(samples > 5) {
                bt_api.setTextColor(sensorAPI.apiOK ? Color.GREEN : Color.RED);
            }
            if (at_sensor.equals(sensorEvent.sensor)) {
                ambientTemperature = sensorEvent.values[0];
                TextView tv_temperature = findViewById(R.id.tv_temp_val);
                tv_temperature.setText(String.format("%.1f", ambientTemperature));
                sensorAPI.storeValue(2, ambientTemperature);
                samples += 1;
            }
            if (rh_sensor.equals(sensorEvent.sensor)) {
                relativeHumidity = sensorEvent.values[0];
                TextView tv_humidity = findViewById(R.id.tv_hum_val);
                tv_humidity.setText(String.format("%.1f", relativeHumidity));
                sensorAPI.storeValue(3, relativeHumidity);
                samples += 1;
            }
            TextView tv_cached = findViewById(R.id.tv_cached);
            tv_cached.setText(String.format("%d values, %d cached", samples, sensorAPI.apiCached.longValue()));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
