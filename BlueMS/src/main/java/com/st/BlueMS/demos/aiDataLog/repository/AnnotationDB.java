package com.st.BlueMS.demos.aiDataLog.repository;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities = {Annotation.class},
        version = 1,
        exportSchema = false)
public abstract class AnnotationDB extends RoomDatabase {
    
    public abstract AnnotationDao getAnnotations();


    private static volatile AnnotationDB INSTANCE;

    static AnnotationDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AnnotationDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AnnotationDB.class, "AnnotationDB")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
