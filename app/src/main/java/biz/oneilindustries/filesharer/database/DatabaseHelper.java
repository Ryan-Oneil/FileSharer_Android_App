package biz.oneilindustries.filesharer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String KEY_ROWID = "_id";

    public static final String DATABASE_NAME = "FileSharer.db";
    public static final int DATABASE_VERSION = 2;

    public static final String DATABASE_TABLE_LINK = "Link";
    public static final String KEY_LINK_TITLE = "title";
    public static final String KEY_LINK_EXPIRES = "expires";
    public static final String KEY_LINK_CREATION = "creation";
    public static final String KEY_LINK_SIZE = "size";
    public static final String KEY_LINK_VIEWS = "views";

    public static final String DATABASE_TABLE_SHAREDFILE = "shared_file";
    public static final String KEY_FILE_NAME = "name";
    public static final String KEY_FILE_SIZE = "size";
    public static final String KEY_FILE_LINK_ID = "link_id";

    private static final String DATABASE_CREATE_LINK_TABLE =
            "create table " + DATABASE_TABLE_LINK +
                    " (_id text primary key not null, " +
                    "title text," +
                    "expires datetime not null," +
                    "creation datetime not null," +
                    "size double," +
                    "views integer);";

    private static final String DATABASE_CREATE_SHARED_TABLE =
            "create table " + DATABASE_TABLE_SHAREDFILE +
                    " (_id text primary key not null, " +
                    "name text not null," +
                    "size double," +
                    "link_id integer not null);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_LINK_TABLE);
        db.execSQL(DATABASE_CREATE_SHARED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}

