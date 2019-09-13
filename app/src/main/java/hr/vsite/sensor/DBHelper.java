package hr.vsite.sensor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static hr.vsite.sensor.DBContract.tCache;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "Sensors";
    public static final int DB_VERSION = 1;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + tCache.TABLE_NAME + " (" +
                    "_ID INTEGER PRIMARY KEY, " +
                    tCache.COL1_NAME + " TEXT);";

    public static final String DROP_TABLE =
            "DROP TABLE " + tCache.TABLE_NAME + ";";

    public DBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        db.execSQL(CREATE_TABLE);
    }

}
