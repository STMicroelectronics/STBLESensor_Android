package com.st.BlueMS.demos.aiDataLog.repository;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(indices = {@Index(value = {"label"},
        unique = true)})
public class Annotation implements Parcelable{

    private static final int MAX_ANNOTATION_LENGTH = 18;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "label")
    public final String label;

    private void checkIsValidAnnotation(String label){
        if(label.isEmpty())
            throw new IllegalArgumentException("Empty Annotation");
        if(label.length()>=18){
            throw new IllegalArgumentException("Annotation lenght >"+MAX_ANNOTATION_LENGTH);
        }
    }

    public Annotation(@NonNull String label){
        String trimLabel= label.trim();
        checkIsValidAnnotation(trimLabel);
        this.label=trimLabel;

    }

    protected Annotation(Parcel in) {
        label = in.readString();
    }

    public static final Creator<Annotation> CREATOR = new Creator<Annotation>() {
        @Override
        public Annotation createFromParcel(Parcel in) {
            return new Annotation(in);
        }

        @Override
        public Annotation[] newArray(int size) {
            return new Annotation[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Annotation that = (Annotation) o;

        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
    }
}
