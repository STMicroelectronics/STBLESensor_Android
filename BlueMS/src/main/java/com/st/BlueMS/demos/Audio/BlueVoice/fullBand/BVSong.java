/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.BlueMS.demos.Audio.BlueVoice.fullBand;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Class that defines the status of a playable song
 */
public class BVSong implements Comparable<BVSong>, Parcelable {
    private String title;
    private String path;
    private Uri uriPath;
    private String type;
    private boolean isPlaying;
    private boolean isDemoSong;

    public BVSong(String path, boolean isDemoSong) {
        this.path = path;
        String[] splittedPath = this.path.split("/");
        String lastElem = splittedPath[splittedPath.length - 1];
        String[] splittedLastElem = lastElem.split("\\.");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < splittedLastElem.length-1 ; i++){
            sb.append(splittedLastElem[i]);
        }
        this.title = sb.toString();
        this.type = splittedPath[splittedPath.length - 1].split("\\.")[splittedLastElem.length-1];
        this.isPlaying = false;
        this.isDemoSong = isDemoSong;
    }

    public BVSong(Uri path, boolean isDemoSong) {
        uriPath = path;
        String[] splittedPath = uriPath.getLastPathSegment().split("/");
        String lastElem = splittedPath[splittedPath.length - 1];
        String[] splittedLastElem = lastElem.split("\\.");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < splittedLastElem.length-1 ; i++){
            sb.append(splittedLastElem[i]);
        }
        this.title = sb.toString();
        this.type = splittedLastElem[splittedLastElem.length-1];
        this.isPlaying = false;
        this.isDemoSong = isDemoSong;
    }

    protected BVSong(Parcel in) {
        title = in.readString();
        path = in.readString();
        uriPath = in.readParcelable(Uri.class.getClassLoader());
        type = in.readString();
        isPlaying = in.readByte() != 0;
        isDemoSong = in.readByte() != 0;
    }

    public static final Creator<BVSong> CREATOR = new Creator<BVSong>() {
        @Override
        public BVSong createFromParcel(Parcel in) {
            return new BVSong(in);
        }

        @Override
        public BVSong[] newArray(int size) {
            return new BVSong[size];
        }
    };

    /**
     * Get the song title
     * @return the title of the song
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the song file path
     * @return the path of the song file
     */
    public String getPath() {
        return path;
    }
    public Uri getUri() {
        return uriPath;
    }

    /**
     * Get the song type (extension)
     * @return the extension of the song file
     */
    public String getType() {
        return type;
    }

    /**
     * Return the song playing status
     * @return true if the selected song is playing, false elsewhere
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Set the song playing status
     * @param playing the new status
     */
    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    /**
     * Return true if this is a demo song (saved in assets .apk folder)
     * @return true if it is a demo song, false elsewhere
     */
    public boolean isDemoSong() {
        return isDemoSong;
    }

    @Override
    public int compareTo(@NonNull BVSong another) {
        return this.getTitle().compareTo(another.getTitle());
    }

    @Override
    public String toString() {
        return "BVSong{" +
                "title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(path);
        parcel.writeString(type);
    }
}
