package com.st.BlueMS.demos.aiDataLog.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.st.BlueMS.demos.aiDataLog.repository.Annotation;
import com.st.BlueMS.demos.aiDataLog.repository.AnnotationRepository;
import com.st.BlueMS.preference.nucleo.NucleoConsole;
import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAILogging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnnotationLogViewModel extends AndroidViewModel {
    private AnnotationRepository mRepository;

    /**
     * create a list of selectable annotation from the current stored annotation, if an annotation is
     * already manage by the ViewModel the old state will be used.
     */
    private Observer<List<Annotation>> mAnnotationToSelectableAnnotation = new Observer<List<Annotation>>() {

        private @Nullable SelectableAnnotation findSelectableAnnotation(@Nullable List<SelectableAnnotation> list,
                                                                        @NonNull Annotation annotation){
            if(list==null)
                return null;

            for (SelectableAnnotation selectableAnnotation : list){
                if(selectableAnnotation.annotation.equals(annotation))
                    return selectableAnnotation;
            }
            return null;
        }

        /**
         * create a new list of selectableAnnotation from a list of annotation.
         * if an annotation is already in the current list its value will be used otherwise a
         * deselected annotation will be created
         * @param annotations
         */
        @Override
        public void onChanged(@Nullable List<Annotation> annotations) {
            if(annotations==null){
                mSelectableAnnotation.setValue(null);
                return;
            }
            List<SelectableAnnotation> selectable = new ArrayList<>(annotations.size());
            List<SelectableAnnotation> currentData = mSelectableAnnotation.getValue();
            for(Annotation annotation : annotations) {
                SelectableAnnotation oldData = findSelectableAnnotation(currentData, annotation);
                if (oldData != null) {
                    selectable.add(oldData);
                }else{
                    selectable.add(new SelectableAnnotation(annotation));
                }//if-else
            }
            mSelectableAnnotation.setValue(selectable);

        }
    };

    /**
     * list of all the user annotation
     */
    private LiveData<List<Annotation>> mAllAnnotation;
    /**
     * list of selectable annotation
     */
    private MutableLiveData<List<SelectableAnnotation>> mSelectableAnnotation= new MutableLiveData<>();

    /**
     * boolean that tell if the user is doing an annotation session
     */
    private MutableLiveData<Boolean> mIsLogging = new MutableLiveData<>();
    private MutableLiveData<Boolean> mMissingSDError = new MutableLiveData<>();
    private MutableLiveData<Boolean> mGenericIOError = new MutableLiveData<>();

    /**
     * date where the last session start, null if there are no running session
     */
    private @Nullable Date mSessionId;

    /**
     * when a log session is running it contains the current annotation status
     */
    private MutableLiveData<SessionAnnotationEvent> mLastAnnotation = new MutableLiveData<>();

    public AnnotationLogViewModel(Application application) {
        super(application);
        mRepository = new AnnotationRepository(application);
        mAllAnnotation = mRepository.getAllAnnotation();
        mIsLogging.setValue(false);
        mAllAnnotation.observeForever(mAnnotationToSelectableAnnotation);
    }

    public LiveData<List<SelectableAnnotation>> getAllAnnotation() { return mSelectableAnnotation; }
    public LiveData<Boolean> getIsLogging() { return mIsLogging; }

    /**
     * reset the missing sd error state
     */
    public void missSDErrorShown(){
        mMissingSDError.postValue(false);
    }
    public LiveData<Boolean> getMissingSDError() { return mMissingSDError; }
    public LiveData<Boolean> getIOError() { return mGenericIOError; }

    /**
     * reset the io error state
     */
    public void ioErrorShown(){
        mGenericIOError.postValue(false);
    }

    private ArrayList<Annotation> getSelectedAnnotation(){
        List<SelectableAnnotation> currentData = mSelectableAnnotation.getValue();
        if(currentData==null){
            return new ArrayList<>();
        }
        ArrayList<Annotation> selected = new ArrayList<>(currentData.size());
        for(SelectableAnnotation annotation : currentData) {
            if(annotation.isSelected()){
                selected.add(annotation.annotation);
            }
        }
        return selected;
    }

    public void select(SelectableAnnotation annotation){
        annotation.setSelected(true);
        if(mSessionId!=null){
            mLastAnnotation.setValue(new SessionAnnotationEvent(mSessionId,new Date(), getSelectedAnnotation()));
            if(mLoggingFeature!=null){
                mLoggingFeature.updateAnnotation(">"+annotation.annotation.label);
            }
        }
    }

    public void deselect(SelectableAnnotation annotation){
        annotation.setSelected(false);
        if(mSessionId!=null){
            mLastAnnotation.setValue(new SessionAnnotationEvent(mSessionId,new Date(), getSelectedAnnotation()));
            if(mLoggingFeature!=null) {
                mLoggingFeature.updateAnnotation("<" + annotation.annotation.label);
            }
        }
    }


    public boolean insert(String label) {
        try {
            mRepository.add(new Annotation(label));
        }catch (IllegalArgumentException e){
            Log.e(AnnotationLogViewModel.class.getName(),e.getMessage(),e);
            return false;
        }
        return true;
    }

    public void remove(SelectableAnnotation annotation){ mRepository.remove(annotation.annotation); }
    
    private @Nullable FeatureAILogging mLoggingFeature;
    private Feature.FeatureListener mLoggingStatus = (f, sample) -> {
        FeatureAILogging featureAILogging = (FeatureAILogging) f;
        @FeatureAILogging.LoggingStatus int status = FeatureAILogging.getLoggingStatus(sample);
        switch (status){
            case FeatureAILogging.LOGGING_STARTED:
                mSessionId = new Date();
                mLastAnnotation.postValue(new SessionAnnotationEvent(mSessionId,new Date(), getSelectedAnnotation()));
                mIsLogging.postValue(true);
                mGenericIOError.postValue(false);
                mMissingSDError.postValue(false);
                return;
            case FeatureAILogging.LOGGING_STOPPED:
                mSessionId = null;
                mIsLogging.postValue(false);
                return;
            case FeatureAILogging.LOGGING_IO_ERROR:
                featureAILogging.stopLogging();
                mGenericIOError.postValue(true);
                return;
            case FeatureAILogging.LOGGING_NO_SD:
                featureAILogging.stopLogging();
                mMissingSDError.postValue(true);
                return;
            case FeatureAILogging.LOGGING_UNKNOWN:
                featureAILogging.stopLogging();
        }
    };

    public void start(@Nullable FeatureAILogging feature){
        mLoggingFeature = feature;
        if(mLoggingFeature != null) {
            mLoggingFeature.addFeatureListener(mLoggingStatus);
            mLoggingFeature.getParentNode().enableNotification(mLoggingFeature);
        }
    }


    public void stop(){
        if(mLoggingFeature != null) {
            mLoggingFeature.removeFeatureListener(mLoggingStatus);
            mLoggingFeature.getParentNode().disableNotification(mLoggingFeature);
        }
        mLoggingFeature = null;
    }


    public LiveData<SessionAnnotationEvent> getLastAnnotationEvent() {
        return mLastAnnotation;
    }

    @Override
    protected void onCleared() {
        mAllAnnotation.removeObserver(mAnnotationToSelectableAnnotation);
        super.onCleared();
    }

    private void setNodeTime(@Nullable Debug console){
        if(console!=null){
            new NucleoConsole(console).setDateAndTime(new Date());
        }
    }
    private void startLogging(long featureMask, float environmentalFreq, float inertialFreq, float audioVolume){
        if(mLoggingFeature!=null) {
            setNodeTime(mLoggingFeature.getParentNode().getDebug());
            byte volume = (byte)(audioVolume*32.0f);
            mLoggingFeature.startLogging(featureMask, environmentalFreq, inertialFreq,volume);
        }
    }

    private void stopLogging(){
        mLastAnnotation.setValue(new SessionAnnotationEvent(mSessionId, new Date(), new ArrayList<>()));
        if(mLoggingFeature!=null)
            mLoggingFeature.stopLogging();
    }

    /**
     * start a new annotation session or ai_log_stop the current one
     */
    public void startStopLogging(long featureMask,float environmentalFreq,float inertialFreq, float audioVolume) {
        Boolean isLogging = mIsLogging.getValue();
        if(isLogging==null || !isLogging){
            startLogging(featureMask,environmentalFreq,inertialFreq,audioVolume);
        }else{
            stopLogging();
        }
    }
}
