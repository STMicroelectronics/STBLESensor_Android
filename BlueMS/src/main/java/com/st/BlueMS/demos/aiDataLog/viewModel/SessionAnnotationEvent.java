package com.st.BlueMS.demos.aiDataLog.viewModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.st.BlueMS.demos.aiDataLog.repository.Annotation;

import java.util.ArrayList;
import java.util.Date;

public class SessionAnnotationEvent implements Parcelable{

    public final Date sessionStart;
    public final Date eventTime;
    public final ArrayList<Annotation> labels;

    SessionAnnotationEvent(Date sessionStart, Date eventTime, ArrayList<Annotation> labels) {
        this.sessionStart = sessionStart;
        this.eventTime = eventTime;
        this.labels = labels;
    }

    private SessionAnnotationEvent(Parcel in) {
        sessionStart = new Date(in.readLong());
        eventTime = new Date(in.readLong());
        labels = in.createTypedArrayList(Annotation.CREATOR);
    }

    public static final Creator<SessionAnnotationEvent> CREATOR = new Creator<SessionAnnotationEvent>() {
        @Override
        public SessionAnnotationEvent createFromParcel(Parcel in) {
            return new SessionAnnotationEvent(in);
        }

        @Override
        public SessionAnnotationEvent[] newArray(int size) {
            return new SessionAnnotationEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(sessionStart.getTime());
        dest.writeLong(eventTime.getTime());
        dest.writeTypedList(labels);
    }
}
