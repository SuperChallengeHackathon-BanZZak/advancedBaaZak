package org.tensorflow.lite.examples.posenet.AppDB;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.tensorflow.lite.examples.posenet.AlarmDB.AlarmInfoDAO;
import org.tensorflow.lite.examples.posenet.AlarmDB.AlarmInfoDAO_Impl;

@SuppressWarnings("unchecked")
public final class AppDB_Impl extends AppDB {
  private volatile AlarmInfoDAO _alarmInfoDAO;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `alarm_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hour` TEXT NOT NULL, `minute` TEXT NOT NULL, `ampm` TEXT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"30bf5bb8d4d6028504ac72f0b998cb00\")");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `alarm_table`");
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsAlarmTable = new HashMap<String, TableInfo.Column>(4);
        _columnsAlarmTable.put("id", new TableInfo.Column("id", "INTEGER", true, 1));
        _columnsAlarmTable.put("hour", new TableInfo.Column("hour", "TEXT", true, 0));
        _columnsAlarmTable.put("minute", new TableInfo.Column("minute", "TEXT", true, 0));
        _columnsAlarmTable.put("ampm", new TableInfo.Column("ampm", "TEXT", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAlarmTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAlarmTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAlarmTable = new TableInfo("alarm_table", _columnsAlarmTable, _foreignKeysAlarmTable, _indicesAlarmTable);
        final TableInfo _existingAlarmTable = TableInfo.read(_db, "alarm_table");
        if (! _infoAlarmTable.equals(_existingAlarmTable)) {
          throw new IllegalStateException("Migration didn't properly handle alarm_table(org.tensorflow.lite.examples.posenet.AlarmDB.AlarmInfo).\n"
                  + " Expected:\n" + _infoAlarmTable + "\n"
                  + " Found:\n" + _existingAlarmTable);
        }
      }
    }, "30bf5bb8d4d6028504ac72f0b998cb00", "6a6665e318021cb5d12a04c3c31316fb");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "alarm_table");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `alarm_table`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  public AlarmInfoDAO alarmInfoDAO() {
    if (_alarmInfoDAO != null) {
      return _alarmInfoDAO;
    } else {
      synchronized(this) {
        if(_alarmInfoDAO == null) {
          _alarmInfoDAO = new AlarmInfoDAO_Impl(this);
        }
        return _alarmInfoDAO;
      }
    }
  }
}
