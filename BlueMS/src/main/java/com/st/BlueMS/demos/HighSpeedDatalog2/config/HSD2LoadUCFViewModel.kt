/*
 * Copyright (c) 2020  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 * STMicroelectronics company nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 * in a directory whose title begins with st_images may only be used for internal purposes and
 * shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 * icons, pictures, logos and other images that are provided with the source code in a directory
 * whose title begins with st_images.
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

package com.st.BlueMS.demos.HighSpeedDatalog2.config

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL
import com.st.BlueSTSDK.Node
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOError

internal class HSD2LoadUCFViewModel : ViewModel(){

    var context: Context?=null

    private val _error = MutableLiveData<String>(null)
    val error: LiveData<String?>
        get() = _error

    private val _isLoading = MutableLiveData(false)
    val isLoading:LiveData<Boolean>
        get() = _isLoading

    private var mPnPLFeature: FeaturePnPL? =null
    private var mCompName = ""
    private var mContName = ""
    private var mReqName = "ucf_data"
    private var mCommFields = mapOf<String,Any>()

    fun openLoadUCF(comp_name: String, cont_name: String){
        mCompName = comp_name
        mContName = cont_name
    }

    fun attachTo(node: Node){
        mPnPLFeature=node.getFeature(FeaturePnPL::class.java)
    }

    fun compressUCFString(ucfContent: String): String {
        val isSpace = "\\s+".toRegex()
        return ucfContent.lineSequence()
            .filter { isCommentLine(it) }
            .map { it.replace(isSpace, "").drop(2) }
            .joinToString("")
    }

    private fun isCommentLine(line:String):Boolean{
        return !line.startsWith("--")
    }

    fun loadUCFFromFile(file: Uri?, contentResolver: ContentResolver){
        if(file == null){
            _error.postValue("Invalid file")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val stream = contentResolver.openInputStream(file)
                if(stream==null){
                    _isLoading.postValue(false)
                    _error.postValue("Impossible to read file")
                    return@launch
                }
                val fullSize = stream.readBytes().toString(Charsets.UTF_8)
                stream.close()
                val compressedUcf = compressUCFString(fullSize)

                mPnPLFeature?.sendPnPLCommandCmd(
                    mCompName,
                    mContName,
                    mReqName,
                    mapOf("size" to compressedUcf.length, "data" to compressedUcf),
                    Runnable {
                    Log.e("MLC","LoadComplete")
                    _isLoading.postValue(false)
                    }
                )

            }catch (e: FileNotFoundException){
                e.printStackTrace()
                _isLoading.postValue(false)
                _error.postValue("File not found")
            }catch (e: IOError){
                e.printStackTrace()
                _isLoading.postValue(false)
                _error.postValue("Impossible to read file")
            }
        }
    }


}