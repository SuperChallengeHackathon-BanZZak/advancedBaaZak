package org.tensorflow.lite.examples.posenet.AppDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.tensorflow.lite.examples.posenet.AlarmDB.AlarmInfo;
import org.tensorflow.lite.examples.posenet.AlarmDB.AlarmInfoDAO;

@Database(entities = {AlarmInfo.class}, version = 1)
public abstract class AppDB extends RoomDatabase {

    public abstract AlarmInfoDAO alarmInfoDAO();

    private static volatile AppDB INSTANCE;

    public static AppDB getDB(final Context context){
        if(INSTANCE == null){
            synchronized (AppDB.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDB.class,
                            "banzzak_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
