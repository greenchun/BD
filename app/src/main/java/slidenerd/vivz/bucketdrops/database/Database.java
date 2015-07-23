package slidenerd.vivz.bucketdrops.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import slidenerd.vivz.bucketdrops.beans.Drop;

/**
 * Created by vivz on 08/07/15.
 */
public class Database {


    private Helper mHelper;
    private SQLiteDatabase mDatabase;

    /**
     * Creates application database.
     *
     * @author itcuties
     */
    public Database(Context context) {
        mHelper = new Helper(context);
        mDatabase = mHelper.getWritableDatabase();
    }

    /**
     * Return a cursor object with all rows in the table.
     *
     * @return SimpleItemTouchHelperCallback cursor suitable for use in a SimpleCursorAdapter
     */
    public Cursor readAll() {
        String columns[] = new String[]{Helper.COL_ID, Helper.COL_WHAT, Helper.COL_ADDED, Helper.COL_WHEN, Helper.COL_STATUS};
        Cursor cursor = mDatabase.query(Helper.TABLE_NAME, columns, null, null, null, null, null);
        return cursor;
    }

    /**
     * @return Cursor object containing all drops ordered in such a way that the drop with the nearest target date comes first
     */
    public Cursor readAllSortedByDateAddedAsc() {
        String columns[] = new String[]{Helper.COL_ID, Helper.COL_WHAT, Helper.COL_ADDED, Helper.COL_WHEN, Helper.COL_STATUS};
        Cursor cursor = mDatabase.query(Helper.TABLE_NAME, columns, null, null, null, null, Helper.COL_WHEN + " ASC");
        return cursor;
    }

    /**
     * @return Cursor object containing all drops ordered in such a way that the drop with the nearest target date comes last
     */
    public Cursor readAllSortedByDateAddedDesc() {
        String columns[] = new String[]{Helper.COL_ID, Helper.COL_WHAT, Helper.COL_ADDED, Helper.COL_WHEN, Helper.COL_STATUS};
        Cursor cursor = mDatabase.query(Helper.TABLE_NAME, columns, null, null, null, null, Helper.COL_WHEN + " DESC");
        return cursor;
    }

    /**
     * @return Cursor object containing all drops ordered in such a way that the ones marked complete by the user are returned
     */
    public Cursor readAllComplete() {
        String columns[] = new String[]{Helper.COL_ID, Helper.COL_WHAT, Helper.COL_ADDED, Helper.COL_WHEN, Helper.COL_STATUS};
        String selection = Helper.COL_STATUS + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(1)};
        String orderBy = Helper.COL_WHEN + " ASC";
        Cursor cursor = mDatabase.query(Helper.TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
        return cursor;
    }

    /**
     * @return Cursor object containing all the drops ordered in such a way that the ones that are not marked complete by the user are returned. Notice there is a significant difference in saying "not marked" vs "incomplete" in this app because any drop that the user of this app has not marked complete is assumed to be incomplete whether it is yet to approach its target date or its target date has already elapsed
     */
    public Cursor readAllIncomplete() {
        String columns[] = new String[]{Helper.COL_ID, Helper.COL_WHAT, Helper.COL_ADDED, Helper.COL_WHEN, Helper.COL_STATUS};
        String selection = Helper.COL_STATUS + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(0)};
        String orderBy = Helper.COL_WHEN + " ASC";
        Cursor cursor = mDatabase.query(Helper.TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
        return cursor;
    }

    /**
     * @return An ArrayList containing all the Drops retrieved in the order in which the user added them to the database
     */
    public ArrayList<Drop> getAllDrops() {
        String[] columns = new String[]{Helper.COL_WHAT, Helper.COL_ADDED, Helper.COL_WHEN, Helper.COL_STATUS};
        String orderBy = Helper.COL_WHEN + " ASC";
        Cursor cursor = mDatabase.query(Helper.TABLE_NAME, columns, null, null, null, null, orderBy);
        ArrayList<Drop> listDrops = new ArrayList<>();
        while (cursor.moveToNext()) {
            String what = cursor.getString(cursor.getColumnIndex(Helper.COL_WHAT));
            long added = cursor.getLong(cursor.getColumnIndex(Helper.COL_ADDED));
            long when = cursor.getLong(cursor.getColumnIndex(Helper.COL_WHEN));
            boolean status = cursor.getInt(cursor.getColumnIndex(Helper.COL_STATUS)) == 1 ? true : false;
            Drop drop = new Drop(what, added, when, status);
            listDrops.add(drop);
        }
        cursor.close();
        return listDrops;
    }

    /**
     * Add a new row to the database table
     *
     * @return The unique id of the newly added row
     */
    public long insert(Drop drop) {
        if (drop == null) return 0;
        ContentValues row = new ContentValues();
        row.put(Helper.COL_WHAT, drop.what);
        row.put(Helper.COL_ADDED, drop.added);
        row.put(Helper.COL_WHEN, drop.when);
        row.put(Helper.COL_STATUS, drop.status);
        long id = mDatabase.insert(Helper.TABLE_NAME, null, row);
        return id;
    }

    /**
     * @param todoId is the value of _id of a Drop that you want to delete from the SQLite database
     * @return an integer indicating the number of rows that were removed in this case
     */
    public int delete(long todoId) {
        int numberOfRowsDeleted = 0;
        String whereClause = Helper.COL_ID + " =? ";
        String[] whereArgs = new String[]{String.valueOf(todoId)};
        numberOfRowsDeleted = mDatabase.delete(Helper.TABLE_NAME, whereClause, whereArgs);
        return numberOfRowsDeleted;
    }

    /**
     * Delete all the items from the 'Drop' table and reset the auto increment SQLite counter for the primary index column _id
     */
    public void deleteAll() {
        mDatabase.delete(Helper.TABLE_NAME, null, null);
        mDatabase.delete("sqlite_sequence", "name = ?", new String[]{Helper.TABLE_NAME});
    }


    /**
     * @param dropId is the value of the column _id of a Drop which we would like to mark as complete as and when triggered by the user
     * @return an integer value indicating the number of rows updated in this case
     */
    public int markAsComplete(long dropId) {
        int numberOfRowsUpdated = 0;
        if (dropId >= 0) {
            ContentValues row = new ContentValues();
            row.put(Helper.COL_STATUS, 1);
            numberOfRowsUpdated = mDatabase.update(Helper.TABLE_NAME, row, Helper.COL_ID + " = ?", new String[]{dropId + ""});
        }
        return numberOfRowsUpdated;
    }

    /**
     * @param drop an object whose itemid or value of the column _id we would like to retrieve
     * @return the value of the item id or column _id of the given drop
     */
    public long getId(Drop drop) {
        long rowId = -1;
        String[] columns = {Helper.COL_ID};
        String selection = Helper.COL_WHAT + " = ? AND " + Helper.COL_ADDED + " = ? AND " + Helper.COL_WHEN + " = ? AND " + Helper.COL_STATUS + " = ? ";
        String[] selectionArgs = new String[]{drop.what, drop.added + "", drop.when + "", drop.getStatusAsString()};
        Cursor cursor = mDatabase.query(Helper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            rowId = cursor.getInt(cursor.getColumnIndex(Helper.COL_ID));
        }
        return rowId;
    }

    public class Helper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "bucket_db";
        public static final String TABLE_NAME = "bucket_drops";
        public static final int DATABASE_VERSION = 1;

        public static final String COL_ID = "_id";
        public static final String COL_WHAT = "todo_what";
        public static final String COL_ADDED = "todo_added";
        public static final String COL_WHEN = "todo_when";
        public static final String COL_STATUS = "todo_status";
        public static final String CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_WHAT + " TEXT NOT NULL," +
                COL_ADDED + " INTEGER NOT NULL," +
                COL_WHEN + " INTEGER NOT NULL," +
                COL_STATUS + " INTEGER DEFAULT 0)";


        public Helper(Context context) {
            // Databse: todos_db, Version: 1
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Execute create table SQL
            db.execSQL(CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
            // DROP table
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            // Recreate table
            onCreate(db);
        }
    }
}
