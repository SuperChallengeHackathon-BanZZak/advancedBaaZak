package org.tensorflow.lite.examples.posenet.AlarmDB;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("unchecked")
public final class AlarmInfoDAO_Impl implements AlarmInfoDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfAlarmInfo;

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfAlarmInfo;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public AlarmInfoDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAlarmInfo = new EntityInsertionAdapter<AlarmInfo>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `alarm_table`(`id`,`hour`,`minute`,`ampm`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AlarmInfo value) {
        stmt.bindLong(1, value.getId());
        if (value.getHour() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getHour());
        }
        if (value.getMin() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getMin());
        }
        if (value.getAmpm() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getAmpm());
        }
      }
    };
    this.__deletionAdapterOfAlarmInfo = new EntityDeletionOrUpdateAdapter<AlarmInfo>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `alarm_table` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AlarmInfo value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM alarm_table";
        return _query;
      }
    };
  }

  @Override
  public void insert(final AlarmInfo alarmInfo) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfAlarmInfo.insert(alarmInfo);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAlarm(final AlarmInfo alarmInfo) {
    __db.beginTransaction();
    try {
      __deletionAdapterOfAlarmInfo.handle(alarmInfo);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public LiveData<List<AlarmInfo>> getAllAlarm() {
    final String _sql = "SELECT * FROM alarm_table ORDER BY hour and minute ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"alarm_table"}, new Callable<List<AlarmInfo>>() {
      @Override
      public List<AlarmInfo> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMin = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfAmpm = CursorUtil.getColumnIndexOrThrow(_cursor, "ampm");
          final List<AlarmInfo> _result = new ArrayList<AlarmInfo>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AlarmInfo _item;
            _item = new AlarmInfo();
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            final String _tmpHour;
            _tmpHour = _cursor.getString(_cursorIndexOfHour);
            _item.setHour(_tmpHour);
            final String _tmpMin;
            _tmpMin = _cursor.getString(_cursorIndexOfMin);
            _item.setMin(_tmpMin);
            final String _tmpAmpm;
            _tmpAmpm = _cursor.getString(_cursorIndexOfAmpm);
            _item.setAmpm(_tmpAmpm);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<AlarmInfo> getAll() {
    final String _sql = "SELECT * FROM alarm_table";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = DBUtil.query(__db, _statement, false);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
      final int _cursorIndexOfMin = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
      final int _cursorIndexOfAmpm = CursorUtil.getColumnIndexOrThrow(_cursor, "ampm");
      final List<AlarmInfo> _result = new ArrayList<AlarmInfo>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final AlarmInfo _item;
        _item = new AlarmInfo();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpHour;
        _tmpHour = _cursor.getString(_cursorIndexOfHour);
        _item.setHour(_tmpHour);
        final String _tmpMin;
        _tmpMin = _cursor.getString(_cursorIndexOfMin);
        _item.setMin(_tmpMin);
        final String _tmpAmpm;
        _tmpAmpm = _cursor.getString(_cursorIndexOfAmpm);
        _item.setAmpm(_tmpAmpm);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
