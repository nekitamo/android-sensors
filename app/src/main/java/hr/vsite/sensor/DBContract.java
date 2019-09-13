package hr.vsite.sensor;

import android.provider.BaseColumns;

public final class DBContract {
    private DBContract() {}

    public static abstract class tCache implements BaseColumns {
        public static final String TABLE_NAME = "Cache";
        public static final String COL1_NAME = "jsonString";
    }
}
