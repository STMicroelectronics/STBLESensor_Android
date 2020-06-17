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
package com.st.BlueMS.demos.HighSpeedDataLog.tagging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.R

internal class HSDAnnotationListAdapter(private val mCallback: AnnotationInteractionCallback) :
        ListAdapter<AnnotationViewData, HSDAnnotationListAdapter.ViewHolder>(HSDAnnotationDiffCallback()) {

    interface AnnotationInteractionCallback {
        fun onAnnotationSelected(selected: AnnotationViewData)
        fun onAnnotationDeselected(deselected: AnnotationViewData)
        fun requestLabelEditing(annotation: AnnotationViewData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_annotation, parent, false)
        return ViewHolder(mCallback,view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val annotation = getItem(position)
        holder.bind(annotation)
    }

    internal class ViewHolder(mCallback: AnnotationInteractionCallback, itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var currentData: AnnotationViewData? = null

        private val tagLabel: TextView = itemView.findViewById(R.id.annotationView_label)
        private val tagDescription: TextView = itemView.findViewById(R.id.annotationView_desc)
        private val tagType: TextView = itemView.findViewById(R.id.annotationView_type)
        private val tagEditLabel: ImageView = itemView.findViewById(R.id.annotationView_editLabel)
        private val tagSelector: CompoundButton = itemView.findViewById(R.id.annotationView_selectSwitch)

        init {
            tagEditLabel.setOnClickListener {
                val annotation = currentData ?: return@setOnClickListener
                mCallback.requestLabelEditing(annotation)
            }
        }

        fun bind(annotation: AnnotationViewData) {
            currentData = annotation

            tagType.setText(annotation.tagType)
            tagLabel.setText(annotation.label)

            if(annotation.pinDesc!=null){
                tagDescription.visibility = View.VISIBLE
                tagDescription.text = annotation.pinDesc
            }else{
                tagDescription.visibility = View.INVISIBLE
            }

            tagSelector.visibility = if(annotation.userCanSelect){
                View.VISIBLE
            }else{
                View.GONE
            }

            setEditButtonVisibility(annotation.userCanEditLabel)
            setCheckedStatus(annotation.isSelected)
        }

        private fun setEditButtonVisibility(isEditable:Boolean){
            if(isEditable){
                tagEditLabel.visibility = View.VISIBLE
            }else{
                tagEditLabel.visibility = View.GONE
            }
        }

        private fun setCheckedStatus(isChecked:Boolean){
            tagSelector.setOnCheckedChangeListener(null)
            tagSelector.isChecked = isChecked
            tagSelector.setOnCheckedChangeListener(tagSelectorListener)
        }

        private val tagSelectorListener = CompoundButton.OnCheckedChangeListener{ _, isChecked ->
            val annotation = currentData ?: return@OnCheckedChangeListener
            if (isChecked) {
                mCallback.onAnnotationSelected(annotation)
            } else{
                mCallback.onAnnotationDeselected(annotation)
            }
        }

    }



}