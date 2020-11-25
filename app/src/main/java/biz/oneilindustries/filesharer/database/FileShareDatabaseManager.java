package biz.oneilindustries.filesharer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import biz.oneilindustries.filesharer.DTO.Link;
import biz.oneilindustries.filesharer.DTO.SharedFile;

import static biz.oneilindustries.filesharer.database.DatabaseHelper.DATABASE_TABLE_LINK;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.DATABASE_TABLE_SHAREDFILE;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_FILE_LINK_ID;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_FILE_NAME;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_FILE_SIZE;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_LINK_CREATION;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_LINK_EXPIRES;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_LINK_SIZE;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_LINK_TITLE;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_LINK_VIEWS;
import static biz.oneilindustries.filesharer.database.DatabaseHelper.KEY_ROWID;

public class FileShareDatabaseManager {

    private Context context;
    private DatabaseHelper myDatabaseHelper;
    private SQLiteDatabase myDatabase;


    public FileShareDatabaseManager(Context context) {
        this.context = context;
    }

    public FileShareDatabaseManager open() throws SQLException {
        myDatabaseHelper = new DatabaseHelper(context);
        myDatabase = myDatabaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        myDatabaseHelper.close();
    }

    public ArrayList<Link> getLinks() {
        Cursor mCursor =
                myDatabase.query(true, DATABASE_TABLE_LINK, new String[]{
                                KEY_ROWID,
                                KEY_LINK_TITLE,
                                KEY_LINK_EXPIRES,
                                KEY_LINK_CREATION,
                                KEY_LINK_SIZE,
                                KEY_LINK_VIEWS
                        },
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
        } else {
            return new ArrayList<>();
        }
        return cursorsToLinks(mCursor);
    }

    public Link getLink(String id) {
        Cursor mCursor =
                myDatabase.query(true, DATABASE_TABLE_LINK, new String[]{
                                KEY_ROWID,
                                KEY_LINK_TITLE,
                                KEY_LINK_EXPIRES,
                                KEY_LINK_CREATION,
                                KEY_LINK_SIZE,
                                KEY_LINK_VIEWS
                        },
                        KEY_ROWID + "=" + String.format("'%s'", id),
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            try {
                return cursorToLink(mCursor);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ArrayList<Link> cursorsToLinks(Cursor cursor) {
        ArrayList<Link> links = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            try {
                links.add(cursorToLink(cursor));
            } catch (ParseException e) {
                Log.e("Parse exception", e.getMessage());
            }
            cursor.moveToNext();
        }
        return links;
    }

    public Link cursorToLink(Cursor cursor) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        if (cursor == null || cursor.getCount() == 0) return null;

        int id = cursor.getColumnIndex(KEY_ROWID);
        int titleIndex = cursor.getColumnIndex(KEY_LINK_TITLE);
        int expiresIndex = cursor.getColumnIndex(KEY_LINK_EXPIRES);
        int creationIndex = cursor.getColumnIndex(KEY_LINK_CREATION);
        int sizeIndex = cursor.getColumnIndex(KEY_LINK_SIZE);
        int viewsIndex = cursor.getColumnIndex(KEY_LINK_VIEWS);

        return new Link(
                cursor.getString(id),
                cursor.getString(titleIndex),
                format.parse(cursor.getString(expiresIndex)),
                format.parse(cursor.getString(creationIndex)),
                cursor.getLong(sizeIndex),
                cursor.getLong(viewsIndex)
        );
    }

    public void updateOrInsertLink(Link link) {
        ContentValues values = new ContentValues();

        values.put("_id", link.getId());
        values.put("title", link.getTitle());
        values.put("expires", link.getExpiryDatetime().toString());
        values.put("creation", link.getCreationDate().toString());
        values.put("size", link.getSize());
        values.put("views", link.getViews());

        int exists = myDatabase.update(DATABASE_TABLE_LINK, values, KEY_ROWID + "=" + String.format("'%s'", link.getId()), null);

        if (exists == 0) {
            myDatabase.insert(DATABASE_TABLE_LINK, null, values);
        }
        if (link.getFiles() != null) {
            link.getFiles().forEach(sharedFile -> updateOrInsertFile(sharedFile, link.getId()));
        }
    }

    public void updateOrInsertFile(SharedFile sharedFile, String linkId) {
        ContentValues values = new ContentValues();

        values.put("_id", sharedFile.getId());
        values.put("name", sharedFile.getName());
        values.put("size", sharedFile.getSize());
        values.put("link_id", linkId);

        int exists = myDatabase.update(DATABASE_TABLE_SHAREDFILE, values, KEY_ROWID + "=" + String.format("'%s'", sharedFile.getId()), null);

        if (exists == 0) {
            myDatabase.insert(DATABASE_TABLE_SHAREDFILE, null, values);
        }
    }

    public ArrayList<SharedFile> getsLinkFiles(String linkId) {
        Cursor mCursor =
                myDatabase.query(true, DATABASE_TABLE_SHAREDFILE, new String[]{
                                KEY_ROWID,
                                KEY_FILE_NAME,
                                KEY_FILE_SIZE,
                                KEY_FILE_LINK_ID
                        },
                        KEY_FILE_LINK_ID + "=" + String.format("'%s'", linkId),
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
        } else {
            return new ArrayList<>();
        }
        return cursorsToSharedFiles(mCursor);
    }

    public ArrayList<SharedFile> cursorsToSharedFiles(Cursor cursor) {
        ArrayList<SharedFile> sharedFiles = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            sharedFiles.add(cursorToSharedFile(cursor));
            cursor.moveToNext();
        }
        return sharedFiles;
    }

    public SharedFile cursorToSharedFile(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) return null;

        int id = cursor.getColumnIndex(KEY_ROWID);
        int nameIndex = cursor.getColumnIndex(KEY_FILE_NAME);
        int sizeIndex = cursor.getColumnIndex(KEY_FILE_SIZE);
        int linkIdIndex = cursor.getColumnIndex(KEY_FILE_LINK_ID);
        Link link = new Link();
        link.setId(cursor.getString(linkIdIndex));

        return new SharedFile(
                cursor.getString(id),
                cursor.getString(nameIndex),
                cursor.getLong(sizeIndex),
                link
        );
    }

    public void deleteFile(SharedFile file) {
        myDatabase.delete(DATABASE_TABLE_SHAREDFILE, KEY_ROWID + "=?", new String[]{file.getId()});

        Link link = file.getLink();

        if (link != null) {
            link.setSize(link.getSize() - file.getSize());
            link.setFiles(null);
            updateOrInsertLink(link);
        }
    }

    public void deleteLink(Link link) {
        myDatabase.delete(DATABASE_TABLE_LINK, KEY_ROWID + "=?", new String[]{link.getId()});
        myDatabase.delete(DATABASE_TABLE_SHAREDFILE, KEY_FILE_LINK_ID + "=?", new String[]{link.getId()});
    }

    public void clearDatabase() {
        myDatabase.execSQL("delete from " + DATABASE_TABLE_LINK);
        myDatabase.execSQL("delete from " + DATABASE_TABLE_SHAREDFILE);
    }
}
