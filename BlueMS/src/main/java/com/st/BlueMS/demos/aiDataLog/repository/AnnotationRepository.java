package com.st.BlueMS.demos.aiDataLog.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnnotationRepository {

    private static ExecutorService DB_THREAD = Executors.newFixedThreadPool(1);

    private AnnotationDao annotationDao;
    private LiveData<List<Annotation>> allAnnotation;

    public AnnotationRepository(Application appContext){
        annotationDao = AnnotationDB.getDatabase(appContext).getAnnotations();
        allAnnotation = annotationDao.getAll();
    }

    public LiveData<List<Annotation>> getAllAnnotation() {
        return allAnnotation;
    }

    public void add(Annotation annotation){
        DB_THREAD.execute(()-> annotationDao.add(annotation));
    }

    public void remove(Annotation annotation){
        DB_THREAD.execute(()->annotationDao.delete(annotation));
    }

}
