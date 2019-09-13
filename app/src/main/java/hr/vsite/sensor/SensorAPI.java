package hr.vsite.sensor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

import hr.vsite.sensor.DBContract.tCache;

public class SensorAPI {

    private URL apiValues;
    public boolean apiOK = false;
    public Context apiContext;
    public AtomicLong apiCached;

    public SensorAPI(Context context) {
        long rows = 0;
        try {
            DBHelper helper = new DBHelper(context);
            SQLiteDatabase dbase = helper.getReadableDatabase();
            rows = DatabaseUtils.queryNumEntries(dbase, tCache.TABLE_NAME);
            dbase.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        apiCached = new AtomicLong(rows);
        apiContext = context;
    }

    public boolean setURL(String urlString) {
        try {
            apiValues = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void storeValue(int sensorId, float value){
        String jsonString = "{\"Id\":0,\"SensorId\":"
                + String.format("%d", sensorId)
                + ",\"Timestamp\":"
                + String.format("%d", System.currentTimeMillis()/1000)
                + ",\"Value\":\""
                + String.format("%d", Math.round(value*1000)) +"\"}";
        new postJson().execute(jsonString);
    }

    public void storeCached(){
        try {
            DBHelper helper = new DBHelper(apiContext);
            SQLiteDatabase dbase = helper.getWritableDatabase();
            Cursor cursor = dbase.query(
                    tCache.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
            int col0 = cursor.getColumnIndexOrThrow("_ID");
            int col1 = cursor.getColumnIndexOrThrow(tCache.COL1_NAME);
            while(cursor.moveToNext()) {
                long id = cursor.getLong(col0);
                new postJson().execute(cursor.getString(col1));
                dbase.execSQL("DELETE FROM " + tCache.TABLE_NAME + String.format(" WHERE _ID = %d;", id));
            }
            cursor.close();
            long rows = DatabaseUtils.queryNumEntries(dbase, tCache.TABLE_NAME);
            dbase.close();
            apiCached.set(rows);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private class postJson extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            String jsonString = args[0];
            try {
                HttpURLConnection apiConnection = (HttpURLConnection) apiValues.openConnection();
                apiConnection.setConnectTimeout(5000);
                apiConnection.setRequestMethod("POST");
                apiConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                apiConnection.setRequestProperty("Accept", "application/json");
                apiConnection.setDoOutput(true);
                apiConnection.getOutputStream().write(jsonString.getBytes());
                if (apiConnection.getResponseCode() == 200) {
                    jsonString = null;
                    apiOK = true;
                }
                apiConnection.disconnect();
            } catch (java.net.SocketTimeoutException e) {
                apiOK = false;
            } catch (IOException e) {
                e.printStackTrace();
                apiOK = false;
            }
            return jsonString;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null) {
                ContentValues values = new ContentValues();
                values.put(tCache.COL1_NAME, result);
                try {
                    DBHelper helper = new DBHelper(apiContext);
                    SQLiteDatabase dbase = helper.getWritableDatabase();
                    if(dbase.insert(tCache.TABLE_NAME,"", values) != -1){
                        apiCached.incrementAndGet();
                    };
                    dbase.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            super.onPostExecute(result);
        }
    }
}
