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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.st.BlueMS.R;

import java.util.List;

/**
 * Adapter view for a list of discovered songs
 */
class SongViewAdapter extends RecyclerView.Adapter<SongViewAdapter.ViewHolder> {

    private List<BVSong> mValues;

    /**
     * Interface to use when a song is selected by the user
     */
    public interface OnSongSelectedListener{
        /**
         * function call when a song is selected by the user
         * @param s selected song
         */
        void onSongSelected(@NonNull BVSong s, int itemPosition);
    }


    private OnSongSelectedListener mListener;

    SongViewAdapter(List<BVSong> items, OnSongSelectedListener listener) {
        mListener = listener;
        mValues = items;
    }//NodeRecyclerViewAdapter

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_list_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final BVSong s = mValues.get(position);
        holder.song = s;
        holder.titleLabel.setText(s.getTitle());
        String type = holder.typeLabel.getContext().getString(R.string.blueVoiceFB_songTypeFormat,s.getType());
        holder.typeLabel.setText(type);

        if(s.isPlaying()){
            holder.playButton.setIconResource(R.drawable.ic_stop);
            holder.playButton.setText(R.string.blueVoiceFB_stop);
            holder.itemView.setSelected(true);
        }else{
            holder.playButton.setIconResource(R.drawable.ic_play_arrow);
            holder.playButton.setText(R.string.blueVoiceFB_play);
            holder.itemView.setSelected(false);
        }

        holder.playButton.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onSongSelected(holder.song,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final MaterialButton playButton;
        final TextView titleLabel;
        final TextView typeLabel;
        BVSong song;

        ViewHolder(View view) {
            super(view);
            playButton = view.findViewById(R.id.blueVoice_songItem_playButton);
            titleLabel = view.findViewById(R.id.blueVoice_songItem_title);
            typeLabel = view.findViewById(R.id.blueVoice_songItem_type);
        }
    }
}
