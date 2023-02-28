package com.st.BlueSTSDK.Features

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion
import com.st.BlueSTSDK.Utils.STL2TransportProtocol

class FeatureBinaryContent constructor(n: Node) :
    Feature(FEATURE_NAME, n, arrayOf(FEATURE_BINARY_CONFIG),false) {

    private var mSTL2TransportDecoder = STL2TransportProtocol()

    companion object {
        fun getBinaryContent(sample: Sample?): ByteArray  {
            return if (sample != null) {
                val originalSample = sample.data
                val modifiedByteArray = ByteArray(sample.data.size) { i -> originalSample[i].toByte() }
                modifiedByteArray
            } else {
                byteArrayOf()
            }
        }

        fun getBinaryContentToUInt32(data: ByteArray): LongArray  {
            val longArray = LongArray(data.size / 4)
            for(i in data.indices step 4){
                if(i<=data.size) {
                    longArray[i / 4] = NumberConversion.LittleEndian.bytesToUInt32(data, i)
                }
            }
            return longArray
        }

        private const val FEATURE_NAME = "Binary Content"
        private const val FEATURE_DATA_NAME = "Binary Content"
        private val FEATURE_BINARY_CONFIG =
            Field(FEATURE_DATA_NAME, null, Field.Type.ByteArray, Byte.MAX_VALUE, Byte.MIN_VALUE)
    }


    fun setMaxPayLoadSize(payLoadSize: Int) {
        mSTL2TransportDecoder.setMaxPayLoadSize(payLoadSize)
    }

    fun getMaxPayLoadSize() = mSTL2TransportDecoder.getMaxPayLoadSize()
    fun sendBinaryContent(bytesToSend: ByteArray, maxPayloadSize :Int,onSendComplete: Runnable? = null) {
        //Log.i("BinaryContent","write =${bytesToSend.size} [Max=${maxPayloadSize}]")
        mSTL2TransportDecoder.setMaxPayLoadSize(maxPayloadSize)
        val bytesToSendEncapsulated = mSTL2TransportDecoder.encapsulate(bytesToSend)
        sendWrite(bytesToSendEncapsulated,maxPayloadSize, onSendComplete)
    }

    private fun sendWrite(bytesToSend: ByteArray, maxPayloadSize :Int, onSendComplete: Runnable?) {
        var byteSend = 0
        while (bytesToSend.size - byteSend > maxPayloadSize) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, byteSend + maxPayloadSize))
            byteSend += maxPayloadSize
        }
        if (byteSend != bytesToSend.size) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, bytesToSend.size), onSendComplete)
        } //if
    }

    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        val contentData = mSTL2TransportDecoder.decapsulate(data)

        //Log.i("BinaryContent","Notify Chunk=${data.size} [${mSTL2TransportDecoder.getMaxPayLoadSize()}")

        if(contentData!=null) {
            return ExtractResult(Sample(timestamp, contentData.toTypedArray(), fieldsDesc), data.size)
        }
        return ExtractResult(null, data.size)
    }
}