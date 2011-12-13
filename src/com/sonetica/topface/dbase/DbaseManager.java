package com.sonetica.topface.dbase;

import java.util.ArrayList;
import com.sonetica.topface.utils.Debug;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 *   Менеджер для работы с базой данных
 */
public class DbaseManager extends SQLiteOpenHelper {
  // Data
  private SQLiteDatabase mDBase;
  // Constants
  private static final String DB_NAME = "topface";
  private static final int DB_VERSION = 1;
  // Table Owener Profile
  private static final String TABLE_PROFILE  = "profile";
  private static final String PROFILE_NAME   = "name";
  private static final String PROFILE_TEXT   = "text";
  private static final String CREATE_PROFILE = 
      "create table " + TABLE_PROFILE + " (_id integer primary key autoincrement, " + PROFILE_NAME + " TEXT, " + PROFILE_TEXT + " TEXT)";
  // Table Tops file names
  private static final String TABLE_TOPS     = "tops";
  private static final String TOPS_POSITION  = "position";
  private static final String TOPS_FILENAME  = "filename";
  private static final String CREATE_TOPS    = 
      "create table " + TABLE_TOPS + " (_id integer primary key autoincrement, " + TOPS_POSITION + " INTEGER, " + TOPS_FILENAME + " TEXT)";
  //---------------------------------------------------------------------------
  public DbaseManager(Context context) {
    super(context,DB_NAME,null,DB_VERSION);
    //context.deleteDatabase(DB_NAME);
    mDBase = getWritableDatabase();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onCreate(SQLiteDatabase db) {
    //db.execSQL(CREATE_PROFILE);
    db.execSQL(CREATE_TOPS);
  }
  //---------------------------------------------------------------------------
  //private static final String  ALTER_TABLE_CHATS = "alter table " + TABLE_NAME + " add " + CLIENT_ID + " TEXT" ;
  @Override
  public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
    if(oldVersion==newVersion)
      return;
    
    if(db.isReadOnly())
      return;
  }
  //---------------------------------------------------------------------------
  public void insert(int pos,String name) {
    ContentValues values = new ContentValues();
    values.put(TOPS_POSITION,pos);
    values.put(TOPS_FILENAME,name);
    //mDBase.insert(TABLE_TOPS,null,values);
    mDBase.replace(TABLE_TOPS,null,values);
    Debug.log(null,"insert: pos:"+pos+"name:"+name);
  }
  //---------------------------------------------------------------------------
  public ArrayList<String> getTops() {
    ArrayList<String> array = new ArrayList<String>();
    Cursor cursor = mDBase.query(TABLE_TOPS,new String[]{TOPS_FILENAME},null,null,null,null,TOPS_POSITION);
    while(cursor.moveToNext())
      array.add(cursor.getString(0));
    cursor.close();
    return array;    
  }
  //---------------------------------------------------------------------------
  public void close() {
    mDBase.close();    
  }
  //---------------------------------------------------------------------------
//    if (oldVersion < 3 && newVersion == 3 )
//      db.execSQL(CREATE_TABLE_CHATS);
//    if(oldVersion > 3 && newVersion < 11) {
//      db.execSQL(ALTER_TABLE_CHATS);
//      db.execSQL(ALTER_TABLE_CHATS2);
//    }
//    if(oldVersion < 11)
//      db.execSQL(ALTER_TABLE_CHATS3);
//  }
  //---------------------------------------------------------------------------
//  public Cursor getChats() {
//    SQLiteDatabase db = getReadableDatabase();
//    Cursor cursor = db.query(
//        TABLE_NAME_CHATS, new String[] { USER_ID, TEXT, CREATED, NEW_COUNT, "_id" }, 
//        null, null, null, null, NEW_COUNT + " DESC, " + CREATED + " DESC");
//    db.close();
//
//    return cursor;
//  }
  //---------------------------------------------------------------------------
}
