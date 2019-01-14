package com.st.BlueMS.demos.aiDataLog.repository;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface AnnotationDao {

    @Query("SELECT * FROM Annotation")
    LiveData<List<Annotation>> getAll();

    @Delete
    void delete(Annotation annotation);

    @Insert
    void add(Annotation ... annotation);

}
