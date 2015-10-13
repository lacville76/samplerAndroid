package com.ms.utils;
 
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import com.ms.activities.SequencerActivity;
 

 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
 
public class SQLiteHelper extends SQLiteOpenHelper {
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "TrackDB";
 
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_BOOK_TRACK = "CREATE TABLE tracks ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
               "source TEXT)";
 
        // create books table
        db.execSQL(CREATE_BOOK_TRACK);
       
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS tracks");
 
        // create fresh books table
        this.onCreate(db);
    }
    //---------------------------------------------------------------------
 
    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */
 
  
    private static final String TABLE_TRACKS = "tracks";
 
   
    private static final String KEY_ID = "id";

    private static final String KEY_SOURCE = "source";
 
    private static final String[] COLUMNS = {KEY_ID,KEY_SOURCE};
 
    public void addTrack(Track track){
        Log.d("addTrack", track.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
          values.put(KEY_SOURCE, track.getSource()); 
 
        // 3. insert
        db.insert(TABLE_TRACKS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
 
        // 4. close
        db.close(); 
    }
 
    public Track getTrack(int id){
 
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
 
        // 2. build query
        Cursor cursor = 
                db.query(TABLE_TRACKS, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections 
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
 
        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();
 
        // 4. build book object
        Track track = new Track();
        track.setId(Integer.parseInt(cursor.getString(0)));
        track.setSource(cursor.getString(1));
       
 
        Log.d("getTrack("+id+")", track.toString());
 
        // 5. return book
        return track;
    }
 
    
    public List<Track> getAllTracks() {
        List<Track> tracks = new LinkedList<Track>();
 
        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_TRACKS;
 
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
 
        // 3. go over each row, build book and add it to list
        Track track = null;
        if (cursor.moveToFirst()) {
            do {
            	track = new Track();
            	track.setId(Integer.parseInt(cursor.getString(0)));
            	track.setSource(cursor.getString(1));
             
 
           
            	tracks.add(track);
            } while (cursor.moveToNext());
        }
 
        Log.d("getAllTracks()", track.toString());
 
        // return books
        return tracks;
    }
 
     // Updating single book
    public int updateTrack(Track track,String path) {
 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("source", path); 
       
 
        // 3. updating row
        int i = db.update(TABLE_TRACKS, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(track.getId()) }); //selection args
 
        // 4. close
        db.close();
 
        return i;
 
    }
 
    // Deleting single book
    public void deleteTrack(Track track) {
 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. delete
        db.delete(TABLE_TRACKS,
                KEY_ID+" = ?",
                new String[] { String.valueOf(track.getId()) });
 
        // 3. close
        db.close();
 
        Log.d("deleteTrack", track.toString());
 
    }
    public void clearTable()   {
    	   SQLiteDatabase db = this.getWritableDatabase();
    	 //  db.execSQL("delete from 'tracks'");
    	 //  db.execSQL("delete from sqlite_sequence where name='tracks';");
    }
    
    public void createTable()
    {
    	 this.clearTable();
  	   for (int i=1; i<17; i++)
  	   {
  		   this.addTrack(new Track("null"));
  		   
  	   }
    }
    public void writeToSD(String DB_Path) throws IOException {
 
    	String DB_PATH = DB_Path;
    	File sd = Environment.getExternalStorageDirectory();
     
        if (sd.canWrite()) {
            String currentDBPath = DATABASE_NAME;
            String backupDBPath = "backupname.db";
            File currentDB = new File(DB_PATH, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        }
    }
    
}